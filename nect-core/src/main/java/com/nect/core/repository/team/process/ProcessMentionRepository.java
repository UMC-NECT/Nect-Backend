package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProcessMentionRepository extends JpaRepository<ProcessMention, Long> {
    @Query("select m from ProcessMention m where m.process.id = :processId")
    List<ProcessMention> findAllByProcessId(@Param("processId") Long processId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ProcessMention m
           set m.deletedAt = :now
         where m.process.id = :processId
           and m.deletedAt is null
    """)
    int softDeleteAllByProcessId(@Param("processId") Long processId,
                                 @Param("now") LocalDateTime now);
}
