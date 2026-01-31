package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.process.enums.AssignmentRole;
import com.nect.core.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "process_user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_process_user_process_member",
                        columnNames = {"process_id", "user_id"}
                )
        }
)
public class ProcessUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 하나의 프로세스에 여러 유저 배정, 관찰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_role", nullable = false)
    private AssignmentRole assignmentRole;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // TODO : user 추가
    @Builder
    public ProcessUser(Process process, User user, AssignmentRole assignmentRole, LocalDateTime assignedAt) {
        this.process = process;
        this.user = user;
        this.assignmentRole = assignmentRole;
        this.assignedAt = (assignedAt != null) ? assignedAt : LocalDateTime.now();
        this.deletedAt = null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
        this.assignedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    void setProcess(Process process) {
        this.process = process;
    }

    void setUser(User user) {
        this.user = user;
    }
}
