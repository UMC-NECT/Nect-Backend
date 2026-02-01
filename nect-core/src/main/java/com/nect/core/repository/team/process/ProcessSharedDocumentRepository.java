package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessSharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessSharedDocumentRepository extends JpaRepository<ProcessSharedDocument, Long> {
    Optional<ProcessSharedDocument> findByProcessIdAndDocumentIdAndDeletedAtIsNull(Long processId, Long documentId);
    boolean existsByProcessIdAndDocumentIdAndDeletedAtIsNull(Long processId, Long documentId);
}
