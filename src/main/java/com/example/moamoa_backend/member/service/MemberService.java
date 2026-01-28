package com.example.moamoa_backend.member.service;

import com.example.moamoa_backend.global.util.RedisUtil;
import com.example.moamoa_backend.member.dto.req.MemberReqDto;
import com.example.moamoa_backend.member.dto.res.MemberResDto;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.MemberStatus;
import com.example.moamoa_backend.member.enums.Provider;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    /**
     * 비밀번호 변경
     * 기존 비밀번호, 새 비밀번호, 새 비밀번호 확인 세가지 데이터를 통해 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long memberId, MemberReqDto.PasswordChange request) {

        // 멤버 조회 및 로컬 로그인 회원인지 체크
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if(member.getProvider() != Provider.LOCAL){
            throw new MemberException(MemberErrorCode.SOCIAL_LOGIN_MEMBER);
        }


        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.PASSWORD_NOT_MATCH);
        }

        // 새 비밀번호 확인 일치 검증
        if (!request.newPassword().equals(request.newPasswordCheck())) {
            throw new MemberException(MemberErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }

        // 기존 비밀번호와 새 비밀번호 동일 여부 검증
        if (passwordEncoder.matches(request.newPassword(), member.getPassword())) {
            throw new MemberException(MemberErrorCode.SAME_AS_OLD_PASSWORD);
        }

        // 비밀번호 변경
        member.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * 회원 탈퇴
     * 복구 요청 가능하기 때문에 access token 보유시 별도의 인증없이 삭제
     */
    @Transactional
    public void deleteMember(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 정지 계정 혹은 이미 삭제 요청된 계정 체크
        if(member.getStatus() == MemberStatus.BANNED){
            throw new MemberException(MemberErrorCode.MEMBER_BANNED);
        }
        if(member.getStatus() == MemberStatus.WITHDRAWN){
            throw new MemberException(MemberErrorCode.MEMBER_WITHDRAWN);
        }

        // soft delete 수행
        member.softDelete();

        // 서버에 저장된 Refresh Token 삭제
        redisUtil.deleteData("RT:" + memberId);
    }

    /**
     * 프로필 조회
     */
    public MemberResDto.ProfileResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        return MemberResDto.ProfileResponse.from(member);
    }
}