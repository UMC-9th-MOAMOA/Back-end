package com.example.moamoa_backend.global.util;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ObjectRedisUtil {

	private final RedisTemplate<String, Object> objectRedisTemplate;

	public Object get(String key) {
		return objectRedisTemplate.opsForValue().get(key);
	}

	public void delete(String key) {
		objectRedisTemplate.delete(key);
	}

	/**
	 * SETNX + TTL
	 * key가 없을 때만 저장하고 TTL을 건다.
	 * @return true(저장 성공), false(이미 존재)
	 */
	public boolean setIfAbsentExpire(String key, Object value, long ttlSeconds) {
		Boolean ok = objectRedisTemplate.opsForValue()
			.setIfAbsent(key, value, Duration.ofSeconds(ttlSeconds));
		return Boolean.TRUE.equals(ok);
	}

}
