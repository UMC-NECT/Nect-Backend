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

    @EntityGraph(attributePaths = { "processUsers", "processUsers.user" })
    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
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
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
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
    @EntityGraph(attributePaths = { "processUsers", "processUsers.user" })
    @Query("""
        select p
        from Process p
        left join ProcessLaneOrder o
          on o.process = p
         and o.projectId = :projectId
         and o.laneKey = 'TEAM'
         and o.status = p.status
         and o.deletedAt is null
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
        order by
          p.status asc,
          coalesce(o.sortOrder, 999999) asc,
          p.id asc
    """)
    List<Process> findAllForTeamBoard(@Param("projectId") Long projectId);

    // ROLE 레인: 조건에 맞는 Process ID만
    @Query("""
        select distinct p.id
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and pf.deletedAt is null
          and pf.roleField = :roleField
    """)
    List<Long> findRoleLaneIds(
            @Param("projectId") Long projectId,
            @Param("roleField") RoleField roleField
    );

    // CUSTOM 레인: 조건에 맞는 Process ID만
    @Query("""
        select distinct p.id
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and pf.deletedAt is null
          and pf.roleField = com.nect.core.entity.user.enums.RoleField.CUSTOM
          and trim(pf.customFieldName) = :customName
    """)
    List<Long> findCustomLaneIds(
            @Param("projectId") Long projectId,
            @Param("customName") String customName
    );

    /**
     * 파트 보드 조회:
     * - 지정된 roleField가 매핑된 프로세스만 조회한다.
     */
    // 유저용
    @EntityGraph(attributePaths = { "processUsers", "processUsers.user" })
    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and p.id in :ids
    """)
    List<Process> findAllByIdsInProjectWithUsers(
            @Param("projectId") Long projectId,
            @Param("ids") List<Long> ids
    );

    @EntityGraph(attributePaths = { "processFields" })
    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and p.id in :ids
    """)
    List<Process> findAllByIdsInProjectWithFields(
            @Param("projectId") Long projectId,
            @Param("ids") List<Long> ids
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
          AND (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
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
          AND (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
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
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
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
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
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

    // status 내 TEAM 전체 (GENERAL만)
    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and p.status = :status
    """)
    List<Process> findAllByStatusInProject(
            @Param("projectId") Long projectId,
            @Param("status") ProcessStatus status
    );

    // status 내 ROLE lane 전체 (GENERAL만)
    @Query("""
        select distinct p
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and p.status = :status
          and pf.deletedAt is null
          and pf.roleField = :roleField
    """)
    List<Process> findAllInRoleLaneByStatus(
            @Param("projectId") Long projectId,
            @Param("status") ProcessStatus status,
            @Param("roleField") RoleField roleField
    );


    // status 내 CUSTOM lane 전체 (GENERAL만)
    @Query("""
        select distinct p
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and p.status = :status
          and pf.deletedAt is null
          and pf.roleField = com.nect.core.entity.user.enums.RoleField.CUSTOM
          and trim(pf.customFieldName) = :customName
    """)
    List<Process> findAllInCustomLaneByStatus(
            @Param("projectId") Long projectId,
            @Param("status") ProcessStatus status,
            @Param("customName") String customName
    );

    // 프로젝트에 존재하는 lane 목록(ROLE + CUSTOM 이름)
    interface LaneKeyRow {
        RoleField getRoleField();
        String getCustomFieldName();
    }

    @Query("""
        select distinct
          pf.roleField as roleField,
          pf.customFieldName as customFieldName
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and pf.deletedAt is null
          and pf.roleField is not null
    """)
    List<LaneKeyRow> findLaneKeysInProject(@Param("projectId") Long projectId);

    int countByProjectIdAndDeletedAtIsNullAndStatus(Long projectId, ProcessStatus status);


    /**
     * TEAM lane total (GENERAL만)
     */
    @Query("""
        select count(p)
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.status = :status
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
    """)
    int countTeamLaneTotalExcludingWeekMission(
            @Param("projectId") Long projectId,
            @Param("status") ProcessStatus status
    );

    // ROLE lane total
    @Query("""
        select count(distinct p)
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and p.status = :status
          and pf.deletedAt is null
          and pf.roleField = :roleField
    """)
    int countRoleLaneTotal(
            @Param("projectId") Long projectId,
            @Param("status") ProcessStatus status,
            @Param("roleField") RoleField roleField
    );

    // CUSTOM lane total
    @Query("""
        select count(distinct p)
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and p.status = :status
          and pf.deletedAt is null
          and pf.roleField = com.nect.core.entity.user.enums.RoleField.CUSTOM
          and trim(pf.customFieldName) = :customName
    """)
    int countCustomLaneTotal(
            @Param("projectId") Long projectId,
            @Param("status") ProcessStatus status,
            @Param("customName") String customName
    );

    // WEEK_MISSION 상세(체크리스트 포함)
    @EntityGraph(attributePaths = { "taskItems", "createdBy" })
    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.id = :processId
          and p.deletedAt is null
          and p.processType = com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION
    """)
    Optional<Process> findWeekMissionDetail(
            @Param("projectId") Long projectId,
            @Param("processId") Long processId
    );


    // WEEK_MISSION 주차별 조회
    interface WeekMissionCardRow {
        Long getProcessId();
        Integer getMissionNumber();
        ProcessStatus getStatus();
        String getTitle();
        LocalDate getStartDate();
        LocalDate getDeadLine();
        Long getDoneCount();
        Long getTotalCount();
        Long getLeaderUserId();
        String getLeaderNickname();
        String getLeaderProfileImageUrl();
    }

    @Query("""
        select
          p.id as processId,
          p.missionNumber as missionNumber,
          p.status as status,
          p.title as title,
          p.startAt as startDate,
          p.endAt as deadLine,
          sum(case when ti.isDone = true then 1 else 0 end) as doneCount,
          count(ti.id) as totalCount,
          u.userId as leaderUserId,
          u.nickname as leaderNickname,
          u.profileImageUrl as leaderProfileImageUrl
        from Process p
        left join p.taskItems ti on ti.deletedAt is null
        left join p.processUsers pu
               on pu.deletedAt is null
              and pu.assignmentRole = com.nect.core.entity.team.process.enums.AssignmentRole.ASSIGNEE
        left join pu.user u
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.processType = com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION
          and (
                (p.startAt is null and p.endAt is null)
             or (p.startAt is null and p.endAt >= :start)
             or (p.endAt is null and p.startAt <= :end)
             or (p.startAt <= :end and p.endAt >= :start)
          )
        group by p.id, p.missionNumber, p.status, p.title, p.startAt, p.endAt,
                 u.userId, u.nickname, u.profileImageUrl
        order by p.missionNumber asc nulls last, p.startAt asc nulls last, p.id asc
    """)
    List<WeekMissionCardRow> findWeekMissionCardsInRange(
            @Param("projectId") Long projectId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );



    // WEEK_MISSION 중 가장 이른 startAt
    @Query("""
        select min(p.startAt)
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.processType = com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION
          and p.startAt is not null
    """)
    LocalDate findMinWeekMissionStartAt(@Param("projectId") Long projectId);

    // 전체 프로세스 중 가장 이른 startAt (GENERAL + WEEK_MISSION 포함)
    @Query("""
        select min(p.startAt)
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.startAt is not null
    """)
    LocalDate findMinProcessStartAt(@Param("projectId") Long projectId);

    @Query("""
        select p
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.processType = com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION
          and p.startAt <= :date
          and p.endAt >= :date
    """)
    Optional<Process> findWeekMissionContainingDate(
            @Param("projectId") Long projectId,
            @Param("date") LocalDate date
    );

    @Query("""
        select (count(p) > 0)
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and pf.deletedAt is null
          and pf.roleField = :roleField
          and p.startAt <= :end
          and p.endAt >= :start
    """)
    boolean existsOverlappingInRoleLane(
            @Param("projectId") Long projectId,
            @Param("roleField") RoleField roleField,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        select (count(p) > 0)
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and (p.processType is null or p.processType <> com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION)
          and pf.deletedAt is null
          and pf.roleField = com.nect.core.entity.user.enums.RoleField.CUSTOM
          and trim(pf.customFieldName) = :customName
          and p.startAt <= :end
          and p.endAt >= :start
    """)
    boolean existsOverlappingInCustomLane(
            @Param("projectId") Long projectId,
            @Param("customName") String customName,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    interface MissionPeriodRow {
        LocalDate getStartAt();
        LocalDate getEndAt();
    }

    @Query("""
        select p.startAt as startAt, p.endAt as endAt
        from Process p
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.processType = com.nect.core.entity.team.process.enums.ProcessType.WEEK_MISSION
          and p.missionNumber = :missionNumber
    """)
    Optional<MissionPeriodRow> findWeekMissionPeriodByMissionNumber(
            @Param("projectId") Long projectId,
            @Param("missionNumber") Integer missionNumber
    );

    @Query("""
        select case when count(p) > 0 then true else false end
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.id <> :excludeProcessId
          and pf.deletedAt is null
          and pf.roleField = :roleField
          and not (p.endAt < :start or p.startAt > :end)
    """)
    boolean existsOverlappingInRoleLaneExcludingProcess(
            @Param("projectId") Long projectId,
            @Param("roleField") RoleField roleField,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludeProcessId") Long excludeProcessId
    );

    @Query("""
        select case when count(p) > 0 then true else false end
        from Process p
        join p.processFields pf
        where p.project.id = :projectId
          and p.deletedAt is null
          and p.id <> :excludeProcessId
          and pf.deletedAt is null
          and pf.roleField = com.nect.core.entity.user.enums.RoleField.CUSTOM
          and pf.customFieldName = :customName
          and not (p.endAt < :start or p.startAt > :end)
    """)
    boolean existsOverlappingInCustomLaneExcludingProcess(
            @Param("projectId") Long projectId,
            @Param("customName") String customName,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludeProcessId") Long excludeProcessId
    );

}

