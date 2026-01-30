package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessMentionRepository extends JpaRepository<ProcessMention, Long> {
    @Query("select m from ProcessMention m where m.process.id = :processId")
    List<ProcessMention> findAllByProcessId(@Param("processId") Long processId);
}
