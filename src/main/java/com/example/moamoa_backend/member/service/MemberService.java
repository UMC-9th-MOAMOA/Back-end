package com.example.moamoa_backend.member.service;

import com.example.moamoa_backend.global.util.RedisUtil;
import com.example.moamoa_backend.member.dto.req.MemberReqDto;
import com.example.moamoa_backend.member.dto.res.MemberResDto;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.Gender;
import com.example.moamoa_backend.member.enums.MemberStatus;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

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

    /**
     * 프로필 수정
     */
    @Transactional
    public void updateProfile(Long memberId, MemberReqDto.ProfileUpdate request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Gender genderEnum = null;
        if (request.gender() != null) {
            try {
                genderEnum = Gender.valueOf(request.gender());
            } catch (IllegalArgumentException e) {
                throw new MemberException(MemberErrorCode.INVALID_GENDER);
            }
        }

        member.updateProfile(
                request.profileImage(),
                request.name(),
                request.birthday(),
                genderEnum
        );
    }
}