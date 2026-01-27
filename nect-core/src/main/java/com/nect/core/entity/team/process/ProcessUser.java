package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.process.enums.AssignmentRole;
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
                        columnNames = {"process_id", "member_id"}
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

    // TODO
    // 연관 관계는 주석으로 유지하되, DB 컬럼만 미리 만들어둠
    @Column(name = "member_id")
    private Long memberId;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_role", nullable = false)
    private AssignmentRole assignmentRole;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // TODO : user 추가
    @Builder
    public ProcessUser(Process process, AssignmentRole assignmentRole, LocalDateTime assignedAt) {
        this.process = process;
//        this.user = user;
        this.assignmentRole = assignmentRole;
        this.assignedAt = (assignedAt != null) ? assignedAt : LocalDateTime.now();
        this.deletedAt = null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    void setProcess(Process process) {
        this.process = process;
    }

//    void setMember(User user) {
//        this.user = user;
//    }
}
