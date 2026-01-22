package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "process_field",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_process_field_process_field",
                        columnNames = {"process_id", "field_id"}
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

    // TODO
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "field_id", nullable = false)
//    private Field field;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // TODO : Field 추가
    @Builder
    public ProcessField(Process process) {
        this.process = process;
//        this.field = field;
    }

    void setProcess(Process process) { this.process = process; }
}
