package com.example.moamoa_backend.domain.keyword.repository;

import com.example.moamoa_backend.domain.keyword.entity.Keyword;
import com.example.moamoa_backend.domain.keyword.enums.KeywordType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findTop5ByNameContaining(String name);

    Optional<Keyword> findByNameAndKeywordType(String name, KeywordType keywordType);
}
