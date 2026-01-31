package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name="process_field",
        uniqueConstraints = {
                @UniqueConstraint(
                        name="uk_process_field_process_role_field",
                        columnNames={"process_id","role_field"}
                )
        }
)
public class ProcessField extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_field", nullable = false, length = 50)
    private RoleField roleField;

    // CUSTOM 대응
    @Column(name = "custom_field_name", length = 50)
    private String customFieldName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public ProcessField(Process process, RoleField roleField, String customFieldName) {
        this.process = process;
        this.roleField = roleField;
        this.customFieldName = customFieldName;
    }

    void setProcess(Process process) { this.process = process; }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
