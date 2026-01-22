package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.team.SharedDocument;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "process")
public class Process extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프로젝트 1 : N 프로세스(카드)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // TODO
    // 작성자(created_by)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "created_by", nullable = false)
//    private User createdBy;

    // TODO
    // 파트(분야/레인) - Field FK
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "field_id", nullable = false)
//    private Field field;

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Lob
    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessStatus status;

    @Column(name = "start_at")
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Column(name = "status_order", nullable = false)
    private Integer statusOrder = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProcessTaskItem> taskItems = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Link> links = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProcessUser> processUsers = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProcessSharedDocument> sharedDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProcessField> processFields = new ArrayList<>();


    // TODO : User createdBy, Field field 매개변수 추가하기
    @Builder
    private Process(Project project, String title, String content) {
        this.project = project;
//        this.createdBy = createdBy;
//        this.field = field;
        this.title = title;
        this.content = content;
        this.status = ProcessStatus.PLANNING;
        this.statusOrder = 0;
    }

    public void attachDocument(SharedDocument doc) {
        ProcessSharedDocument psd = ProcessSharedDocument.builder()
                .process(this)
                .document(doc)
                .build();
        this.sharedDocuments.add(psd);
    }

    public void addTaskItem(ProcessTaskItem item) {
        item.setProcess(this);
        this.taskItems.add(item);
    }

    public void addLink(Link link) {
        link.setProcess(this);
        this.links.add(link);
    }

    public void addProcessUser(ProcessUser pu) {
        pu.setProcess(this);
        this.processUsers.add(pu);
    }

    // TODO : Field 엔티티 생성되면 주석 풀기
//    public void addField(Field field) {
//        ProcessField pf = ProcessField.builder()
//                .process(this)
//                .field(field)
//                .build();
//        this.processFields.add(pf);
//    }

    public void updateStatus(ProcessStatus status) {
        if (status != null) this.status = status;
    }

    public void updatePeriod(LocalDate startAt, LocalDate endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}


