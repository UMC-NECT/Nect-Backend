package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessFeedbackRepository extends JpaRepository<ProcessFeedback, Long> {
    Optional<ProcessFeedback> findByIdAndProcessIdAndDeletedAtIsNull(Long feedbackId, Long processId);
}

