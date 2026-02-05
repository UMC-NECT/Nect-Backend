package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "process_task_item")
public class ProcessTaskItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="process_id", nullable=false)
    private Process process;

    @Column(name = "content",nullable = false)
    private String content;

    @Column(name = "is_done")
    private boolean isDone;

    @Column(name = "done_at")
    private LocalDate doneAt;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", length = 50)
    private RoleField roleField;

    @Column(name = "custom_role_field_name", length = 50)
    private String customRoleFieldName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public ProcessTaskItem(Process process,
                           String content,
                           boolean isDone,
                           Integer sortOrder,
                           RoleField roleField,
                           String customRoleFieldName) {
        this.process = process;
        this.content = content;
        this.isDone = isDone;
        this.sortOrder = (sortOrder != null) ? sortOrder : 0;
        this.doneAt = isDone ? LocalDate.now() : null;

        // roleField는 일반 프로세스에서 null 허용
        if (roleField == RoleField.CUSTOM && (customRoleFieldName == null || customRoleFieldName.isBlank())) {
            throw new IllegalArgumentException("roleField가 CUSTOM이면 customRoleFieldName이 필수입니다.");
        }
        this.roleField = roleField;
        this.customRoleFieldName = (roleField == RoleField.CUSTOM) ? customRoleFieldName : null;
    }

    public void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    public void updateDone(boolean done) {
        this.isDone = done;
        this.doneAt = done ? LocalDate.now() : null;
    }

    public void updateSortOrder(Integer sortOrder) {
        if (sortOrder != null) this.sortOrder = sortOrder;
    }

    // 리더형 모달에서만 호출하도록 서비스에서 제한
    public void updateRoleField(RoleField roleField, String customRoleFieldName) {
        if (roleField == RoleField.CUSTOM && (customRoleFieldName == null || customRoleFieldName.isBlank())) {
            throw new IllegalArgumentException("roleField가 CUSTOM이면 customRoleFieldName이 필수입니다.");
        }
        this.roleField = roleField;
        this.customRoleFieldName = (roleField == RoleField.CUSTOM) ? customRoleFieldName : null;
    }


    void setProcess(Process process) {
        this.process = process;
    }


    public void softDelete() {
        if (this.deletedAt != null) return;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
