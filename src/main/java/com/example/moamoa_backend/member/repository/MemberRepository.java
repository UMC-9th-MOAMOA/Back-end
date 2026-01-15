package com.example.moamoa_backend.member.repository;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByEmailAndProvider(String email, Provider provider);
}
