package com.example.moamoa_backend.wallet.repository;

import java.time.LocalDateTime;

import com.example.moamoa_backend.wallet.entity.WalletHistory;
import com.example.moamoa_backend.wallet.enums.TransactionType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletHistoryRepository extends JpaRepository<WalletHistory, Long> {

	@Query("""
        select count(wh)
        from WalletHistory wh
        join wh.wallet w
        where w.member.id = :memberId
          and wh.type = :type
          and wh.createdAt >= :startAt
          and wh.createdAt < :endAt
        """)
	long countByMemberAndTypeBetween(
		@Param("memberId") Long memberId,
		@Param("type") TransactionType type,
		@Param("startAt") LocalDateTime startAt,
		@Param("endAt") LocalDateTime endAt
	);
}
