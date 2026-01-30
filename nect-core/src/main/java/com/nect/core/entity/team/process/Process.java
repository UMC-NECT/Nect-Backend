package com.nect.core.entity.team.process;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.team.SharedDocument;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

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

    @Column(name = "content", columnDefinition = "TEXT")
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

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL)
    @SQLRestriction("deleted_at is null")
    private final List<ProcessTaskItem> taskItems = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL)
    @SQLRestriction("deleted_at is null")
    private final List<Link> links = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL)
    @SQLRestriction("deleted_at is null")
    private final List<ProcessFeedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProcessUser> processUsers = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL)
    @SQLRestriction("deleted_at is null")
    private final List<ProcessSharedDocument> sharedDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProcessField> processFields = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL)
    @SQLRestriction("deleted_at is null")
    private final List<ProcessMention> mentions = new ArrayList<>();


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
        if (doc == null || doc.getId() == null) {
            throw new IllegalArgumentException("문서 객체 또는 문서 ID는 null일 수 없습니다.");
        }

        boolean exists = sharedDocuments.stream()
                .anyMatch(psd ->
                        psd.getDocument() != null
                                && psd.getDocument().getId() != null
                                && psd.getDocument().getId().equals(doc.getId())
                );

        if (exists) {
            throw new IllegalStateException("해당 문서는 이미 첨부되었습니다. documentId = " + doc.getId());
        }

        ProcessSharedDocument psd = ProcessSharedDocument.builder()
                .process(this)
                .document(doc)
                .build();

        sharedDocuments.add(psd);
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

    public void updateTitle(String title) {
        this.title = title;
    }


    public void updateStatusOrder(Integer statusOrder) {
        if (statusOrder != null) this.statusOrder = statusOrder;
    }


    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void softDeleteCascade() {
        if (this.deletedAt != null) return; // 이미 삭제면 idempotent

        this.softDelete(); // deletedAt 세팅

        if (this.taskItems != null) {
            this.taskItems.forEach(ProcessTaskItem::softDelete);
        }
        if (this.feedbacks != null) {
            this.feedbacks.forEach(ProcessFeedback::softDelete);
        }
        if (this.links != null) {
            this.links.forEach(Link::softDelete);
        }
        if (this.sharedDocuments != null) {
            this.sharedDocuments.forEach(ProcessSharedDocument::softDelete);
        }
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}


