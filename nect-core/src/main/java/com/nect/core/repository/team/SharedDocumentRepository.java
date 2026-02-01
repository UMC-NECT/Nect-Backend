package com.nect.core.repository.team;

import com.nect.core.entity.team.SharedDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SharedDocumentRepository extends JpaRepository<SharedDocument, Long> {
    Optional<SharedDocument> findByIdAndProjectIdAndDeletedAtIsNull(Long id, Long projectId);
    Optional<SharedDocument> findByIdAndDeletedAtIsNull(Long id);

    // 프로젝트 내 공유문서함 프리뷰 조회
    @Query("""
        SELECT d
        FROM SharedDocument d
        JOIN FETCH d.createdBy u
        WHERE d.project.id = :projectId
          AND d.deletedAt IS NULL
        ORDER BY d.isPinned DESC, d.createdAt DESC, d.id DESC
    """)
    List<SharedDocument> findPreviewByProjectId(Long projectId, Pageable pageable);
}
