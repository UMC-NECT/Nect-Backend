package com.nect.core.repository.team;

import com.nect.core.entity.team.SharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedDocumentRepository extends JpaRepository<SharedDocument, Long> {
    Optional<SharedDocument> findByIdAndProjectIdAndDeletedAtIsNull(Long id, Long projectId);
    Optional<SharedDocument> findByIdAndDeletedAtIsNull(Long id);
}
