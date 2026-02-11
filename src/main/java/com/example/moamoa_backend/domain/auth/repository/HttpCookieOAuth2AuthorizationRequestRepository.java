package com.example.moamoa_backend.domain.auth.repository;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

/**
 * OAuth2 인증 요청을 쿠키에 저장/조회/삭제하는 Repository
 * - 세션 대신 쿠키 사용 (Stateless 유지)
 * - CSRF 방지를 위한 state 값 보존 용도
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
	implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
	private static final int COOKIE_EXPIRE_SECONDS = 180;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return this.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
			.map(this::deserialize)
			.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(
		OAuth2AuthorizationRequest authorizationRequest,
		HttpServletRequest request,
		HttpServletResponse response) {

		if (authorizationRequest == null) {
			this.removeAuthorizationRequestCookies(request, response);
			return;
		}

		addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
			serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

		String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
		if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
			addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME,
				redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
		}
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(
		HttpServletRequest request,
		HttpServletResponse response) {

		OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
		if (authorizationRequest != null) {
			this.removeAuthorizationRequestCookies(request, response);
		}

		return authorizationRequest;
	}

	public void removeAuthorizationRequestCookies(
		HttpServletRequest request,
		HttpServletResponse response) {

		deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
	}

	// ----- Helper Methods -----

	private java.util.Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return java.util.Optional.of(cookie);
				}
			}
		}
		return java.util.Optional.empty();
	}

	private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.maxAge(maxAge)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					ResponseCookie deleteCookie = ResponseCookie.from(name, "")
						.path("/")
						.httpOnly(true)
						.secure(true)
						.sameSite("None")
						.maxAge(0)
						.build();
					response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
					return;
				}
			}
		}
	}

	private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
		return Base64.getUrlEncoder()
			.encodeToString(SerializationUtils.serialize(authorizationRequest));
	}

	private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
		return (OAuth2AuthorizationRequest)SerializationUtils.deserialize(
			Base64.getUrlDecoder().decode(cookie.getValue()));
	}
}