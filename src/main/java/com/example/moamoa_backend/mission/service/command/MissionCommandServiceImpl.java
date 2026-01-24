package com.example.moamoa_backend.mission.service.command;

import com.example.moamoa_backend.interest.entity.SubInterest;
import com.example.moamoa_backend.interest.repository.SubInterestRepository;
import com.example.moamoa_backend.keyword.entity.Keyword;
import com.example.moamoa_backend.keyword.enums.KeywordType;
import com.example.moamoa_backend.keyword.repository.KeywordRepository;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.entity.mapping.MemberMission;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberMissionRepository;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.mission.converter.MissionConverter;
import com.example.moamoa_backend.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.enums.MissionStatus;
import com.example.moamoa_backend.mission.exception.MissionException;
import com.example.moamoa_backend.mission.exception.code.MissionErrorCode;
import com.example.moamoa_backend.mission.entity.mapping.MissionKeyword;
import com.example.moamoa_backend.mission.entity.mapping.MissionSubInterest;
import com.example.moamoa_backend.mission.repository.MissionKeywordRepository;
import com.example.moamoa_backend.mission.repository.MissionRepository;
import com.example.moamoa_backend.mission.repository.MissionSubInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionCommandServiceImpl implements MissionCommandService {
    private final MissionRepository missionRepository;
    private final KeywordRepository keywordRepository;
    private final MissionKeywordRepository missionKeywordRepository;
    private final SubInterestRepository subInterestRepository;
    private final MissionSubInterestRepository missionSubInterestRepository;
    private final MissionConverter missionConverter;
    private final MemberMissionRepository memberMissionRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public MissionResponseDto.CreateResult createMission(MissionRequestDto.Create request) {
        Mission newMission = missionConverter.toEntity(request);
        missionRepository.save(newMission);


        //키워드 이미 있으면 연결, 없으면 테이블에 생성
        if (request.keywords() != null) {
            request.keywords().forEach(keywordDto -> {
                KeywordType type = (keywordDto.type()!=null)
                        ? KeywordType.valueOf(keywordDto.type()) : KeywordType.KEYWORD;

                Keyword keyword = keywordRepository.findByName(keywordDto.name())
                        .orElseGet(() -> keywordRepository.save(Keyword.builder().name(keywordDto.name()).keywordType(type).build()));

                MissionKeyword missionKeyword = MissionKeyword.builder()
                        .mission(newMission)
                        .keyword(keyword)
                        .build();

                missionKeywordRepository.save(missionKeyword);
            });
        }
        if (request.category() != null) {
            SubInterest subInterest = subInterestRepository.findByName(request.category())
                    .orElseThrow(() -> new MissionException(MissionErrorCode.CATEGORY_NOT_FOUND));

            MissionSubInterest missionSubInterest = MissionSubInterest.builder()
                    .mission(newMission)
                    .subInterest(subInterest)
                    .build();
            missionSubInterestRepository.save(missionSubInterest);
            newMission.getMissionSubInterests().add(missionSubInterest);
        }

        return missionConverter.toCreateResult(newMission);
    }

    @Transactional
    @Override
    public MissionResponseDto.WatchResult updateMissionWatchStatus(Long memberId, Long missionId) {
        Mission mission = missionRepository.findById(missionId).orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        MemberMission memberMission = memberMissionRepository.findByMemberIdAndMissionId(memberId, missionId).orElse(null);

        if (memberMission == null) {
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

            memberMission = missionConverter.toMemberMission(member, mission);
            memberMissionRepository.save(memberMission);
        } else {
            memberMission.changeIsContentWatched(true);
        }

        return missionConverter.toWatchResult(memberMission);
    }

    @Transactional
    @Override
    public MissionResponseDto.StatusResult updateMissionStatus(Long memberId, Long missionId, MissionRequestDto.PatchStatus request) {

        MissionStatus missionStatus;
        try {
            missionStatus = MissionStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new MissionException(MissionErrorCode.INVALID_MISSION_STATUS);
        }

        Mission mission = missionRepository.findById(missionId).orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));
        MemberMission memberMission = memberMissionRepository.findByMemberIdAndMissionId(memberId, missionId).orElse(null);

        //멤버 미션 기록이 없을 때 -> 영상 안 보고 찜만 누른거
        if (memberMission == null) {
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

            memberMission = missionConverter.toMemberMission(member, mission, missionStatus);

            memberMissionRepository.save(memberMission);
        }

        //MemberMission에 데이터(기록)이 이미 있을 때 (재도전, 찜, 포기)
        else {
            //그만두기 FAIL 요청 -> 재도전 리스트로
            if (missionStatus == MissionStatus.FAIL) {

                //시작도 안했는데 포기하는거 불가
                if (memberMission.getAttemptCount() == 0) {
                    throw new MissionException(MissionErrorCode.MISSION_NOT_STARTED);
                }

                //이미 성공한 미션을 FAIL로 바꿀 수 없게 방어 -> SUCCESS인 미션도 문제 풀려고 다시 풀 수 있으니까
                if (memberMission.getMissionStatus() != MissionStatus.SUCCESS) {
                    memberMission.changeMissionStatus(MissionStatus.FAIL);
                }
            }

            //찜하기 SCRAP 요청 -> 찜 리스트로
            else if (missionStatus == MissionStatus.SCRAP) {
                //이미 성공한 미션은 찜 상태로 바꿀 수 없음!
                if (memberMission.getMissionStatus() != MissionStatus.SUCCESS) {
                    memberMission.changeMissionStatus(MissionStatus.SCRAP);
                }
            }

            //도전 시작/ 재도전/ 찜 취소 -> NONE 요청
            else if (missionStatus == MissionStatus.NONE) {

                //상황 1. 찜 취소인 경우
                if (memberMission.getMissionStatus() == MissionStatus.SCRAP) {

                    //이전에 도전했던 기록(FAIL)이 있는지 확인
                    if (memberMission.getAttemptCount() > 0) {

                        //기록이 있으면 FAIL 상태로 복구
                        memberMission.changeMissionStatus(MissionStatus.FAIL);
                    } else {
                        memberMission.changeMissionStatus(MissionStatus.NONE);
                    }
                }
                //상황 2. 도전 시작/ 재도전인 경우 -> 시도횟수 +1
                else {
                    memberMission.addAttemptCount();
                }

            }
        }
        return missionConverter.toStatusResult(memberMission);
    }
}


