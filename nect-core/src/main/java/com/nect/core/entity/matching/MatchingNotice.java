package com.nect.core.entity.matching;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matching_notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
