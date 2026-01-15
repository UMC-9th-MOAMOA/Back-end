package com.example.moamoa_backend.policy.repository;

import com.example.moamoa_backend.policy.entity.MemberPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPolicyRepository extends JpaRepository<MemberPolicy,Long> {
}
