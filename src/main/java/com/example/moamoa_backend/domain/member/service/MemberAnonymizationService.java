package com.example.moamoa_backend.domain.member.service;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberAnonymizationService {

    private final MemberRepository memberRepository;

    @Transactional
    public void anonymizeOne(Long memberId) {
        memberRepository.findById(memberId)
                .ifPresent(Member::anonymize);
    }
}