package com.example.moamoa_backend.interest.repository;

import com.example.moamoa_backend.interest.entity.SubInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubInterestRepository extends JpaRepository<SubInterest, Long> {

	// interest_id로 세부관심사 전체 조회
	List<SubInterest> findAllByInterest_IdOrderByIdAsc(Long interestId);

    Optional<SubInterest> findByName(String name);
}
