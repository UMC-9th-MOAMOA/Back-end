package com.example.moamoa_backend.policy.repository;

import com.example.moamoa_backend.policy.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy,Long> {
    List<Policy> findByIsMandatoryTrueAndIsActiveTrue();
}
