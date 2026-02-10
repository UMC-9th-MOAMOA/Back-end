package com.example.moamoa_backend.domain.attendance.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attendance_streak")
public class AttendanceStreak extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ 별도 PK

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member; // ✅ member_id는 유니크로 1:1 보장


    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "last_attended_date")
    private LocalDate lastAttendedDate;

    /**
     * 마지막 7일 달성 날짜
     * - "다음날 streak 1부터 재시작" 처리에 사용
     */
    @Column(name = "last_completed_date")
    private LocalDate lastCompletedDate;

    private AttendanceStreak(Member member) {
        this.member = member;
        this.currentStreak = 0;
    }

    public static AttendanceStreak create(Member member) {
        return new AttendanceStreak(member);
    }

    public void applyToday(LocalDate today, int newStreak, boolean completedToday) {
        this.currentStreak = newStreak;
        this.lastAttendedDate = today;
        if (completedToday) {
            this.lastCompletedDate = today;
        }
    }
}
