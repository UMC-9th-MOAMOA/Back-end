package com.example.moamoa_backend.domain.attendance.repository;

import com.example.moamoa_backend.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsByMember_IdAndAttendanceDate(Long memberId, LocalDate date);
}
