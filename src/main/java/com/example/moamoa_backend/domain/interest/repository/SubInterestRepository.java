package com.example.moamoa_backend.domain.interest.repository;

import com.example.moamoa_backend.domain.interest.entity.SubInterest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubInterestRepository extends JpaRepository<SubInterest, Long> {

	List<SubInterest> findAllByInterest_IdOrderByIdAsc(Long interestId);

	Optional<SubInterest> findByName(String name);

	//인터페이스 프로젝션
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
