package com.example.moamoa_backend.domain.wallet.repository;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.wallet.entity.Wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import jakarta.persistence.LockModeType;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
	Optional<Wallet> findByMemberId(Long memberId);

	boolean existsByMember(Member member);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select w from Wallet w where w.member.id = :memberId")
	Optional<Wallet> findByMemberIdForUpdate(@Param("memberId") Long memberId);
}
