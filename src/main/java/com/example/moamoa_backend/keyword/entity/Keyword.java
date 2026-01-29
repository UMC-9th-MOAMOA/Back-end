package com.example.moamoa_backend.keyword.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.keyword.enums.KeywordType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KeywordType keywordType;
}
