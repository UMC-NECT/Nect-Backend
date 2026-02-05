package com.nect.core.entity.user;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_careers")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCareer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_career_id")
    private Long userCareerId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "industry_field")
    private String industryField;

    @Column(name = "start_date", nullable = false)
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "is_ongoing", nullable = false)
    private Boolean isOngoing = false;

    @Column(name = "role")
    private String role;
}
