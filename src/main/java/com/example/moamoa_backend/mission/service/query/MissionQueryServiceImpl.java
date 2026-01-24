package com.example.moamoa_backend.mission.service.query;

import com.example.moamoa_backend.keyword.entity.Keyword;
import com.example.moamoa_backend.keyword.repository.KeywordRepository;
import com.example.moamoa_backend.member.entity.mapping.MemberMission;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberMissionRepository;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.member.repository.MemberSubInterestRepository;
import com.example.moamoa_backend.mission.converter.MissionConverter;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.enums.MissionStatus;
import com.example.moamoa_backend.mission.exception.MissionException;
import com.example.moamoa_backend.mission.exception.code.MissionErrorCode;
import com.example.moamoa_backend.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionQueryServiceImpl implements MissionQueryService{

    private final MissionConverter missionConverter;
    private final MissionRepository missionRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final KeywordRepository keywordRepository;
    private final MemberRepository memberRepository;
    private final MemberSubInterestRepository memberSubInterestRepository;
    @Override
    public MissionResponseDto.MissionDetail getMissionDetail(Long memberId, Long missionId){
        Mission mission = missionRepository.findById(missionId).orElseThrow(()-> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        MemberMission memberMission = memberMissionRepository.findByMemberIdAndMissionId(memberId,missionId).orElse(null);

        return missionConverter.toMissionDetail(mission,memberMission);


    }

    @Override
    public MissionResponseDto.KeywordListResult getRecommendedKeywords() {

        List<Keyword> keywordList = keywordRepository.findAll();

        return missionConverter.toKeywordList(keywordList);
    }

    @Override
    public MissionResponseDto.KeywordListResult getRelatedKeywords(String keyword){
        List<Keyword> keywordList = keyword.isBlank() ?
                Collections.emptyList() :
                keywordRepository.findTop5ByNameContaining(keyword);
        return missionConverter.toKeywordList(keywordList);
    }

    @Override
    public List<MissionResponseDto.RecommendResult> getTodayRecommendMissions(Long memberId, Integer requestTime){

        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        List<Long> interestIds = memberSubInterestRepository.findInterestSubPairsByMemberId(memberId).stream()
                .map(MemberSubInterestRepository.InterestSubPair::getInterestId) // 인터페이스 메서드 호출
                .distinct()
                .toList();

        List<Mission> missions = missionRepository.findTodayRecommendMission(memberId,interestIds,requestTime);

        return missions.stream()
                .map(mission -> {
                    boolean isScrapped = memberMissionRepository.existsByMemberIdAndMissionIdAndMissionStatus(
                            memberId, mission.getId(), MissionStatus.SCRAP
                    );
                    return missionConverter.toRecommendResult(mission, isScrapped);
                })
                .toList();

    }

    @Override
    public MissionResponseDto.SearchResponse searchMissions(
            Long memberId, String searchText, List<String> keywords,
            Long categoryId, Long subCategoryId, Long seed, Pageable pageable
    ){
        Slice<Mission> missionSlice = missionRepository.searchMissions(
                memberId,searchText,keywords,categoryId,subCategoryId,seed,pageable
        );

        List<Long> missionIds = missionSlice.getContent().stream()
                .map(Mission::getId)
                .toList();

        Map<Long,MemberMission> myMissionMap = memberMissionRepository
                .findAllByMemberIdAndMissionIdIn(memberId,missionIds).stream()
                .collect(Collectors.toMap(
                        mm-> mm.getMission().getId(),
                        mm -> mm
                ));

        return missionConverter.toSearchResponse(missionSlice,myMissionMap);
    }
}
