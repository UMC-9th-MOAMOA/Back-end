package com.example.moamoa_backend.inquiry.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.inquiry.enums.InquiryCategory;
import com.example.moamoa_backend.inquiry.enums.InquiryStatus;
import com.example.moamoa_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Setter
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private String content;

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

    // 질문 이미지
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryImage> inquiryImages = new ArrayList<>();

    // ⭐ 답변 이미지
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerImage> answerImages = new ArrayList<>();

    // ⭐ 문의 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryCategory category;

}
