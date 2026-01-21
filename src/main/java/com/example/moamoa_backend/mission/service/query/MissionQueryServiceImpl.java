package com.example.moamoa_backend.mission.service.query;

import com.example.moamoa_backend.keyword.entity.Keyword;
import com.example.moamoa_backend.keyword.repository.KeywordRepository;
import com.example.moamoa_backend.member.entity.mapping.MemberMission;
import com.example.moamoa_backend.member.repository.MemberMissionRepository;
import com.example.moamoa_backend.mission.converter.MissionConverter;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.exception.MissionException;
import com.example.moamoa_backend.mission.exception.code.MissionErrorCode;
import com.example.moamoa_backend.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionQueryServiceImpl implements MissionQueryService{

    private final MissionConverter missionConverter;
    private final MissionRepository missionRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final KeywordRepository keywordRepository;

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

}
