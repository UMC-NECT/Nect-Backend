package com.nect.core.entity.matching;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "recruitment_requirement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RecruitmentRequirement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @Column(name = "content")
    private String content;

    @Column(name = "sort_order")
    private int sortOrder;

    protected void setRecruitment(Recruitment recruitment) {
        this.recruitment = recruitment;
    }
}
