package com.nect.core.repository.team.workspace;

import com.nect.core.entity.team.workspace.PostSharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PostSharedDocumentRepository extends JpaRepository<PostSharedDocument, Long> {
    boolean existsByPostIdAndDocumentIdAndDeletedAtIsNull(Long postId, Long documentId);

    Optional<PostSharedDocument> findByPostIdAndDocumentIdAndDeletedAtIsNull(Long postId, Long documentId);

    @Query("""
        select psd
        from PostSharedDocument psd
        join fetch psd.document d
        where psd.post.id = :postId
          and psd.deletedAt is null
    """)
    List<PostSharedDocument> findAllActiveByPostIdWithDocument(@Param("postId") Long postId);
}
