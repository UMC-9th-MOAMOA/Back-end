package com.example.moamoa_backend.domain.interest.repository;

import java.util.List;

import com.example.moamoa_backend.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, Long> {
	List<Interest> findAllByOrderByIdAsc();
}
