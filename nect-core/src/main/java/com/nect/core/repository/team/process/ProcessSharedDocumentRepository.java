package com.nect.core.repository.team.process;

import com.nect.core.entity.team.process.ProcessSharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessSharedDocumentRepository extends JpaRepository<ProcessSharedDocument, Long> {
    Optional<ProcessSharedDocument> findByProcessIdAndDocumentIdAndDeletedAtIsNull(Long processId, Long documentId);
    boolean existsByProcessIdAndDocumentIdAndDeletedAtIsNull(Long processId, Long documentId);

    @Modifying
    @Query("""
        update ProcessSharedDocument psd
        set psd.deletedAt = CURRENT_TIMESTAMP
        where psd.process.project.id = :projectId
          and psd.document.id = :documentId
          and psd.deletedAt is null
    """)
    int softDeleteAllAttachments(@Param("projectId") Long projectId, @Param("documentId") Long documentId);

    @Query("""
        select psd
        from ProcessSharedDocument psd
        join fetch psd.document d
        where psd.process.id = :processId
          and psd.deletedAt is null
          and d.deletedAt is null
    """)
    List<ProcessSharedDocument> findAliveAttachmentsWithDoc(@Param("processId") Long processId);
}
