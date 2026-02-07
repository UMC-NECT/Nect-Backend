package com.nect.core.entity.team;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.team.enums.PlanFileType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectPlanFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name; // 사용자가 입력한 파일 명칭

    @Column(name = "file_name", nullable = false, columnDefinition = "TEXT")
    private String fileName; // r2에 올라갈 파일 이름 ( 링크 X )

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_file_type", nullable = false)
    private PlanFileType planFileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_ext", nullable = true)
    private FileExt fileExt; // planFileType = LINK이면 fileExt = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Builder
    protected ProjectPlanFile(
            String name,
            String fileName,
            PlanFileType planFileType,
            FileExt fileExt,
            Project project
    ){
        this.name = name;
        this.fileName = fileName;
        this.planFileType = planFileType;
        this.fileExt = planFileType == PlanFileType.FILE ? fileExt : null; // planFileType = LINK이면 fileExt = null
        this.project = project;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeFile(String fileName) {
        this.fileName = fileName;
    }

    public void changePlanFileType(PlanFileType planFileType) {
        this.planFileType = planFileType;
    }

    public void changeFileExt(FileExt fileExt) {
        this.fileExt = fileExt;
    }

}
