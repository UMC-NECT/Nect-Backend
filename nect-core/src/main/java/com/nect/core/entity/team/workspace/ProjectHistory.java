package com.nect.core.entity.team.workspace;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.workspace.enums.ActionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "project_history")
public class ProjectHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    // TODO
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "project_id", nullable = false)
//    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "content", nullable = false)
    private String content;


}
