package com.example.moamoa_backend.domain.policy.repository;

import com.example.moamoa_backend.domain.policy.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 약관 Repository
 */
public interface PolicyRepository extends JpaRepository<Policy,Long> {

    /**
     * 활성화된 모든 약관 조회
     * - 필수 약관 먼저, 그 다음 ID 오름차순 정렬
     * - 회원가입 시 약관 목록 노출용
     *
     * @return 활성화된 약관 목록 (필수 우선 정렬)
     */
    List<Policy> findAllByIsActiveTrueOrderByIsMandatoryDescIdAsc();
}
