package com.example.moamoa_backend.item.repository;

import com.example.moamoa_backend.item.entity.Item;
import com.example.moamoa_backend.item.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
	List<Item> findByType(ItemType type);
}
