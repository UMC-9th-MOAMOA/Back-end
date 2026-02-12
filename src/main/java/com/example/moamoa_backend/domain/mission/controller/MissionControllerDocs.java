package com.example.moamoa_backend.domain.mission.controller;

import com.example.moamoa_backend.global.apiPayload.response.ApiResponse;
import com.example.moamoa_backend.domain.mission.dto.request.MissionRequestDto;
import com.example.moamoa_backend.domain.mission.dto.response.MissionResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Mission API", description = "미션 관련 API")
public interface MissionControllerDocs {

	@Operation(
		summary = "미션 등록 API (관리자용)",
		description = """
			관리자가 새로운 미션을 등록하는 API입니다.
			
			**[기능 상세]**
			- **영상 길이 자동 계산**: 입력된 유튜브 URL을 분석해 영상 시간(초)과 소요 시간(분)을 서버가 자동으로 저장합니다.
			- **보상 자동 계산**: 등록된 퀴즈의 타입별 배점에 따라 총 보상(도토리)이 자동으로 합산됩니다.
			
			**[키워드 타입 가이드]**
			- **SITUATION**: 추천상황 
			- **SKILL**: 획득스킬 
			- **KEYWORD**: 일반 키워드 
			
			**[주의사항]**
			- **카테고리**: 반드시 '카테고리 코드표'의 소분류 한글명을 정확히 입력해야 매핑됩니다. (ex: "경제의 흐름")
			- **퀴즈 옵션**: OX는 **["O", "X"]**, 단답형은 **[]** 빈 배열을 준수해주세요.
			"""
	)
	ApiResponse<MissionResponseDto.CreateResult> createMission(
		@RequestBody @Valid MissionRequestDto.Create request
	);

    @Operation(
            summary = "미션 상세 조회 API",
            description = """
          특정 미션의 상세 정보와 퀴즈 데이터를 조회합니다.
          
          **[프론트엔드 처리 가이드]**
          
          **1. 실시간 채점 로직 (O/X 피드백)**
          - **단답형**: 유저 입력값이 acceptedAnswers 리스트에 포함되는지 확인 (대소문자 무시, 공백 제거 필수)
          - **객관식/OX**: 유저 선택값이 acceptedAnswers의 첫 번째 값과 일치하는지 확인
          
          **2. 재시도 로직 (UI 복구)**
          - **previousCorrectAnswer 값이 있음**: 해당 값으로 UI를 **정답(체크/입력) 상태**로 렌더링
          - **previousCorrectAnswer 값이 없음(null)**: 틀렸거나 처음 푸는 문제입니다. **초기 상태**로 렌더링
          
          **[Response 필드 설명]**
          - **isContentWatched**: 영상 시청 완료 여부 (true여야 도전 가능)
          - **attemptCount**: 현재까지의 시도 횟수
          """
    )
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MISSION404_1: 해당 미션을 찾을 수 없음")
	})
	ApiResponse<MissionResponseDto.MissionDetail> getMissionDetail(
		@PathVariable Long missionId,
		@Parameter(hidden = true) UserDetails userDetails
	);

	@Operation(
		summary = "미션 영상 시청 완료 API",
		description = """
			영상을 끝까지 시청했을 때 호출합니다.
			
			**[중요]**
			- 이 API를 호출하여 **시청 완료(isContentWatched=true)** 상태가 되어야만, **PATCH /missions/{id}/status (도전하기)** API를 호출할 수 있습니다.
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시청 완료 처리 성공 (이제 도전 가능)"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "미션 정보를 찾을 수 없음")
	})
	ApiResponse<MissionResponseDto.WatchResult> updateWatchStatus(
		@PathVariable Long missionId,
		@Parameter(hidden = true) UserDetails userDetails
	);

	@Operation(
		summary = "미션 상태 변경 API (도전하기/찜/포기)",
		description = """
			미션의 상태를 변경합니다. 유저의 현재 상태와 요청 값에 따라 동작이 달라집니다.
			
			| 요청 상태 (status) | 동작 설명 | 비고 |
			| :--- | :--- | :--- |
			| **NONE** | **도전하기 / 재도전 / 찜 취소** | 기본적으로 시도 횟수가 1 증가합니다. <br> 단, **찜(SCRAP)** 상태에서 취소하는 경우엔 횟수가 늘지 않습니다. |
			| **SCRAP** | **찜하기** | 미션을 찜 보관함으로 이동시킵니다. (성공한 미션은 불가) |
			| **FAIL** | **포기하기** | 미션을 포기하고 재도전 리스트로 보냅니다. (시작 전에는 불가) |
			
			**[주의사항]**
			- **SUCCESS(성공)** 상태로는 이 API로 변경할 수 없습니다. (정답 제출 API 사용)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패 이유:\n" +
			"- **MISSION400_2**: 잘못된 파라미터\n" +
			"- **MISSION400_3**: 시작 전 포기 시도\n" +
			"- **MISSION400_4**: 유효하지 않은 상태값 파라미터\n" +
			"- **MISSION400_7**: 영상 시청하지 않고 도전 시도"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "미션 정보를 찾을 수 없음")
	})
	ApiResponse<MissionResponseDto.StatusResult> updateStatus(
		@PathVariable Long missionId,
		@Valid @RequestBody MissionRequestDto.PatchStatus request,
		@Parameter(hidden = true) UserDetails userDetails
	);

	@Operation(
		summary = "추천 키워드(태그) 목록 조회 API",
		description = """
			탐색 화면 하단이나 필터링에 사용할 **추천 키워드 전체 목록**을 조회합니다.
			
			**[반환 데이터]**
			- 키워드 ID, 이름(태그명), 타입(SITUATION, SKILL 등)을 반환합니다.
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
	})
	ApiResponse<MissionResponseDto.KeywordListResult> getRecommendKeyword();

	@Operation(
		summary = "연관 검색어(자동완성) 조회 API",
		description = """
			사용자가 검색창에 입력 중인 단어를 포함하는 키워드를 **최대 5개** 추천합니다.
			
			**[로직 설명]**
			- 입력된 단어가 포함된(**Like %keyword%**) 키워드를 조회합니다.
			- 공백이나 빈 문자열 입력 시 빈 리스트를 반환합니다.
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (결과 없으면 빈 리스트)"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터")
	})
	ApiResponse<MissionResponseDto.KeywordListResult> getRelatedKeywords(
		@Parameter(description = "검색어 (예: '주식')", example = "주식")
		@RequestParam String keyword
	);

    @Operation(
            summary = "오늘의 추천 미션 리스트 조회 API",
            description = """
           유저의 관심사와 자투리 시간(**time**)을 반영하여 맞춤형 미션 5개를 추천합니다.
           
           **[정렬 로직]**
           1. **기본 진입 (isRefresh=false)**:
              - **1순위**: 유저가 **찜(SCRAP)** 해둔 미션 (최상단 노출)
              - **2순위**: 관심사 & 시간 조건에 맞는 미션
              
           2. **새로고침 (isRefresh=true)**:
              - **찜 우선순위 해제**: 찜 여부와 상관없이 **전체 목록을 무작위로 섞어서(Shuffle)** 반환합니다.
              - (기존의 필터링 조건은 그대로 유지됩니다)
           
           **[Response]**
           - **isScrapped**: 해당 미션을 유저가 찜했는지 여부
           """
    )
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER404_1: 사용자를 찾을 수 없음")
	})
    ApiResponse<List<MissionResponseDto.RecommendResult>> getTodayRecommendMissions(
            @Parameter(description = "여유 시간(분). 이 시간보다 짧거나 같은 미션만 추천됩니다. (입력 안 하면 전체)", example = "10")
            @RequestParam(required = false) Integer time,

            @Parameter(description = "새로고침 여부 (true면 랜덤 섞기, false면 찜 우선)", example = "false")
            @RequestParam(required = false, defaultValue = "false") Boolean isRefresh,

            @Parameter(hidden = true) UserDetails userDetails
    );

	@Operation(
		summary = "미션 검색 API (키워드/검색어)",
		description = """
			사용자가 입력한 검색어(**searchText**) 또는 선택한 키워드(**keywords**)로 미션을 검색합니다.
			
			**[검색 로직]**
			- **검색어 + 키워드**: 두 조건이 모두 있는 경우 **AND** 조건으로 검색합니다. (둘 다 만족하는 미션만 노출)
			- **랜덤 정렬**: **seed** 값을 기준으로 결과를 무작위로 섞습니다.
			
			**[페이징 가이드 (중요)]**
			- 무한 스크롤 구현 시, **첫 페이지 요청 때 생성한 seed 값을 다음 페이지 요청에도 동일하게 보내주세요.**
			- 시드가 바뀌면 페이지 넘길 때 중복된 미션이 나올 수 있습니다.
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공 (결과 없으면 빈 리스트)"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터 요청")
	})
	ApiResponse<MissionResponseDto.SearchResponse> searchMissions(
		@Parameter(hidden = true) UserDetails userDetails,

		@Parameter(description = "제목 검색어 (포함 일치)", example = "주식")
		@RequestParam(required = false) String searchText,

		@Parameter(description = "키워드 리스트", example = "[\"초보\", \"투자\"]")
		@RequestParam(required = false) List<String> keywords,

		@Parameter(description = "랜덤 셔플용 시드값 (프론트 고정 전송)", example = "98765")
		@RequestParam(required = false) Long seed,

		Pageable pageable
	);

	@Operation(
		summary = "카테고리별 미션 조회 API",
		description = """
			대분류(**categoryId**) 또는 소분류(**subCategoryId**)를 선택하여 미션을 조회합니다.
			
			**[파라미터 설명]**
			- **categoryId**: 대분류 ID (예: 경제, IT) - 이것만 보내면 해당 대분류 하위의 모든 미션 조회
			- **subCategoryId**: 소분류 ID (예: 주식, 코딩) - 특정 소분류만 콕 집어서 조회할 때 사용
			
			**[랜덤 정렬 가이드 (seed)]**
			- 이 API는 결과를 무작위로 섞어서 보여줍니다.
			- **페이징(무한 스크롤) 시 중복을 방지하기 위해, 프론트엔드에서 고정된 seed 값을 보내주세요.**
			- (페이지 진입 시 생성한 시드값을 2페이지 요청 때도 동일하게 전송)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터 요청")
	})
	ApiResponse<MissionResponseDto.SearchResponse> getMissionsByCategory(
		@Parameter(hidden = true) UserDetails userDetails,
		@RequestParam(required = false) Long categoryId,
		@RequestParam(required = false) Long subCategoryId,
		@RequestParam(required = false) Long seed,
		Pageable pageable
	);

	@Operation(
		summary = "내 미션 보관함 조회 API (찜/완료/다시풀기)",
		description = """
			내 미션 상태에 따라 목록을 조회합니다 (무한 스크롤 지원).
			
			**파라미터 설명:**
			- **status** (필수):
			    - **SCRAP**: 찜한 미션
			    - **COMPLETE**: 성공(SUCCESS)한 미션
			    - **RETRY**: 실패(FAIL)했거나, 풀다가 중단한(NONE + 시도횟수>0) 미션
			- **condition** (선택, 기본값 LATEST):
			    - **LATEST**: 최근 활동(저장/완료/시도) 순
			    - **TIME_ASC**: 소요시간 짧은 순
			    - **TIME_DESC**: 소요시간 긴 순
			- **categoryId** (선택): 대분류 ID로 필터링
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터 요청")
	})
	ApiResponse<MissionResponseDto.SearchResponse> getMyMissions(
		@Parameter(hidden = true) UserDetails userDetails,
		@RequestParam String status,
		@RequestParam(required = false) String condition,
		@RequestParam(required = false) Long categoryId,
		Pageable pageable
	);

	@Operation(summary = "미션 정답 제출 및 채점 API",
		description = """
			**[기능 설명]**
			유저의 답안을 최종 제출하여 서버에서 검증하고, 결과에 따라 보상을 지급합니다.
			
			**[서버 채점 로직]**
			프론트엔드 가이드와 동일하게 **대소문자 무시, 모든 공백 제거, 동의어 허용**을 적용하여 유연하게 채점합니다.
			
			**[보상 지급 정책]**
			1. **첫 시도 & 정답** (**isFirstAttempt: true**)
			   - **isSuccess: true**
			   - **미션 보상(도토리) 지급**
			   - **일간/주간 목표 카운트 인정** (목표 달성 시 추가 보상 지급)
			
			2. **첫 시도 & 오답**
			   - **isSuccess: false**
			   - 보상 없음
			
			3. **재시도 & 정답** (이미 성공했거나, 한 번 실패한 후 재성공)
			   - **isSuccess: true**
			   - **보상 없음 (missionReward: 0)**
			   - **목표 카운트 제외** (중복 방지)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "채점 성공 (결과는 result.isSuccess 확인)"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "MISSION400_8: 답안 개수가 맞지 않거나 퀴즈 ID가 잘못됨"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MISSION404_1: 해당 미션을 찾을 수 없음")
	})
	ApiResponse<MissionResponseDto.SubmitResult> submitMission(
		@Parameter(hidden = true) UserDetails userDetails,
		@PathVariable Long missionId,
		@RequestBody @Valid MissionRequestDto.SubmitAnswer request
	);
}