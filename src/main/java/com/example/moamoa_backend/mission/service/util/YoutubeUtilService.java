package com.example.moamoa_backend.mission.service.util;

import com.example.moamoa_backend.mission.exception.MissionException;
import com.example.moamoa_backend.mission.exception.code.MissionErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeUtilService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    public int getDurationInSeconds(String videoUrl) {
        try {
            String videoId = extractVideoId(videoUrl);
            if (videoId == null) {
                throw new MissionException(MissionErrorCode.INVALID_YOUTUBE_URL);
            }

            String apiUrl = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id=" + videoId + "&key=" + apiKey;

            YouTubeResponse response = restClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .body(YouTubeResponse.class);

            if (response == null || response.items() == null || response.items().isEmpty()) {
                throw new MissionException(MissionErrorCode.YOUTUBE_VIDEO_NOT_FOUND);

                }

            String isoDuration = response.items().get(0).contentDetails().duration();
            log.info(" 유튜브 API 호출 성공 ID: {}, 시간: {}", videoId, isoDuration);
            return (int) Duration.parse(isoDuration).getSeconds();

        } catch (MissionException e) {
            throw e;
        } catch (Exception e) {
            log.error("YouTube API 호출 중 예외 발생: {}", e.getMessage(), e);
            throw new MissionException(MissionErrorCode.YOUTUBE_SERVER_ERROR);
        }
    }
    private String extractVideoId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    private record YouTubeResponse(List<YouTubeItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record YouTubeItem(ContentDetails contentDetails) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ContentDetails(String duration) {
    }
}
