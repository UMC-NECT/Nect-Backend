package com.nect.core.repository.team.process;

import com.nect.core.entity.team.enums.DocumentType;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.team.process.ProcessSharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    interface AttachmentAggRow {
        Long getProcessId();
        DocumentType getDocumentType();
        FileExt getFileExt();
        Long getCnt();
    }

    @Query("""
        select
            psd.process.id as processId,
            doc.documentType as documentType,
            doc.fileExt as fileExt,
            count(psd.id) as cnt
        from ProcessSharedDocument psd
        join psd.document doc
        where psd.deletedAt is null
          and doc.deletedAt is null
          and psd.process.id in :processIds
        group by psd.process.id, doc.documentType, doc.fileExt
    """)
    List<AttachmentAggRow> aggregateAttachmentsByProcessIds(@Param("processIds") List<Long> processIds);

    interface AttachmentMetaRow {
        Long getProcessId();
        Long getDocumentId();
        DocumentType getDocumentType();
        FileExt getFileExt();
        LocalDateTime getAttachedAt();
        LocalDateTime getCreatedAt();
    }

    @Query("""
        select
            psd.process.id as processId,
            d.id as documentId,
            d.documentType as documentType,
            d.fileExt as fileExt,
            psd.attachedAt as attachedAt,
            psd.createdAt as createdAt
        from ProcessSharedDocument psd
        join psd.document d
        where psd.deletedAt is null
          and d.deletedAt is null
          and psd.process.id in :processIds
    """)
    List<AttachmentMetaRow> findAttachmentMetasByProcessIds(@Param("processIds") List<Long> processIds);

    @Query("""
        select (count(psd) > 0)
        from ProcessSharedDocument psd
        where psd.document.id = :documentId
          and psd.deletedAt is null
    """)
    boolean existsActiveByDocumentId(@Param("documentId") Long documentId);
}
