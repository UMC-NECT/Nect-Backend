package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "link")
public class Link extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="process_id", nullable=false)
    private Process process;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Builder
    public Link(Process process, String url) {
        this.process = process;
        this.url = url;
    }

    void setProcess(Process process) {
        this.process = process;
    }
}
