package com.example.moamoa_backend.domain.member.repository;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.entity.mapping.MemberQuiz;
import com.example.moamoa_backend.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberQuizRepository extends JpaRepository<MemberQuiz,Long> {

    // 특정 퀴즈에 대한 기록 조회 (업데이트용)
    Optional<MemberQuiz> findByMemberAndQuiz(Member member, Quiz quiz);

    // 특정 미션의 모든 퀴즈 기록 조회 (상세 조회용)
    List<MemberQuiz> findAllByMemberIdAndMissionId(Long memberId, Long missionId);

}
