package com.example.moamoa_backend.global.security.jwt;

import com.example.moamoa_backend.global.security.jwt.exception.JwtException;
import com.example.moamoa_backend.global.security.jwt.exception.code.JwtErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT(Json Web Token) 생성, 검증, 정보 추출을 담당하는 유틸리티 클래스
 */
@Slf4j
@Getter
@Component
public class JwtUtil {

    private final SecretKey secretKey;       // JWT 서명에 사용할 암호화 키
    private final long accessTokenValidity;  // Access Token 유효 시간 (밀리초)
    private final long refreshTokenValidity; // Refresh Token 유효 시간 (밀리초)

    /**
     * application.yml에서 설정 정보를 주입받아 초기화
     */
    public JwtUtil(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity
    ) {
        // 비밀키 최소길이 검증
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.error("JWT Secret Key length error: required >= 32 bytes, actual = {}", keyBytes.length);
            throw new JwtException(JwtErrorCode.SECRET_KEY_INVALID);
        }
        // 비밀키를 HMAC-SHA 알고리즘에 맞게 변환
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    /**
     * Access Token 생성
     *
     * @param userId 사용자 ID (PK)
     * @param role   사용자 권한 (예: ROLE_USER)
     * @return 생성된 JWT Access Token 문자열
     */
    public String createAccessToken(Long userId, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .subject(userId.toString())       // 토큰 제목(Subject)에 userId 저장
                .claim("role", role)              // 사용자 권한 정보 저장
                .claim("type", "access_token")    // 토큰 타입 명시 (access)
                .issuedAt(now)                    // 토큰 발급 시간
                .expiration(validity)             // 토큰 만료 시간
                .signWith(secretKey)              // 비밀키로 서명 (HS256 알고리즘)
                .compact();
    }

    /**
     * Refresh Token 생성
     *
     * @param userId 사용자 ID (PK)
     * @return 생성된 JWT Refresh Token 문자열
     */
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidity);

        return Jwts.builder()
                .subject(userId.toString())       // userId만 저장 (권한 정보 등은 제외)
                .claim("type", "refresh_token")   // 토큰 타입 명시 (refresh)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 Claims(정보 payload) 추출
     * - 만료된 토큰이더라도 정보를 꺼낼 수 있도록 처리 (예: 재발급 시 필요할 수 있음)
     *
     * @param token JWT 토큰
     * @return 추출된 Claims 객체
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이어도 Claims 반환
            return e.getClaims();
        }
    }

    /**
     * 토큰 유효성 검증
     * - 서명, 만료, 형식 등을 검사하고 문제 발생 시 예외(JwtException)를 던짐
     *
     * @param token 검증할 JWT 토큰
     */
    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new JwtException(JwtErrorCode.TOKEN_MALFORMED);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new JwtException(JwtErrorCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new JwtException(JwtErrorCode.TOKEN_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new JwtException(JwtErrorCode.TOKEN_EMPTY);
        }
    }

    /**
     * 토큰 타입 검증 (Access vs Refresh)
     * - 토큰 내 "type" 클레임이 기대하는 타입과 일치하는지 확인
     *
     * @param token        JWT 토큰
     * @param expectedType 기대하는 토큰 타입 (access_token 등)
     */
    public void validateTokenType(String token, String expectedType) {
        String type = getType(token);

        if (!expectedType.equals(type)) {
            log.error("토큰 타입 불일치: Expected={}, Actual={}", expectedType, type);
            throw new JwtException(JwtErrorCode.TOKEN_INVALID_TYPE);
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getMemberId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰에서 역할(Role) 추출
     */
    public String getRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰에서 카테고리(타입) 추출
     */
    public String getType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("type", String.class);
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @return true: 만료됨, false: 유효함
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 토큰 정보를 기반으로 Spring Security 인증 객체(Authentication) 생성
     * - DB를 거치지 않고 토큰 정보만으로 인증 객체를 만듦
     *
     * @param token 검증된 JWT Access Token
     * @return Authentication (UsernamePasswordAuthenticationToken)
     */
    public Authentication getAuthentication(String token) {
        Long memberId = getMemberId(token);
        String role = getRole(token);

        // 권한 목록 생성
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
        );

        // Principal 생성 (User 객체 사용, 비밀번호는 빈 문자열 처리)
        UserDetails principal = new User(memberId.toString(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}