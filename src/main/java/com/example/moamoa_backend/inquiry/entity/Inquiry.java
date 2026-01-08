package com.example.moamoa_backend.inquiry.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.inquiry.enums.InquiryStatus;
import com.example.moamoa_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;//이거 텍스트

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = true)
    private LocalDateTime answeredAt;

    @Column(nullable = false)
    private boolean isSecret;

    //createdAt = 질문일시

}
