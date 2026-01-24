package com.example.moamoa_backend.member.repository;

import com.example.moamoa_backend.member.entity.mapping.MemberSubInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

/**
 * Member - SubInterest 간 N:M 매핑 테이블(MemberSubInterest)을 다루는 Repository.
 * - 온보딩에서 "회원이 선택한 세부 관심사(subInterest)"를 저장/조회/삭제하는 역할
 */
public interface MemberSubInterestRepository extends JpaRepository<MemberSubInterest, Long> {

	/**
	 * 인터페이스 프로젝션:
	 * DB에서 필요한 컬럼만 뽑아서 (interestId, subInterestId) 형태로 받기 위해 사용
	 * - 엔티티 전체 로딩 없이 조회 성능/메모리 효율을 높임
	 */
	interface InterestSubPair {
		Long getInterestId();
		Long getSubInterestId();
	}

	/**
	 * Smart Sync(차이만 반영)에서 "삭제 대상"만 골라 삭제할 때 사용.
	 *
	 * delete from MemberSubInterest
	 * where member_id = :memberId
	 *   and sub_interest_id in (:subIds)
	 *
	 * @Modifying: JPQL delete/update 실행을 위한 어노테이션
	 * clearAutomatically=true: 영속성 컨텍스트에 남아있을 수 있는 기존 엔티티 상태를 정리
	 * flushAutomatically=true: delete 쿼리를 즉시 DB에 반영(이후 insert 충돌 방지)
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
        delete from MemberSubInterest msi
        where msi.member.id = :memberId
          and msi.subInterest.id in :subIds
    """)
	void deleteByMemberIdAndSubInterestIdIn(Long memberId, Collection<Long> subIds);

	/**
	 * 조회(응답 구성용):
	 * 회원이 선택한 세부 관심사들을 (interestId, subInterestId) pair로 가져옴.
	 *
	 * - 응답 selections를 "interestId 기준 그룹핑"해야 하므로
	 *   join을 통해 si.interest.id 까지 함께 가져온다.
	 */
	@Query("""
        select si.interest.id as interestId, si.id as subInterestId
        from MemberSubInterest msi
        join msi.subInterest si
        where msi.member.id = :memberId
        order by si.interest.id asc, si.id asc
    """)
	List<InterestSubPair> findInterestSubPairsByMemberId(Long memberId);

	/**
	 * 조회(Smart Sync 계산용):
	 * 회원이 현재 DB에 저장해 둔 subInterestId 목록만 필요할 때 사용.
	 * - existingSubIds 확보용 (기존 vs 요청 비교)
	 */
	@Query("""
        select msi.subInterest.id
        from MemberSubInterest msi
        where msi.member.id = :memberId
        order by msi.subInterest.id asc
    """)
	List<Long> findSubInterestIdsByMemberId(Long memberId);
}
