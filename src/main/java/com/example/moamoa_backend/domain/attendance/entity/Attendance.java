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
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_attendance_member_date",
                columnNames = {"member_id", "attendanceDate"}
        )
})
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    private Attendance(Member member, LocalDate attendanceDate) {
        this.member = member;
        this.attendanceDate = attendanceDate;
    }

    public static Attendance create(Member member, LocalDate date) {
        return new Attendance(member, date);
    }

}
