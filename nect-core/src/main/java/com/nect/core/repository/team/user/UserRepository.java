package com.nect.core.repository.team.user;

import com.nect.core.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// TODO 임시 리포지토리 수정 필요
@Repository

//TODO 프로젝트 관련 엔티티에 따라 수정
public interface UserRepository extends JpaRepository<User,Long> {
    // 같은 프로젝트 내 사용자 검색 (이름으로)
    @Query("""
        SELECT u FROM User u
        WHERE u.projectId = :projectId
        AND u.username LIKE %:keyword%
    """)
    List<User> searchUsersByProjectAndName(
            @Param("projectId") Long projectId,
            @Param("keyword") String keyword
    );

    //TODO 프로젝트 관련 엔티티에 따라 수정
    List<User> findByProjectId(Long projectId);

    //TODO 프로젝트 관련 엔티티에 따라 수정
    List<User> findAllByProjectId(Long projectId);
}
