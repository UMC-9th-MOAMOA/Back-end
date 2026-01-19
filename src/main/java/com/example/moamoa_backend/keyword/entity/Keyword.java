package com.example.moamoa_backend.keyword.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;
}
