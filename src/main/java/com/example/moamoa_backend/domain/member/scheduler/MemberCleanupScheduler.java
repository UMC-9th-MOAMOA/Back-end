package com.example.moamoa_backend.domain.member.scheduler;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.enums.MemberStatus;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    public void anonymizeWithdrawnMembers() {
        List<Member> members = memberRepository.findByStatusAndDeletedAtBefore(
                MemberStatus.WITHDRAWN,
                LocalDateTime.now().minusDays(30)
        );

        for (Member member : members) {
            member.anonymize();
        }
    }

}
