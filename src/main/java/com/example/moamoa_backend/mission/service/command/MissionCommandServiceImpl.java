package com.example.moamoa_backend.mission.service.command;

import com.example.moamoa_backend.interest.entity.SubInterest;
import com.example.moamoa_backend.interest.repository.SubInterestRepository;
import com.example.moamoa_backend.keyword.entity.Keyword;
import com.example.moamoa_backend.keyword.repository.KeywordRepository;
import com.example.moamoa_backend.mission.converter.MissionConverter;
import com.example.moamoa_backend.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.exception.MissionException;
import com.example.moamoa_backend.mission.exception.code.MissionErrorCode;
import com.example.moamoa_backend.mission.mapping.MissionKeyword;
import com.example.moamoa_backend.mission.mapping.MissionSubInterest;
import com.example.moamoa_backend.mission.repository.MissionKeywordRepository;
import com.example.moamoa_backend.mission.repository.MissionRepository;
import com.example.moamoa_backend.mission.repository.MissionSubInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionCommandServiceImpl implements MissionCommandService{
    private final MissionRepository missionRepository;
    private final KeywordRepository keywordRepository;
    private final MissionKeywordRepository missionKeywordRepository;
    private final SubInterestRepository subInterestRepository;
    private final MissionSubInterestRepository missionSubInterestRepository;
    private final MissionConverter  missionConverter;

    @Transactional
    @Override
    public MissionResponseDto.CreateResult createMission(MissionRequestDto.Create request){
        Mission newMission = missionConverter.toEntity(request);
        missionRepository.save(newMission);


        //키워드 이미 있으면 연결, 없으면 테이블에 생성
        if(request.keywords()!=null){
            request.keywords().forEach(keywordName ->{
                Keyword keyword = keywordRepository.findByName(keywordName)
                        .orElseGet(()-> keywordRepository.save(Keyword.builder().name(keywordName).build()))
                        ;

                MissionKeyword missionKeyword = MissionKeyword.builder()
                        .mission(newMission)
                        .keyword(keyword)
                        .build();

                missionKeywordRepository.save(missionKeyword);
            });
        }
        if(request.category()!=null){
            SubInterest subInterest = subInterestRepository.findByName(request.category())
                    .orElseThrow(()-> new MissionException(MissionErrorCode.CATEGORY_NOT_FOUND));

            MissionSubInterest missionSubInterest = MissionSubInterest.builder()
                    .mission(newMission)
                    .subInterest(subInterest)
                    .build();
            missionSubInterestRepository.save(missionSubInterest);
        }

        return missionConverter.toCreateResult(newMission);
    }
}
