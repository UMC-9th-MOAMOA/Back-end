package com.example.moamoa_backend.domain.item.repository;

import com.example.moamoa_backend.domain.item.entity.Item;
import com.example.moamoa_backend.domain.item.enums.ItemType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
	List<Item> findByTypeIn(List<ItemType> types);
}
