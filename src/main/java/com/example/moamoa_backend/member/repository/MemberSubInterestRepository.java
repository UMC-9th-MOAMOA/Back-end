package com.example.moamoa_backend.member.repository;

import com.example.moamoa_backend.member.entity.MemberSubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface MemberSubInterestRepository extends JpaRepository<MemberSubInterest, Long> {

	interface InterestSubPair {
		Long getInterestId();
		Long getSubInterestId();
	}

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
        delete from MemberSubInterest msi
        where msi.member.id = :memberId
          and msi.subInterest.id in :subIds
    """)
	void deleteByMemberIdAndSubInterestIdIn(Long memberId, Collection<Long> subIds);

	@Query("""
        select si.interest.id as interestId, si.id as subInterestId
        from MemberSubInterest msi
        join msi.subInterest si
        where msi.member.id = :memberId
        order by si.interest.id asc, si.id asc
    """)
	List<InterestSubPair> findInterestSubPairsByMemberId(Long memberId);

	@Query("""
        select msi.subInterest.id
        from MemberSubInterest msi
        where msi.member.id = :memberId
        order by msi.subInterest.id asc
    """)
	List<Long> findSubInterestIdsByMemberId(Long memberId);
}
