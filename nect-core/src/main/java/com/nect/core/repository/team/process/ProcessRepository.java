package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import com.nect.core.entity.user.enums.RoleField;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProcessRepository extends JpaRepository<Process, Long> {

    // 소속 검증 + 소프트 delete 제외
    Optional<Process> findByIdAndProjectIdAndDeletedAtIsNull(Long id, Long projectId);

    @EntityGraph(attributePaths = {
            "taskItems",
            "processFields",
            "processUsers",
            "processUsers.user"
    })
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
    @EntityGraph(attributePaths = {
            "taskItems",
            "processFields",
            "processUsers",
            "processUsers.user"
    })
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
     * - 지정된 roleField가 매핑된 프로세스만 조회한다.
     */
    @EntityGraph(attributePaths = {
            "taskItems",
            "processFields",
            "processUsers",
            "processUsers.user"
    })
    @Query("""
        select distinct p
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and pf.deletedAt is null
          and pf.roleField = :roleField
        order by p.status asc, p.statusOrder asc, p.id asc
    """)
    List<Process> findAllForRoleLaneBoard(
            @Param("projectId") Long projectId,
            @Param("roleField") RoleField roleField
    );

    @EntityGraph(attributePaths = {
            "taskItems",
            "processFields",
            "processUsers",
            "processUsers.user"
    })
    @Query("""
        select distinct p
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and pf.deletedAt is null
          and pf.roleField = com.nect.core.entity.user.enums.RoleField.CUSTOM
          and pf.customFieldName = :customName
        order by p.status asc, p.statusOrder asc, p.id asc
    """)
    List<Process> findAllForCustomLaneBoard(
            @Param("projectId") Long projectId,
            @Param("customName") String customName
    );


    int countByProjectIdAndDeletedAtIsNullAndStatus(Long projectId, ProcessStatus status);

    @Query("""
        select count(distinct p)
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.status = :status
          and pf.deletedAt is null
          and pf.roleField = :roleField
    """)
    int countRoleLaneTotal(
            @Param("projectId") Long projectId,
            @Param("status") ProcessStatus status,
            @Param("roleField") RoleField roleField
    );

    @Query("""
        select count(distinct p)
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.status = :status
          and pf.deletedAt is null
          and pf.roleField = com.nect.core.entity.user.enums.RoleField.CUSTOM
          and pf.customFieldName = :customName
    """)
    int countCustomLaneTotal(
            @Param("projectId") Long projectId,
            @Param("status") ProcessStatus status,
            @Param("customName") String customName
    );

    interface MissionProgressRow {
        RoleField getRoleField();
        String getCustomFieldName(); // roleField == CUSTOM 일 때 값
        Long getTotalCount();
        Long getCompletedCount();
    }

    @Query("""
        SELECT
            pf.roleField AS roleField,
            pf.customFieldName AS customFieldName,
            COUNT(DISTINCT p.id) AS totalCount,
            SUM(CASE WHEN p.status = com.nect.core.entity.team.process.enums.ProcessStatus.DONE THEN 1 ELSE 0 END) AS completedCount
        FROM Process p
        JOIN p.processFields pf
        WHERE p.project.id = :projectId
          AND p.deletedAt IS NULL
          AND pf.deletedAt IS NULL
        GROUP BY pf.roleField, pf.customFieldName
    """)
    List<MissionProgressRow> aggregateMissionProgress(@Param("projectId") Long projectId);

    interface MemberProcessCountRow {
        Long getUserId();
        ProcessStatus getStatus();
        Long getCnt();
    }


    @Query("""
        SELECT
          pu.user.userId AS userId,
          p.status AS status,
          COUNT(DISTINCT p.id) AS cnt
        FROM Process p
        JOIN p.processUsers pu
        WHERE p.project.id = :projectId
          AND p.deletedAt IS NULL
          AND pu.deletedAt IS NULL
        GROUP BY pu.user.userId, p.status
        """)
    List<MemberProcessCountRow> aggregateMemberProcessCounts(@Param("projectId") Long projectId);


    interface LaneStatusCountRow {
        RoleField getRoleField();
        String getCustomName();
        ProcessStatus getStatus();
        long getCnt();
    }

    // ROLE 레인 집계 (CUSTOM 제외)
    @Query("""
        select
            pf.roleField as roleField,
            null as customName,
            p.status as status,
            count(distinct p.id) as cnt
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and pf.deletedAt is null
          and pf.roleField is not null
          and pf.roleField <> :custom
          and p.status in :statuses
        group by pf.roleField, p.status
    """)
    List<LaneStatusCountRow> countRoleLaneStatusForProgressSummary(
            @Param("projectId") Long projectId,
            @Param("custom") RoleField custom,
            @Param("statuses") List<ProcessStatus> statuses
    );

    // CUSTOM 레인 집계 (customFieldName별)
    @Query("""
        select
            :custom as roleField,
            trim(pf.customFieldName) as customName,
            p.status as status,
            count(distinct p.id) as cnt
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and pf.deletedAt is null
          and pf.roleField = :custom
          and pf.customFieldName is not null
          and trim(pf.customFieldName) <> ''
          and p.status in :statuses
        group by trim(pf.customFieldName), p.status
    """)
    List<LaneStatusCountRow> countCustomLaneStatusForProgressSummary(
            @Param("projectId") Long projectId,
            @Param("custom") RoleField custom,
            @Param("statuses") List<ProcessStatus> statuses
    );
}

