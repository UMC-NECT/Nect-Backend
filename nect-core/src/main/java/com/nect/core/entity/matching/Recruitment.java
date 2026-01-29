package com.nect.core.entity.matching;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.team.Project;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "recruitment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Recruitment extends BaseEntity {

    // TODO: 타 도메인 구현 완료시 id 저장 방식 -> 엔티티 연관관계로 변경

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    @Column(name = "field_id", nullable = false)
    Long fieldId;

    @Column(name = "capacity", nullable = false)
    Integer capacity;

    public void decreaseCapacity(){
        this.capacity -= 1;
    }
}
