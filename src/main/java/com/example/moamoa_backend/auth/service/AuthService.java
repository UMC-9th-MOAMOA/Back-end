package com.example.moamoa_backend.auth.service;

import com.example.moamoa_backend.auth.dto.req.AuthReqDto;
import com.example.moamoa_backend.auth.dto.res.AuthResDto;
import com.example.moamoa_backend.auth.exception.AuthException;
import com.example.moamoa_backend.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.global.security.jwt.JwtUtil;
import com.example.moamoa_backend.global.security.jwt.exception.JwtException;
import com.example.moamoa_backend.global.security.jwt.exception.code.JwtErrorCode;
import com.example.moamoa_backend.global.util.RedisUtil;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.MemberStatus;
import com.example.moamoa_backend.member.enums.Provider;
import com.example.moamoa_backend.member.enums.Role;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.policy.entity.MemberPolicy;
import com.example.moamoa_backend.policy.entity.Policy;
import com.example.moamoa_backend.policy.repository.MemberPolicyRepository;
import com.example.moamoa_backend.policy.repository.PolicyRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final PolicyRepository policyRepository;
    private final MemberPolicyRepository memberPolicyRepository;
    private final JwtUtil jwtUtil;

    // 이메일과 보안코드 저장 관련 (예)AuthCode:moamoa@gmail.com : 123456
    private static final String AUTH_CODE_PREFIX = "AuthCode:";
    private static final long AUTH_CODE_EXPIRE_SEC = 180L;

    // 이메일 인증 성공 플래그 저장 관련 (예)Verified:moamoa@gmail.com : TRUE
    private static final String VERIFIED_PREFIX = "Verified:";
    private static final long VERIFIED_EXPIRE_SEC = 300L;

    // 이메일 1회 전송에 대해 n회 인증 실패시 관련 (예)AuthCodeFail:moamoa@gamil.com : 4
    // MAX_AUTH_ATTEMPTS 도달시 파기
    private static final String AUTH_CODE_FAIL_PREFIX = "AuthCodeFail:";
    private static final int MAX_AUTH_ATTEMPTS = 5;

    // 동일 이메일에 대한 요청 Cool-down 관련
    // (예)EmailSendBlock:moamoa@gmail.com : BLOCKED
    private static final String EMAIL_SEND_BLOCK_PREFIX = "EmailSendBlock:";
    private static final long EMAIL_SEND_BLOCK_SEC = 30L;

    // 동일 IP 단위 시간당 MAX REQUEST 도달 시 차단 관련
    // (예)AuthIp:1.2.3.4 : 17
    // (예)AuthIp:1.2.3.4:BAN : BLOCKED
    private static final String AUTH_IP_PREFIX = "AuthIp:";
    private static final int MAX_IP_REQUESTS = 20;
    private static final long AUTH_IP_EXPIRE_SEC = 3600L;
    private static final long AUTH_IP_BAN_SEC = 3600L;

    private static final int AUTH_CODE_LENGTH = 6; // 인증코드 6자리 설정

    // 이메일 인증번호 전송
    public void sendEmailAuthCode(String email, String clientIp) {
        // 중복 가입 체크
        if (memberRepository.findByProviderAndProviderId(Provider.LOCAL, email).isPresent()) {
            throw new MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 30초 쿨다운 체크
        if (redisUtil.getData(EMAIL_SEND_BLOCK_PREFIX + email) != null) {
            throw new AuthException(AuthErrorCode.EMAIL_SEND_BLOCKED);
        }

        // IP BAN 체크
        String ipKey = AUTH_IP_PREFIX + clientIp;
        String banStatus = redisUtil.getData(ipKey + ":BAN");
        if (banStatus != null) {
            throw new AuthException(AuthErrorCode.IP_RATE_LIMIT_EXCEEDED);
        }

        // 현재 카운트 확인
        String currentCountStr = redisUtil.getData(ipKey);
        Long currentIpCount = (currentCountStr != null) ? Long.parseLong(currentCountStr) : 0;

        // IP 시도횟수 초과했는지 체크
        if (currentIpCount >= MAX_IP_REQUESTS) {
            // BAN 처리
            redisUtil.deleteData(ipKey);
            redisUtil.setDataExpire(ipKey + ":BAN", "BLOCKED", AUTH_IP_BAN_SEC);

            log.warn("IP banned for 1 hours: {}", clientIp);
            throw new AuthException(AuthErrorCode.IP_RATE_LIMIT_EXCEEDED);
        }

        // 인증 번호 생성 (6자리)
        String authCode = createAuthCode();

        // 이메일 전송
        try {
            MimeMessage message = createEmailForm(email, authCode);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            // 이메일 전송 실패 에러처리
            log.error("Email send failed for {}: {}", maskEmail(email), e.getMessage());
            throw new AuthException(AuthErrorCode.EMAIL_SEND_FAILED);
        }

        //카운트 증가 및 첫 요청이면 만료 시간 설정
        Long newCount = redisUtil.increment(ipKey);
        if (newCount != null && newCount == 1) {
            redisUtil.setExpire(ipKey, AUTH_IP_EXPIRE_SEC);
        }

        // Redis 저장 (이메일 정보 + 인증코드, 3분)
        redisUtil.setDataExpire(AUTH_CODE_PREFIX + email, authCode, AUTH_CODE_EXPIRE_SEC);

        // 재전송 방지 플래그 저장 (30초 동안 유지)
        redisUtil.setDataExpire(EMAIL_SEND_BLOCK_PREFIX + email, "BLOCKED", EMAIL_SEND_BLOCK_SEC);

        // 기존 실패 횟수 초기화 (새 코드를 전송했기 떄문)
        redisUtil.deleteData(AUTH_CODE_FAIL_PREFIX + email);


    }

    // 이메일 인증번호 검증
    public void verifyEmailAuthCode(String email, String code) {
        String authCodeKey = AUTH_CODE_PREFIX + email;
        String failCountKey = AUTH_CODE_FAIL_PREFIX + email;
        String redisAuthCode = redisUtil.getData(authCodeKey);

        // 데이터 없음 -> 잘못된 코드
        if (redisAuthCode == null) {
            throw new AuthException(AuthErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        // 인증번호가 불일치하는 경우 (실패)
        if(!redisAuthCode.equals(code)) {
            Long currentFailCount = redisUtil.increment(failCountKey); // 카운트 1 증가

            // 카운트가 처음 생성되었다면, 만료 시간(3분) 설정
            if (currentFailCount != null && currentFailCount == 1) {
                redisUtil.setExpire(failCountKey, AUTH_CODE_EXPIRE_SEC);
            }

            // 최대 횟수 초과 체크
            if (currentFailCount >= MAX_AUTH_ATTEMPTS) {
                redisUtil.deleteData(authCodeKey);  // 인증번호 파기
                redisUtil.deleteData(failCountKey); // 카운트 정리
                throw new AuthException(AuthErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED); // 입력횟수 초과 에러
            }

            // 단순 불일치 에러
            throw new AuthException(AuthErrorCode.VERIFICATION_CODE_INVALID);
        }


        // 성공 처리: 인증 코드 삭제 & '인증됨' 플래그 저장 (20분)
        redisUtil.deleteData(authCodeKey);  //인증번호 삭제
        redisUtil.deleteData(failCountKey); //실패 카운트 삭제
        redisUtil.setDataExpire(VERIFIED_PREFIX + email, "TRUE", VERIFIED_EXPIRE_SEC);

    }

    // 인증여부 확인 (회원가입 요청시 메일인증 여부 확인)
    public void checkEmailVerified(String email) {
        String isVerified = redisUtil.getData(VERIFIED_PREFIX + email);
        if (isVerified == null) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED);
        }
    }

    // 인증 플래그 삭제 (회원가입 완료 후 인증 플래그 삭제)
    public void clearVerificationFlag(String email) {
        redisUtil.deleteData(VERIFIED_PREFIX + email);
    }

    /**
     * 회원가입 (Local)
     * - 자동 로그인
     */
    @Transactional
    public AuthResDto.GeneratedTokenDto signup(AuthReqDto.SignupDto request) {

        // 이메일 인증 여부 확인 (인증 안 된 이메일로 가입 시도 차단)
        String isVerified = redisUtil.getData(VERIFIED_PREFIX + request.email());
        if (!"TRUE".equals(isVerified)) {
            //인증되지 않은 이메일을 이용한 회원가입 -> 접근 거부
            throw new AuthException(AuthErrorCode.ACCESS_DENIED);
        }

        // 이메일 중복 체크 (Double Check: 동시성 이슈 및 방어 로직)
        if (memberRepository.findByProviderAndProviderId(Provider.LOCAL, request.email()).isPresent()) {
            throw new MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 필수 약관 동의 검증
        validateRequiredTerms(request.agreedTerms());

        // Member 엔티티 생성 및 비밀번호 암호화
        Member newMember = request.toEntity(passwordEncoder);

        // DB 저장
        Member savedMember = memberRepository.save(newMember);

        // 약관 동의 내역 저장
        saveTermsAgreements(savedMember, request.agreedTerms());

        //자동 로그인을 위해 토큰 생성 및 Redis에 Refresh Token 저장
        AuthResDto.GeneratedTokenDto tokenDto = generateTokens(savedMember.getId(), savedMember.getRole());

        // 인증 플래그 삭제 (재사용 방지)
        redisUtil.deleteData(VERIFIED_PREFIX + request.email());

        return tokenDto;
    }

    /**
     * 로그인 (Local)
     */
    @Transactional
    public AuthResDto.GeneratedTokenDto login(AuthReqDto.LoginDto request) {
        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByProviderAndProviderId(Provider.LOCAL, request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAILED));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.LOGIN_FAILED);
        }

        // 3. 상태 검증
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AuthException(AuthErrorCode.ACCOUNT_WITHDRAWN);
        }
        if (member.getStatus() == MemberStatus.BANNED) {
            throw new AuthException(AuthErrorCode.ACCOUNT_BANNED);
        }

        // 4. 토큰 발급 및 Redis 저장
        return generateTokens(member.getId(), member.getRole());
    }

    /**
     * 토큰 재발급 (RTR 적용)
     */
    @Transactional
    public AuthResDto.GeneratedTokenDto refresh(AuthReqDto.ReissueDto request) {
        String requestRefreshToken = request.refreshToken();

        // 1. Refresh Token 유효성 검증
        jwtUtil.validateToken(requestRefreshToken);

        // 2. 토큰 정보 추출
        Long memberId = jwtUtil.getMemberId(requestRefreshToken);
        String redisKey = "RT:" + memberId;
        String storedRefreshToken = redisUtil.getData(redisKey);

        // 3. 탈취 감지 및 유효성 검사
        // Redis에 토큰이 없거나, 요청 온 토큰과 다르면 탈취로 간주
        if (storedRefreshToken == null || !storedRefreshToken.equals(requestRefreshToken)) {
            redisUtil.deleteData(redisKey); // 저장된 토큰 삭제 (로그인 풀림 처리)
            throw new JwtException(JwtErrorCode.TOKEN_INVALID);
        }

        // 4. 회원 정보 조회 (Role 등 최신 정보 갱신을 위해)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAILED));

        // 5. 새 토큰 발급 (RTR)
        return generateTokens(member.getId(), member.getRole());
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long memberId) {
        // Redis에서 Refresh Token 삭제
        redisUtil.deleteData("RT:" + memberId);
    }

    /**
     * 로컬계정 복구
     */
    @Transactional
    public AuthResDto.GeneratedTokenDto recover(AuthReqDto.LoginDto request) {
        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByProviderAndProviderId(Provider.LOCAL, request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAILED));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.LOGIN_FAILED);
        }

        // 3. WITHDRAWN 상태만 복구 가능
        if (member.getStatus() != MemberStatus.WITHDRAWN) {
            throw new AuthException(AuthErrorCode.INVALID_RECOVER_REQUEST);
        }

        // 4. 상태 변경
        member.activate();

        // 5. 토큰 발급
        return generateTokens(member.getId(), member.getRole());
    }

    // -- Helper Methods --

    // 메일 인증코드 생성
    private String createAuthCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < AUTH_CODE_LENGTH; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
    // 인증 메일 생성
    private MimeMessage createEmailForm(String email, String authCode) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[MoaMoa] 이메일 인증 번호입니다.");
        // HTML 형식으로 전송
        helper.setText(
                "<div style='margin:20px;'>" +
                        "<h1>안녕하세요 MoaMoa 입니다.</h1>" +
                        "<br>" +
                        "<p>아래 인증번호를 입력해주세요.</p>" +
                        "<br>" +
                        "<div align='center' style='border:1px solid black; font-family:verdana;'>" +
                        "<h3 style='color:blue;'>회원가입 인증 번호</h3>" +
                        "<div style='font-size:130%'>" + authCode + "</div>" +
                        "</div>" +
                        "<br/>" +
                        "</div>",
                true
        );
        return message;
    }
    private void validateRequiredTerms(List<AuthReqDto.TermDto> agreedPolicy) {
        Set<Long> agreedPolicyIds = agreedPolicy.stream()
                .filter(AuthReqDto.TermDto::agreed)
                .map(AuthReqDto.TermDto::policyId)
                .collect(Collectors.toSet());

        // PolicyRepository에서 필수 약관 조회
        List<Policy> requiredPolicies = policyRepository.findByIsMandatoryTrueAndIsActiveTrue();

        List<Long> missingTermIds = requiredPolicies.stream()
                .map(Policy::getId)
                .filter(id -> !agreedPolicyIds.contains(id))
                .toList();

        if (!missingTermIds.isEmpty()) {
            throw new AuthException(AuthErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }
    private void saveTermsAgreements(Member member, List<AuthReqDto.TermDto> requestPolicies) {

        // 1. 요청된 약관 ID 추출
        Set<Long> requestedPolicyIds = requestPolicies.stream()
                .map(AuthReqDto.TermDto::policyId)
                .collect(Collectors.toSet());

        // 2. 현재 활성화된 모든 약관 조회
        List<Policy> allActivePolicies = policyRepository.findAllByIsActiveTrue();

        // 3. 유효하지 않은 약관 ID 체크
        Set<Long> validPolicyIds = allActivePolicies.stream()
                .map(Policy::getId)
                .collect(Collectors.toSet());

        Set<Long> invalidPolicyIds = requestedPolicyIds.stream()
                .filter(id -> !validPolicyIds.contains(id))
                .collect(Collectors.toSet());

        if (!invalidPolicyIds.isEmpty()) {
            log.error("Invalid policy IDs received: {}", invalidPolicyIds);
            throw new AuthException(AuthErrorCode.INVALID_POLICY_ID);
        }

        // 4. Map 변환
        Map<Long, Boolean> requestTermMap = requestPolicies.stream()
                .collect(Collectors.toMap(
                        AuthReqDto.TermDto::policyId,
                        AuthReqDto.TermDto::agreed,
                        (existing, replacement) -> replacement
                ));

        // 5. 저장
        List<MemberPolicy> memberPolicies = allActivePolicies.stream()
                .map(policy -> MemberPolicy.builder()
                        .member(member)
                        .policy(policy)
                        .isAgreed(requestTermMap.getOrDefault(policy.getId(), false))
                        .agreedAt(requestTermMap.getOrDefault(policy.getId(), false)
                                ? LocalDateTime.now() : null)
                        .build())
                .toList();

        // 6. 일괄 저장
        memberPolicyRepository.saveAll(memberPolicies);
    }
    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }

        int atIndex = email.indexOf('@');
        // @가 없거나, 아이디가 너무 짧은 경우(2글자 이하) 앞부분 전체 마스킹 처리 등 예외 대응
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }

        // 앞 2글자만 보여주고 나머지는 *** 처리 (예: te***@naver.com)
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    private AuthResDto.GeneratedTokenDto generateTokens(Long memberId, Role role) {
        // Access/Refresh 토큰 생성
        String accessToken = jwtUtil.createAccessToken(memberId, String.valueOf(role));
        String refreshToken = jwtUtil.createRefreshToken(memberId);

        // Redis 저장 (Key: "RT:{id}", Value: token, Duration: 14일)
        // 인자 순서: key, value, 만료시간(ms)
        redisUtil.setDataExpire("RT:" + memberId, refreshToken, jwtUtil.getRefreshTokenValidity()/1000);

        return AuthResDto.GeneratedTokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtUtil.getAccessTokenValidity())
                .build();
    }

    /**
     * 임시 코드(OAuthCode)를 검증하고 AccessToken을 반환
     * 소셜 로그인에서 accessToken 최초 발급 시 사용
     */
    public AuthResDto.TokenDto exchangeAuthCode(String code) {
        // 1. Redis Key 생성
        String redisKey = "OAUTH_CODE:" + code;

        // 2. Redis 조회 및 파기
        String accessToken = redisUtil.getAndDeleteData(redisKey);

        // 3. 검증
        if (accessToken == null) {
            throw new AuthException(AuthErrorCode.INVALID_OAUTH_CODE);
        }

        AuthResDto.TokenDto tokenDto = AuthResDto.TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .accessTokenExpiresIn(jwtUtil.getAccessTokenValidity())
                .build();


        // 4. 토큰 반환
        return tokenDto;
    }

}
