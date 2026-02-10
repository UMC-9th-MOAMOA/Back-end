package com.example.moamoa_backend.item.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.moamoa_backend.item.entity.MemberItem;
import com.example.moamoa_backend.item.enums.ItemType;

@Repository
public interface MemberItemRepository extends JpaRepository<MemberItem, Long> {

	@EntityGraph(attributePaths = "item")
	Optional<MemberItem> findByMemberIdAndItemId(Long memberId, Long itemId);

	@EntityGraph(attributePaths = "item")
	List<MemberItem> findByMemberIdAndItem_TypeIn(Long memberId, List<ItemType> types);

	@EntityGraph(attributePaths = "item")
	List<MemberItem> findByMemberIdAndIsEquippedTrue(Long memberId);
}
