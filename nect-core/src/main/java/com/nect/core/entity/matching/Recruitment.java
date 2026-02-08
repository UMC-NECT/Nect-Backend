package com.nect.core.entity.matching;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "recruitment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Recruitment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "field", nullable = false)
    private RoleField field;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "custom_field")
    private String customField;

    @OneToMany(
            mappedBy = "recruitment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder asc")
    @Builder.Default
    private List<RecruitmentRequirement> requirements = new ArrayList<>();

    public void decreaseCapacity(){
        this.capacity -= 1;
    }

    public void addRequirement(RecruitmentRequirement requirement){
        requirements.add(requirement);
        requirement.setRecruitment(this);
    }

    public void updateField(RoleField field) {
        this.field = field;

    }
    public void updateCapacity(Integer capacity) {
        this.capacity = capacity;

    }
    public void updateCustomField(String customField) {
        this.customField = customField;
    }

}
