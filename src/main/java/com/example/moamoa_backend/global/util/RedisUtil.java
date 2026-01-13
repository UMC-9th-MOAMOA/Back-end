package com.example.moamoa_backend.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    // StringRedisTemplate 사용
    private final StringRedisTemplate stringRedisTemplate;

    // 데이터 저장 (Key, Value, 만료시간 초 단위)
    public void setDataExpire(String key, String value, long durationSeconds) {
        Duration expireDuration = Duration.ofSeconds(durationSeconds);
        stringRedisTemplate.opsForValue().set(key, value, expireDuration);
    }

    // 데이터 조회
    public String getData(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 데이터 삭제
    public void deleteData(String key) {
        stringRedisTemplate.delete(key);
    }

}
