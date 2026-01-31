package com.nect.core.repository.team.workspace;

import com.nect.core.entity.team.workspace.PostMention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMentionRepository extends JpaRepository<PostMention, Long> {
    List<PostMention> findAllByPostId(Long postId);
}
