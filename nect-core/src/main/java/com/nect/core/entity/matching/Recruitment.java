package com.nect.core.entity.matching;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.enums.RoleField;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "recruitment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Recruitment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "field", nullable = false)
    RoleField field;

    @Column(name = "capacity", nullable = false)
    Integer capacity;

    @Column(name = "custom_field")
    private String customField;

    public void decreaseCapacity(){
        this.capacity -= 1;
    }
}
