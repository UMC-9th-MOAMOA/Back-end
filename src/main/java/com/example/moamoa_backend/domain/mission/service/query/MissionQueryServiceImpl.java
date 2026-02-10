package com.example.moamoa_backend.domain.mission.service.query;

import com.example.moamoa_backend.domain.keyword.entity.Keyword;
import com.example.moamoa_backend.domain.keyword.repository.KeywordRepository;
import com.example.moamoa_backend.domain.member.entity.mapping.MemberMission;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.domain.member.repository.MemberMissionRepository;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.member.repository.MemberSubInterestRepository;
import com.example.moamoa_backend.domain.mission.converter.MissionConverter;
import com.example.moamoa_backend.domain.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.domain.mission.entity.Mission;
import com.example.moamoa_backend.domain.mission.enums.MissionStatus;
import com.example.moamoa_backend.domain.mission.exception.MissionException;
import com.example.moamoa_backend.domain.mission.exception.code.MissionErrorCode;
import com.example.moamoa_backend.domain.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /**
     * 미션 상세 정보 조회
     */
    @Override
    public MissionResponseDto.MissionDetail getMissionDetail(Long memberId, Long missionId){
        Mission mission = missionRepository.findByIdWithDetail(missionId).orElseThrow(()-> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        MemberMission memberMission = memberMissionRepository.findByMemberIdAndMissionId(memberId,missionId).orElse(null);

        return missionConverter.toMissionDetail(mission,memberMission);


    }

    /**
     * 추천 키워드 전체 목록 조회
     *
     * @return DB에 저장된 모든 키워드 리스트
     */
    @Override
    public MissionResponseDto.KeywordListResult getRecommendedKeywords() {

        List<Keyword> keywordList = keywordRepository.findAll();

        return missionConverter.toKeywordList(keywordList);
    }


    /**
     * 연관 검색어 (Top 5) 조회
     *
     * @param keyword 사용자 입력 검색어
     * @return 검색어가 포함된 키워드 상위 5개
     */
    @Override
    public MissionResponseDto.KeywordListResult getRelatedKeywords(String keyword){
        List<Keyword> keywordList = keyword.isBlank() ?
                Collections.emptyList() :
                keywordRepository.findTop5ByNameContaining(keyword);
        return missionConverter.toKeywordList(keywordList);
    }

    /**
     * 오늘의 추천 미션 리스트 조회
     *
     * @param memberId 유저 ID
     * @param requestTime 유저가 설정한 가용 시간(분) - null이면 시간 제한 없음
     * @return 추천 미션 리스트 (찜 여부 포함)
     *
     */
    @Override
    public List<MissionResponseDto.RecommendResult> getTodayRecommendMissions(Long memberId, Integer requestTime){

        if (!memberRepository.existsById(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        List<Long> interestIds = memberSubInterestRepository.findInterestSubPairsByMemberId(memberId).stream()
                .map(MemberSubInterestRepository.InterestSubPair::getInterestId)
                .distinct()
                .toList();

        List<Mission> missions = missionRepository.findTodayRecommendMission(memberId, interestIds, requestTime);

        if (missions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> missionIds = missions.stream()
                .map(Mission::getId)
                .toList();

        List<MemberMission> memberMissions = memberMissionRepository.findAllByMemberIdAndMissionIdIn(memberId, missionIds);

        Set<Long> scrappedMissionIds = memberMissions.stream()
                .filter(mm -> mm.getMissionStatus() == MissionStatus.SCRAP)
                .map(mm -> mm.getMission().getId())
                .collect(Collectors.toSet());

        return missions.stream()
                .map(mission -> {
                    boolean isScrapped = scrappedMissionIds.contains(mission.getId());
                    return missionConverter.toRecommendResult(mission, isScrapped);
                })
                .toList();
    }

    /**
     * 미션 검색 (Refactored)
     */
    @Override
    public MissionResponseDto.SearchResponse searchMissions(
            Long memberId, String searchText, List<String> keywords, Long seed, Pageable pageable
    ) {
        Slice<Mission> missionSlice = missionRepository.searchMissions(
                memberId, searchText, keywords, null, null, seed, pageable
        );

        return createSearchResponse(memberId, missionSlice);
    }
    /**
     * 카테고리별 미션 조회
     */
    @Override
    public MissionResponseDto.SearchResponse getMissionsByCategory(
            Long memberId, Long categoryId, Long subCategoryId, Long seed, Pageable pageable
    ) {
        Slice<Mission> missionSlice = missionRepository.searchMissions(
                memberId, null, null, categoryId, subCategoryId, seed, pageable
        );

        return createSearchResponse(memberId, missionSlice);
    }

    @Override
    public MissionResponseDto.SearchResponse getMyMissions(Long memberId, String status, String condition, Long categoryId, Pageable pageable) {

        Slice<MissionResponseDto.RecommendResult> slice = missionRepository.getMyMissions(memberId, status, condition, categoryId, pageable);
        return missionConverter.toMyMissionsResult(slice);
    }

    private MissionResponseDto.SearchResponse createSearchResponse(Long memberId, Slice<Mission> missionSlice) {
        List<Mission> missions = missionSlice.getContent();

        if (missions.isEmpty()) {
            return missionConverter.toSearchResponse(missionSlice, Collections.emptyMap());
        }

        List<Long> missionIds = missions.stream()
                .map(Mission::getId)
                .toList();

        Map<Long, MemberMission> myMissionMap = memberMissionRepository
                .findAllByMemberIdAndMissionIdIn(memberId, missionIds).stream()
                .collect(Collectors.toMap(
                        mm -> mm.getMission().getId(),
                        mm -> mm,
                        (existing, replacement) -> existing // 중복 방지
                ));

        return missionConverter.toSearchResponse(missionSlice, myMissionMap);
    }
}
