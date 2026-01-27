package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessSharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessSharedDocumentRepository extends JpaRepository<ProcessSharedDocument, Long> {
    Optional<ProcessSharedDocument> findByProcessIdAndDocumentId(Long processId, Long documentId);
    boolean existsByProcessIdAndDocumentId(Long processId, Long documentId);
}
