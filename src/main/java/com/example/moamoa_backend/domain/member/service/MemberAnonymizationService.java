package com.example.moamoa_backend.domain.member.service;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.enums.MemberStatus;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberAnonymizationService {

    private final MemberRepository memberRepository;

    @Transactional
    public void anonymizeOne(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);

        if (member == null) {
            log.warn("익명화 대상 회원 없음: memberId={}", memberId);
            return;
        }

        if (member.getStatus() != MemberStatus.WITHDRAWN) {
            log.info("익명화 건너뜀 (상태 변경됨): memberId={}, status={}", memberId, member.getStatus());
            return;
        }

        member.anonymize();
        log.info("회원 익명화 완료: memberId={}", memberId);
    }
}