package com.example.moamoa_backend.global.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkUtil {

    private NetworkUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출합니다.
     * 프록시, 로드밸런서 환경을 고려하여 여러 헤더를 순차적으로 확인합니다.
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest is null");
            return "0.0.0.0";
        }

        // 1. X-Forwarded-For 체크 (가장 우선)
        String ip = extractIpFromHeader(request, "X-Forwarded-For");

        // 2. Proxy-Client-IP (Apache)
        if (!isValidIp(ip)) {
            ip = extractIpFromHeader(request, "Proxy-Client-IP");
        }

        // 3. WL-Proxy-Client-IP (WebLogic)
        if (!isValidIp(ip)) {
            ip = extractIpFromHeader(request, "WL-Proxy-Client-IP");
        }

        // 4. HTTP_CLIENT_IP
        if (!isValidIp(ip)) {
            ip = extractIpFromHeader(request, "HTTP_CLIENT_IP");
        }

        // 5. HTTP_X_FORWARDED_FOR
        if (!isValidIp(ip)) {
            ip = extractIpFromHeader(request, "HTTP_X_FORWARDED_FOR");
        }

        // 6. X-Real-IP (Nginx)
        if (!isValidIp(ip)) {
            ip = extractIpFromHeader(request, "X-Real-IP");
        }

        // 7. X-Cluster-Client-IP (일부 CDN)
        if (!isValidIp(ip)) {
            ip = extractIpFromHeader(request, "X-Cluster-Client-IP");
        }

        // 8. 최종: RemoteAddr
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // IPv6 루프백을 IPv4로 변환
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    /**
     * 헤더에서 IP를 추출하고, 여러 IP가 있는 경우 첫 번째 IP를 반환합니다.
     * X-Forwarded-For: client, proxy1, proxy2 형식에서 client IP만 추출
     */
    private static String extractIpFromHeader(HttpServletRequest request, String headerName) {
        String ip = request.getHeader(headerName);

        if (ip == null || ip.isEmpty()) {
            return null;
        }

        // 쉼표로 구분된 여러 IP 중 첫 번째(실제 클라이언트 IP) 추출
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 유효한 IP인지 검증합니다.
     */
    private static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        // "unknown" 문자열 체크 (대소문자 무관)
        return true;
    }

}