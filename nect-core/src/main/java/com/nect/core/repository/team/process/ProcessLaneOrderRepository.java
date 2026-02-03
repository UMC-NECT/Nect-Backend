package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessLaneOrder;
import com.nect.core.entity.team.process.enums.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessLaneOrderRepository extends JpaRepository<ProcessLaneOrder, Long> {

    @Query("""
        select plo
        from ProcessLaneOrder plo
        where plo.projectId = :projectId
          and plo.deletedAt is null
          and plo.laneKey = :laneKey
          and plo.status = :status
        order by plo.sortOrder asc, plo.id asc
    """)
    List<ProcessLaneOrder> findLaneOrders(
            @Param("projectId") Long projectId,
            @Param("laneKey") String laneKey,
            @Param("status") ProcessStatus status
    );

    @Query("""
        select count(plo)
        from ProcessLaneOrder plo
        where plo.projectId = :projectId
          and plo.deletedAt is null
          and plo.laneKey = :laneKey
          and plo.status = :status
    """)
    long countLaneTotal(
            @Param("projectId") Long projectId,
            @Param("laneKey") String laneKey,
            @Param("status") ProcessStatus status
    );

    Optional<ProcessLaneOrder> findByProjectIdAndProcessIdAndLaneKeyAndStatusAndDeletedAtIsNull(
            Long projectId, Long processId, String laneKey, ProcessStatus status
    );

    @Query("""
        select plo
        from ProcessLaneOrder plo
        where plo.projectId = :projectId
          and plo.deletedAt is null
          and plo.laneKey = :laneKey
          and plo.status = :status
          and plo.process.id in :processIds
    """)
    List<ProcessLaneOrder> findAllByLaneAndProcessIds(
            @Param("projectId") Long projectId,
            @Param("laneKey") String laneKey,
            @Param("status") ProcessStatus status,
            @Param("processIds") List<Long> processIds
    );

    @Query("""
        select plo
        from ProcessLaneOrder plo
        where plo.projectId = :projectId
          and plo.deletedAt is null
          and plo.process.id = :processId
          and plo.laneKey = :laneKey
    """)
    List<ProcessLaneOrder> findAllActiveByLaneKey(
            @Param("projectId") Long projectId,
            @Param("processId") Long processId,
            @Param("laneKey") String laneKey
    );

    @Query("""
        select plo
        from ProcessLaneOrder plo
        where plo.projectId = :projectId
          and plo.deletedAt is null
          and plo.process.id = :processId
    """)
    List<ProcessLaneOrder> findAllActiveByProcess(
            @Param("projectId") Long projectId,
            @Param("processId") Long processId
    );
}
