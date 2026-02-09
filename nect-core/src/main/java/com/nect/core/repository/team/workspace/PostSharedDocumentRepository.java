package com.nect.core.repository.team.workspace;

import com.nect.core.entity.team.workspace.PostSharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PostSharedDocument psd
           set psd.deletedAt = :deletedAt
         where psd.post.id = :postId
           and psd.deletedAt is null
    """)
    int softDeleteAllByPostId(@Param("postId") Long postId, @Param("deletedAt") LocalDateTime deletedAt);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update PostSharedDocument psd
       set psd.deletedAt = :deletedAt
     where psd.document.id = :documentId
       and psd.deletedAt is null
""")
    int softDeleteAllByDocumentId(@Param("documentId") Long documentId,
                                  @Param("deletedAt") LocalDateTime deletedAt);
}
