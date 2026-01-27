package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessFeedbackRepository extends JpaRepository<ProcessFeedback, Integer> {
    Optional<ProcessFeedback> findByIdAndProcessId(Long feedbackId, Long processId);
}

