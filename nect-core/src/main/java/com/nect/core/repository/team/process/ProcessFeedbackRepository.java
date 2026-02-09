package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessFeedback;
import com.nect.core.entity.team.process.enums.ProcessFeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessFeedbackRepository extends JpaRepository<ProcessFeedback, Long> {
    Optional<ProcessFeedback> findByIdAndProcessIdAndDeletedAtIsNull(Long feedbackId, Long processId);

    @Query("""
        select distinct f.process.id
        from ProcessFeedback f
        where f.deletedAt is null
          and f.status = :status
          and f.process.id in :processIds
    """)
    List<Long> findProcessIdsHavingStatusIn(
            @Param("processIds") List<Long> processIds,
            @Param("status") ProcessFeedbackStatus status
    );

    @Query("""
        select distinct f.process.id
        from ProcessFeedback f
        where f.deletedAt is null
          and f.status = com.nect.core.entity.team.process.enums.ProcessFeedbackStatus.OPEN
          and f.process.id in :processIds
    """)
    List<Long> findOpenFeedbackProcessIds(@Param("processIds") List<Long> processIds);

}

