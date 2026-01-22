package com.example.moamoa_backend.attendance.service.query;

import com.example.moamoa_backend.attendance.repository.AttendanceStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceQueryServiceImpl implements AttendanceQueryService{
    private final AttendanceStreakRepository attendanceStreakRepository;

    @Override
    public int getCurrentStreak(Long memberId) {
        return attendanceStreakRepository.findByMember_Id(memberId)
                .map(s -> Math.max(0, s.getCurrentStreak()))
                .orElse(0);
    }
}
