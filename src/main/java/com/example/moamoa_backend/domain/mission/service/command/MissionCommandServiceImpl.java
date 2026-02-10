package com.example.moamoa_backend.domain.mission.service.command;

import com.example.moamoa_backend.domain.interest.entity.SubInterest;
import com.example.moamoa_backend.domain.interest.repository.SubInterestRepository;
import com.example.moamoa_backend.domain.keyword.entity.Keyword;
import com.example.moamoa_backend.domain.keyword.enums.KeywordType;
import com.example.moamoa_backend.domain.keyword.repository.KeywordRepository;
import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.entity.mapping.MemberMission;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.domain.member.repository.MemberMissionRepository;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.mission.converter.MissionConverter;
import com.example.moamoa_backend.domain.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.domain.mission.dto.response.MissionResponseDto;
import com.example.moamoa_backend.domain.mission.entity.Mission;
import com.example.moamoa_backend.domain.mission.enums.MissionStatus;
import com.example.moamoa_backend.domain.mission.exception.MissionException;
import com.example.moamoa_backend.domain.mission.exception.code.MissionErrorCode;
import com.example.moamoa_backend.domain.mission.entity.mapping.MissionKeyword;
import com.example.moamoa_backend.domain.mission.entity.mapping.MissionSubInterest;
import com.example.moamoa_backend.domain.mission.repository.MissionKeywordRepository;
import com.example.moamoa_backend.domain.mission.repository.MissionRepository;
import com.example.moamoa_backend.domain.mission.repository.MissionSubInterestRepository;
import com.example.moamoa_backend.domain.mission.service.util.YoutubeUtilService;
import com.example.moamoa_backend.domain.quiz.entity.Quiz;
import com.example.moamoa_backend.domain.wallet.entity.Wallet;
import com.example.moamoa_backend.domain.wallet.entity.WalletHistory;
import com.example.moamoa_backend.domain.wallet.enums.TransactionType;
import com.example.moamoa_backend.domain.wallet.repository.WalletHistoryRepository;
import com.example.moamoa_backend.domain.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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
    private final WalletHistoryRepository walletHistoryRepository;
    private final WalletRepository walletRepository;
    private final YoutubeUtilService youtubeUtilService;


    /**
     * [관리자용] 미션 생성 메서드
     * 1. 유튜브 API로 영상 길이 조회 및 유효성 검증
     * 2. 미션 및 퀴즈 엔티티 생성 및 저장
     * 3. 키워드 처리 (존재하면 매핑, 없으면 생성 후 매핑)
     * 4. 카테고리(SubInterest) 연결
     *
     * @param request 미션 생성 요청 DTO (제목, URL, 퀴즈 등)
     * @return 생성된 미션 요약 정보 (ID, 총 리워드, 소요시간)
     * @throws MissionException 유효하지 않은 URL, 유튜브 API 오류, 카테고리 없음
     */
    @Transactional
    @Override
    public MissionResponseDto.CreateResult createMission(MissionRequestDto.Create request) {

        int videoDuration = youtubeUtilService.getDurationInSeconds(request.videoUrl());

        Mission newMission = missionConverter.toEntity(request, videoDuration);
        missionRepository.save(newMission);


        //키워드 이미 있으면 연결, 없으면 테이블에 생성
        if (request.keywords() != null && !request.keywords().isEmpty()) {

            List<MissionKeyword> missionKeywords = request.keywords().stream()
                    .map(k -> {
                        KeywordType type;
                        try {
                            type = (k.type() != null)
                                    ? KeywordType.valueOf(k.type())
                                    : KeywordType.KEYWORD;
                        } catch (IllegalArgumentException e) {
                            throw new MissionException(MissionErrorCode.INVALID_KEYWORD_TYPE);
                        }

                        Keyword keyword = keywordRepository.findByNameAndKeywordType(k.name(), type)
                                .orElseGet(() -> keywordRepository.save(
                                        Keyword.builder()
                                                .name(k.name())
                                                .keywordType(type)
                                                .build()
                                ));

                        return MissionKeyword.builder()
                                .mission(newMission)
                                .keyword(keyword)
                                .build();
                    })
                    .toList();

            missionKeywordRepository.saveAll(missionKeywords);
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


    /**
     * 미션 영상 시청 완료 처리
     * - 미션 도전을 위한 필수 선행 조건 (시청 완료 -> true)
     */
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
            if(!memberMission.isContentWatched()){
            memberMission.changeIsContentWatched(true);
            }
        }

        return missionConverter.toWatchResult(memberMission);
    }

    /**
     * 미션 상태 변경 (도전, 찜, 포기)
     * - 복잡한 상태 전이 로직을 담당
     * - SUCCESS 상태로는 변경 불가 (정답 제출로만 가능)
     */
    @Transactional
    @Override
    public MissionResponseDto.StatusResult updateMissionStatus(Long memberId, Long missionId, MissionRequestDto.PatchStatus request) {

        //오타 체크
        MissionStatus missionStatus;
        try {
            missionStatus = MissionStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new MissionException(MissionErrorCode.INVALID_MISSION_STATUS_PARAM);
        }

        //SUCCESS로 변경 시도 차단
        if(missionStatus == MissionStatus.SUCCESS){
            throw new MissionException(MissionErrorCode.INVALID_MISSION_STATUS);
        }

        Mission mission = missionRepository.findById(missionId).orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));
        MemberMission memberMission = memberMissionRepository.findByMemberIdAndMissionId(memberId, missionId).orElse(null);

        if(missionStatus == MissionStatus.NONE){
            boolean isScrapCancel = (memberMission !=null && memberMission.getMissionStatus() == MissionStatus.SCRAP);
            if(!isScrapCancel){
                if(memberMission==null || !memberMission.isContentWatched()){
                throw new MissionException(MissionErrorCode.MISSION_VIDEO_NOT_WATCHED);
                }
            }
        }
        //멤버 미션 기록이 없을 때 -> 영상 안 보고 찜만 누른거
        if (memberMission == null) {
            if(missionStatus==MissionStatus.FAIL){
                throw new MissionException(MissionErrorCode.MISSION_NOT_STARTED);
            }
            return createInitialMemberMission(memberId,mission,missionStatus);
            }

        //MemberMission에 데이터(기록)이 이미 있을 때 (재도전, 찜, 포기)
        switch (missionStatus) {
            case FAIL -> handleFailRequest(memberMission);
            case SCRAP -> handleScrapRequest(memberMission);
            case NONE -> handleNoneRequest(memberMission);
        }
        return missionConverter.toStatusResult(memberMission);
    }


    /**
     * 미션 정답 제출 및 채점
     *
     * 1. **스마트 채점**:
     * - 단답형 정답은 **대소문자를 무시**합니다. (Clock == clock)
     * - **콤마(,)로 구분된 동의어** 중 하나만 맞으면 정답 처리합니다. (ex: "상속,상속성")
     *
     * 2. **보상 지급 정책**:
     * - **최초 시도(`isFirstAttempt`)**이면서 정답일 경우에만 미션 보상과 경험치를 지급합니다.
     * - 재시도 성공 시에는 성공 상태(`SUCCESS`)로만 변경되고 보상은 0입니다.
     *
     * 3. **목표 달성 체크**:
     * - 정답 제출 시 일간/주간 목표 달성 여부를 확인하고 추가 보상을 지급합니다.
     *
     * @param memberId 유저 ID
     * @param missionId 미션 ID
     * @param request 제출한 답안 리스트
     * @return 채점 결과 (성공 여부, 획득 보상, 목표 달성 현황 등)
     */
    @Transactional
    @Override
    public MissionResponseDto.SubmitResult submitMissionAnswer(Long memberId, Long missionId, MissionRequestDto.SubmitAnswer request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));
        Wallet wallet = walletRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<Quiz> quizzes = mission.getQuizzes();

        if(quizzes.size() != request.submissions().size()){
            throw new MissionException(MissionErrorCode.INVALID_QUIZ_ANSWER);
        }

        int earnedScore = 0;
        boolean isAllCorrect = true;

        for(Quiz quiz : quizzes){
            String userAnswer = request.submissions().stream()
                    .filter(s -> s.quizId().equals(quiz.getId()))
                    .findFirst()
                    .map(MissionRequestDto.QuizSubmission::answer)
                    .orElseThrow(() -> new MissionException(MissionErrorCode.INVALID_QUIZ_ANSWER));

            boolean isCorrect = false;
            String dbAnswer = quiz.getAnswer();

            if (dbAnswer == null || dbAnswer.isBlank()) {
                throw new MissionException(MissionErrorCode.QUIZ_ANSWER_NOT_FOUND);
            }

            String[] correctAnswers = dbAnswer.split(",");

            for (String correct : correctAnswers) {
                if (userAnswer.replaceAll("\\s+", "").equalsIgnoreCase(correct.replaceAll("\\s+", ""))) {
                    isCorrect = true;
                    break;
                }
            }

            if(isCorrect){
                earnedScore += missionConverter.getRewardByType(quiz.getType());
            } else {
                isAllCorrect = false;
            }
        }

        //기록 조회 및 첫 시도 판별
        MemberMission memberMission = memberMissionRepository.findByMemberIdAndMissionId(member.getId(), mission.getId())
                .orElse(null);

        //첫 시도 여부 판별
        //기록이 아예 없거나, 기록은 있는데 (NONE 상태 && 시도횟수 1)
        boolean isFirstAttempt = (memberMission == null) || (memberMission.getMissionStatus() == MissionStatus.NONE && memberMission.getAttemptCount() <= 1);

        //기록 없으면 생성
        if (memberMission == null) {
            memberMission = MemberMission.builder()
                    .member(member)
                    .mission(mission)
                    .missionStatus(MissionStatus.NONE)
                    .attemptCount(1)
                    .build();
            memberMissionRepository.save(memberMission);
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate tomorrow = today.plusDays(1);
        LocalDate thisMonday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        //case 1 -> 실패 (일부정답 or 모두 오답)
        if(!isAllCorrect){
            memberMission.changeMissionStatus(MissionStatus.FAIL);

            //첫 시도라면 실패했더라도 MISSION 타입으로 월렛히스토리에 기록
            if(isFirstAttempt){
                if(earnedScore>0){
                    memberMission.recordRewardAt();
                }
                String desc = (earnedScore > 0) ? "참여(부분점수)" : "참여(0점)";

                updateWalletAndSaveHistory(wallet,mission,TransactionType.MISSION,earnedScore,mission.getTitle() + desc);
                walletHistoryRepository.flush();
            }

            long currentDailyCount = countMissionCompleteBetween(memberId, today, tomorrow);
            long currentWeeklyCount = countMissionCompleteBetween(memberId, thisMonday, tomorrow);

            return missionConverter.toSubmitResult(
                    false,
                    earnedScore,
                    0,
                    (int) currentDailyCount,
                    (member.getDailyGoal() != null && currentDailyCount >= member.getDailyGoal()),
                    (int) currentWeeklyCount,
                    (member.getWeeklyGoal() != null && currentWeeklyCount >= member.getWeeklyGoal())
            );
        }

        //case 2 -> 성공 (모두 정답)
        int finalMissionReward = 0;

        if(isFirstAttempt){
            memberMission.markAsSuccess();
            finalMissionReward = earnedScore;
            updateWalletAndSaveHistory(wallet,mission,TransactionType.MISSION_COMPLETE,finalMissionReward,mission.getTitle() + "성공");
            walletHistoryRepository.flush();
        }
        else{
            memberMission.changeMissionStatus(MissionStatus.SUCCESS);
        }

        // --- 목표 달성 체크 ---
        int earnedGoalReward = 0;

        long dailyCount = countMissionCompleteBetween(memberId,today,tomorrow);
        long weeklyCount = countMissionCompleteBetween(memberId, thisMonday, tomorrow);

        if (member.getDailyGoal() != null && dailyCount == member.getDailyGoal()) {
            int reward = member.getDailyGoal() * 5;
            earnedGoalReward += reward;
            updateWalletAndSaveHistory(wallet, null, TransactionType.DAILY_REWARD, reward, "일간 목표 달성");
        }

        if (member.getWeeklyGoal() != null && weeklyCount == member.getWeeklyGoal()) {
            int reward = member.getWeeklyGoal() * 10;
            earnedGoalReward += reward;
            updateWalletAndSaveHistory(wallet, null, TransactionType.WEEKLY_REWARD, reward, "주간 목표 달성");
        }

        if (earnedGoalReward > 0) walletHistoryRepository.flush();

        return missionConverter.toSubmitResult(
                true, finalMissionReward, earnedGoalReward,
                (int) dailyCount, (member.getDailyGoal() != null && dailyCount >= member.getDailyGoal()),
                (int) weeklyCount, (member.getWeeklyGoal() != null && weeklyCount >= member.getWeeklyGoal())
        );

    }

    // [신규 생성] 기록이 아예 없을 때
    private MissionResponseDto.StatusResult createInitialMemberMission(Long memberId, Mission mission, MissionStatus status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        MemberMission newMission = missionConverter.toMemberMission(member, mission, status);
        memberMissionRepository.save(newMission);

        return missionConverter.toStatusResult(newMission);
    }

    // [포기 요청] FAIL 로직
    private void handleFailRequest(MemberMission memberMission) {
        // 시작도 안 했는데(0회) 포기할 순 없음
        if (memberMission.getAttemptCount() == 0) {
            throw new MissionException(MissionErrorCode.MISSION_NOT_STARTED);
        }

        // 성공한 미션은 FAIL로 상태 변경 불가
        if (memberMission.getMissionStatus() != MissionStatus.SUCCESS) {
            memberMission.changeMissionStatus(MissionStatus.FAIL);
        }
    }

    // [찜 요청] SCRAP 로직
    private void handleScrapRequest(MemberMission memberMission) {
        // 성공한 미션은 SCRAP으로 상태 변경 불가
        if (memberMission.getMissionStatus() != MissionStatus.SUCCESS) {
            memberMission.changeMissionStatus(MissionStatus.SCRAP);
        }
    }

    // [도전/재도전/찜취소 요청] NONE 로직
    private void handleNoneRequest(MemberMission memberMission) {
        // 상황 A: 찜 취소인 경우 (SCRAP -> NONE/FAIL)
        if (memberMission.getMissionStatus() == MissionStatus.SCRAP) {
            if (memberMission.getAttemptCount() > 0) {
                memberMission.changeMissionStatus(MissionStatus.FAIL);
            } else {
                memberMission.changeMissionStatus(MissionStatus.NONE);
            }
        }
        // 상황 B: 재도전 (그 외 상태 -> 시도횟수만 증가)
        else {
            // 성공한 미션이어도 시도 횟수는 증가시킴
            memberMission.addAttemptCount();
        }
    }

    //카운트 조회
    private Long countMissionCompleteBetween(Long memberId, LocalDate startDate, LocalDate endDateExclusive){
        return walletHistoryRepository.countByMemberAndTypeBetween(
                memberId,
                TransactionType.MISSION_COMPLETE,
                startDate.atStartOfDay(),
                endDateExclusive.atStartOfDay()
        );
    }


    //히스토리 저장
    private void updateWalletAndSaveHistory(Wallet wallet, Mission mission, TransactionType type, int amount, String description) {

        if (amount > 0) {
            wallet.addPoint(amount);
        }

        WalletHistory history = WalletHistory.create(
                wallet,
                mission,
                null,
                description,
                amount,
                wallet.getPoint(),
                type
        );

        walletHistoryRepository.save(history);
    }
}


