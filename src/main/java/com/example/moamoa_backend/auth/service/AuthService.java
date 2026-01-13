package com.example.moamoa_backend.auth.service;

import com.example.moamoa_backend.auth.dto.req.AuthReqDto;
import com.example.moamoa_backend.auth.exception.AuthException;
import com.example.moamoa_backend.auth.exception.code.AuthErrorCode;
import com.example.moamoa_backend.global.util.RedisUtil;
import com.example.moamoa_backend.member.entity.Member;
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

    private static final String AUTH_CODE_PREFIX = "AuthCode:";
    private static final String VERIFIED_PREFIX = "Verified:";

    private static final long AUTH_CODE_EXPIRE_SEC = 180L; // 3분
    private static final long VERIFIED_EXPIRE_SEC = 1200L; // 20분

    private static final int AUTH_CODE_LENGTH = 6; // 인증코드 6자리 설정

    // 이메일 인증번호 전송
    public void sendEmailAuthCode(String email) {
        // 중복 가입 체크
        if (memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
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

        // Redis 저장 (이메일 정보 + 인증코드, 3분)
        redisUtil.setDataExpire(AUTH_CODE_PREFIX + email, authCode, AUTH_CODE_EXPIRE_SEC);
    }

    // 이메일 인증번호 검증
    public void verifyEmailAuthCode(String email, String code) {
        String redisAuthCode = redisUtil.getData(AUTH_CODE_PREFIX + email);

        // 검증 (데이터 없음 == 만료됨 or 코드 불일치)
        if (redisAuthCode == null || !redisAuthCode.equals(code)) {
            throw new AuthException(AuthErrorCode.VERIFICATION_CODE_INVALID);
        }

        // 성공 처리: 인증 코드 삭제 & '인증됨' 플래그 저장 (20분)
        redisUtil.deleteData(AUTH_CODE_PREFIX + email);
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

    //회원가입
    @Transactional // Write 작업이므로 readOnly = false
    public Long signup(AuthReqDto.SignupDto request) {

        // 이메일 인증 여부 확인 (인증 안 된 이메일로 가입 시도 차단)
        String isVerified = redisUtil.getData(VERIFIED_PREFIX + request.email());
        if (isVerified == null || !"TRUE".equals(isVerified)) {
            //인증되지 않은 이메일을 이용한 회원가입 -> 접근 거부
            throw new AuthException(AuthErrorCode.ACCESS_DENIED);
        }

        // 이메일 중복 체크 (Double Check: 동시성 이슈 및 방어 로직)
        if (memberRepository.existsByEmail(request.email())) {
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

        // 인증 플래그 삭제 (재사용 방지)
        redisUtil.deleteData(VERIFIED_PREFIX + request.email());

        return savedMember.getId();
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
        List<Policy> requiredPolicies = policyRepository.findByIsMandatoryTrue();

        List<Long> missingTermIds = requiredPolicies.stream()
                .map(Policy::getId)
                .filter(id -> !agreedPolicyIds.contains(id))
                .toList();

        if (!missingTermIds.isEmpty()) {
            throw new AuthException(AuthErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }
    private void saveTermsAgreements(Member member, List<AuthReqDto.TermDto> requestPolicies) {
        Map<Long, Boolean> termMap = requestPolicies.stream()
                .distinct()
                .collect(Collectors.toMap(AuthReqDto.TermDto::policyId, AuthReqDto.TermDto::agreed));

        if (termMap.isEmpty()) return;

        List<Policy> policies = policyRepository.findAllById(termMap.keySet());

        if (policies.size() != termMap.size()) {
            throw new AuthException(AuthErrorCode.INVALID_POLICY_ID);
        }

        List<MemberPolicy> memberPolicies = policies.stream()
                .map(policy -> MemberPolicy.builder()
                        .member(member)
                        .policy(policy)
                        .isAgreed(termMap.get(policy.getId())) // 여기서 true/false가 그대로 저장됩니다.
                        .agreedAt(LocalDateTime.now())
                        .build())
                .toList();

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

}
