package com.nect.core.repository.team;

import com.nect.core.entity.team.ProjectSchedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectScheduleRepository extends JpaRepository<ProjectSchedule, Long> {

    Optional<ProjectSchedule> findByIdAndProjectIdAndDeletedAtIsNull(Long id, Long projectId);

    // 월간 인디케이터/다가오는 일정용: 기간 겹치는 일정 조회
    @Query("""
        select s
        from ProjectSchedule s
        where s.project.id = :projectId
          and s.deletedAt is null
          and s.startAt < :rangeEndExclusive
          and s.endAt >= :rangeStart
    """)
    List<ProjectSchedule> findOverlappingSchedules(
            @Param("projectId") Long projectId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive
    );

    // upcoming: from 이후 시작하는 일정
    List<ProjectSchedule> findAllByProjectIdAndDeletedAtIsNullAndStartAtGreaterThanEqualOrderByStartAtAsc(
            Long projectId,
            LocalDateTime from,
            Pageable pageable
    );
}
