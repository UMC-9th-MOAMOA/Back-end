package com.example.moamoa_backend.interest.repository;

import com.example.moamoa_backend.interest.entity.SubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface SubInterestRepository extends JpaRepository<SubInterest, Long> {

	List<SubInterest> findAllByInterest_IdOrderByIdAsc(Long interestId);

	interface InterestSubPair {
		Long getInterestId();
		Long getSubInterestId();
	}

	@Query("""
        select si.interest.id as interestId, si.id as subInterestId
        from SubInterest si
        where si.id in :subIds
    """)
	List<InterestSubPair> findInterestSubPairsBySubIds(Collection<Long> subIds);
}
