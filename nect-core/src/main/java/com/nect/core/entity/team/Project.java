package com.nect.core.entity.team;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.enums.ProjectStatus;
import com.nect.core.entity.team.enums.RecruitmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project")
public class Project extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "information", columnDefinition = "TEXT")
    private String information;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(name = "notice_text", columnDefinition = "TEXT")
    private String noticeText;

    // 정규 회의
    @Column(name = "regular_meeting_text", columnDefinition = "TEXT")
    private String regularMeetingText;

    // 모집 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "recruitment_status", nullable = false)
    private RecruitmentStatus recruitmentStatus;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "planned_started_on")
    private LocalDate plannedStartedOn;

    @Column(name = "planned_ended_on")
    private LocalDate plannedEndedOn;



    @Builder
    protected Project(String title,
                      String description,
                      String information,
                      ProjectStatus status,
                      String noticeText,
                      String regularMeetingText) {
        this.title = title;
        this.description = description;
        this.information = information;
        this.status = (status == null) ? ProjectStatus.ACTIVE : status;
        this.noticeText = noticeText;
        this.regularMeetingText = regularMeetingText;
    }

    public void end() {
        this.status = ProjectStatus.ENDED;
        this.recruitmentStatus = RecruitmentStatus.CLOSED;
        this.endedAt = LocalDateTime.now();
    }

    public void updateNoticeText(String noticeText) {
        this.noticeText = noticeText;
    }

    public void updateRegularMeetingText(String regularMeetingText) {
        this.regularMeetingText = regularMeetingText;
    }
}
