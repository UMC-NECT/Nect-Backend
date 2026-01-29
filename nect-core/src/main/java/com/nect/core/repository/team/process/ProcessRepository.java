package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProcessRepository extends JpaRepository<Process, Long> {

    // 소속 검증 + 소프트 delete 제외
    Optional<Process> findByIdAndProjectIdAndDeletedAtIsNull(Long id, Long projectId);

    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and (
                (p.startAt is null and p.endAt is null)
             or (p.startAt is null and p.endAt >= :start)
             or (p.endAt is null and p.startAt <= :end)
             or (p.startAt <= :end and p.endAt >= :start)
          )
        order by
          case when p.startAt is null then 1 else 0 end asc,
          p.startAt asc,
          case when p.endAt is null then 1 else 0 end asc,
          p.endAt asc,
          p.statusOrder asc,
          p.id asc
        """)
    List<Process> findAllInRangeOrdered(
            @Param("projectId") Long projectId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.id in :ids
    """)
    List<Process> findAllByIdsInProject(
            @Param("projectId") Long projectId,
            @Param("ids") List<Long> ids
    );


    /**
     *  Team 보드(공통) 조회
     * - "모든 팀의 작업들을 전부 확인" => 필드/파트 관계없이 전체 프로세스 조회
     */
    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
        order by p.status asc, p.statusOrder asc, p.id asc
    """)
    List<Process> findAllForTeamBoard(@Param("projectId") Long projectId);

    /**
     * 파트 보드 조회:
     * - 지정된 fieldId가 매핑된 프로세스만 조회한다.
     *
     * TODO(Field 연동 후):
     * - ProcessField의 필드명이 확정되면 아래 조건을 실제 구조에 맞게 수정
     *   예1) pf.fieldId = :fieldId
     *   예2) pf.field.id = :fieldId
     */
    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and exists (
                select 1
                from ProcessField pf
                where pf.process = p
                  and pf.fieldId = :fieldId
          )
        order by p.status asc, p.statusOrder asc, p.id asc
    """)
    List<Process> findAllForPartBoard(@Param("projectId") Long projectId,
                                      @Param("fieldId") Long fieldId);


    int countByProjectIdAndDeletedAtIsNullAndStatus(Long projectId, ProcessStatus status);
}

