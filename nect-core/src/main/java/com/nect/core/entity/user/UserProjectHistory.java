package com.nect.core.entity.user;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_project_history")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProjectHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_project_history_id")
    private Long userProjectHistoryId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "project_image")
    private String projectImage;

    @Column(name = "project_description", columnDefinition = "TEXT")
    private String projectDescription;

    @Column(name = "start_year_month", nullable = false)
    private String startYearMonth;

    @Column(name = "end_year_month", nullable = false)
    private String endYearMonth;
}