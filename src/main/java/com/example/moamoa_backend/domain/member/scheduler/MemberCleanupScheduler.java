package com.example.moamoa_backend.domain.member.scheduler;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.enums.MemberStatus;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.member.service.MemberAnonymizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberRepository memberRepository;
    private final MemberAnonymizationService memberAnonymizationService;

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    public void anonymizeWithdrawnMembers() {
        List<Member> members = memberRepository.findByStatusAndDeletedAtBefore(
                MemberStatus.WITHDRAWN,
                LocalDateTime.now().minusDays(30)
        );

        for (Member member : members) {
            try {
                memberAnonymizationService.anonymizeOne(member.getId());
            } catch (Exception e) {
                log.error("회원 익명화 실패: memberId={}", member.getId(), e);
            }
        }
    }
}
