package com.example.moamoa_backend.domain.mission.service.util;

import com.example.moamoa_backend.domain.mission.exception.MissionException;
import com.example.moamoa_backend.domain.mission.exception.code.MissionErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * YouTube Data API(v3)를 호출하여 영상 길이(초)를 조회하는 유틸 서비스.
 *
 * 예외 정책
 * - URL 파싱 실패: INVALID_YOUTUBE_URL
 * - 영상 조회 실패: YOUTUBE_VIDEO_NOT_FOUND
 * - 외부 호출/파싱 실패: YOUTUBE_SERVER_ERROR
 */
public class YoutubeUtilService {

	@Value("${youtube.api.key}")
	private String apiKey;

	private RestClient restClient;

	@PostConstruct
	/**
	 * RestClient를 초기화하고 타임아웃을 설정한다.
	 */
	private void init() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5000); // 연결 5초 대기
		factory.setReadTimeout(10000);   // 데이터 읽기 10초 대기

		this.restClient = RestClient.builder()
			.requestFactory(factory)
			.build();
	}

	/**
	 * 유튜브 영상 URL을 받아 영상 길이를 초 단위로 반환한다.
	 *
	 * @param videoUrl 유튜브 영상 URL
	 * @return 영상 길이(초)
	 * @throws MissionException 유효하지 않은 URL/영상 없음/서버 오류
	 */
	public int getDurationInSeconds(String videoUrl) {
		try {
			String videoId = extractVideoId(videoUrl);
			if (videoId == null) {
				throw new MissionException(MissionErrorCode.INVALID_YOUTUBE_URL);
			}

			String apiUrl =
				"https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id=" + videoId + "&key=" + apiKey;

			YouTubeResponse response = restClient.get()
				.uri(apiUrl)
				.retrieve()
				.body(YouTubeResponse.class);

			if (response == null || response.items() == null || response.items().isEmpty()) {
				throw new MissionException(MissionErrorCode.YOUTUBE_VIDEO_NOT_FOUND);

			}

			String isoDuration = response.items().get(0).contentDetails().duration();
			log.info(" 유튜브 API 호출 성공 ID: {}, 시간: {}", videoId, isoDuration);
			return (int)Duration.parse(isoDuration).getSeconds();

		} catch (MissionException e) {
			throw e;
		} catch (Exception e) {
			log.error("YouTube API 호출 중 예외 발생: {}", e.getMessage(), e);
			throw new MissionException(MissionErrorCode.YOUTUBE_SERVER_ERROR);
		}
	}

	/**
	 * 유튜브 URL에서 videoId를 추출한다.
	 *
	 * @param url 유튜브 URL
	 * @return 추출된 videoId (추출 실패 시 null)
	 */
	private String extractVideoId(String url) {
		String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(url);
		return matcher.find() ? matcher.group() : null;
	}

	private record YouTubeResponse(List<YouTubeItem> items) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record YouTubeItem(ContentDetails contentDetails) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ContentDetails(String duration) {
	}
}
