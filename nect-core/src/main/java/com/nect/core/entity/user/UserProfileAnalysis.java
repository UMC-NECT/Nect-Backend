package com.nect.core.entity.user;

import com.nect.core.entity.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "user_profile_analysis", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserProfileAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String profileType;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON")
    private String tags;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON")
    private String collaborationStyle;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON")
    private String skills;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON", length = 2000)
    private String roleRecommendation;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON")
    private String growthGuide;
}