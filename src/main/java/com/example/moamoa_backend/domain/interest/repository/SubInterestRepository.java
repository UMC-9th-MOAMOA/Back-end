package com.example.moamoa_backend.domain.interest.repository;

import com.example.moamoa_backend.domain.interest.entity.SubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * SubInterest(세부 관심사) Repository
 * - 관심사 목록 조회/온보딩 검증 시 사용
 */
public interface SubInterestRepository extends JpaRepository<SubInterest, Long> {

	/**
	 * 특정 대분류(interestId)에 속한 세부 관심사 리스트 조회
	 * - 프론트에서 "대분류 선택 -> 세부 목록 표시" 용도로 사용 가능
	 */
	List<SubInterest> findAllByInterest_IdOrderByIdAsc(Long interestId);

    Optional<SubInterest> findByName(String name);
	/**
	 * 인터페이스 프로젝션:
	 * subInterestId가 어느 interestId에 속하는지 (interestId, subId)만 뽑기 위해 사용
	 */
	interface InterestSubPair {
		Long getInterestId();
		Long getSubInterestId();
	}

	/**
	 * 온보딩 검증 핵심:
	 * 요청으로 들어온 subIds에 대해 "DB 기준 실제 소속(interestId)"을 조회한다.
	 *
	 * 이 쿼리로 가능한 것:
	 * 1) 존재 검증: 요청한 subIds 중 DB에 없는 값이 섞였는지 확인 (조회 결과 개수 비교)
	 * 2) 소속 검증: 요청 body의 interestId와 DB의 interestId가 일치하는지 검증
	 *
	 * 즉, 엔티티 전체 로딩 없이 "검증에 필요한 최소 컬럼"만 DB에서 가져온다.
	 */
	@Query("""
        select si.interest.id as interestId, si.id as subInterestId
        from SubInterest si
        where si.id in :subIds
    """)
	List<InterestSubPair> findInterestSubPairsBySubIds(Collection<Long> subIds);
}
