package com.example.moamoa_backend.domain.member.entity.mapping;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.mission.entity.Mission;
import com.example.moamoa_backend.domain.quiz.entity.Quiz;
import com.example.moamoa_backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member_quiz")
public class MemberQuiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    private String selectedAnswer;

    private boolean isCorrect;

    public void updateResult(String selectedAnswer, boolean isCorrect){
        this.selectedAnswer = selectedAnswer;
        this.isCorrect = isCorrect;
    }
}
