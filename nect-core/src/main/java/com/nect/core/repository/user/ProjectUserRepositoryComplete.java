package com.nect.core.repository.user;

import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectUserRepositoryComplete extends JpaRepository<ProjectUser, Long> {

    /**
     * 특정 사용자의 프로젝트 참여 목록 조회 (상태별)
     */
    @Query("SELECT pu FROM ProjectUser pu " +
            "JOIN FETCH pu.project " +
            "WHERE pu.userId = :userId " +
            "AND pu.memberStatus = :memberStatus")
    List<ProjectUser> findByUserIdAndMemberStatus(
            @Param("userId") Long userId,
            @Param("memberStatus") ProjectMemberStatus memberStatus);

    /**
     * 특정 프로젝트의 모든 참여자 조회 (상태별)
     */
    @Query("SELECT pu FROM ProjectUser pu " +
            "WHERE pu.project.id = :projectId " +
            "AND pu.memberStatus = :memberStatus")
    List<ProjectUser> findByProjectIdAndMemberStatus(
            @Param("projectId") Long projectId,
            @Param("memberStatus") ProjectMemberStatus memberStatus);

    /**
     * 특정 프로젝트의 리더 조회
     */
    @Query("SELECT pu FROM ProjectUser pu " +
            "WHERE pu.project.id = :projectId " +
            "AND pu.memberType = :memberType")
    Optional<ProjectUser> findByProjectIdAndMemberType(
            @Param("projectId") Long projectId,
            @Param("memberType") ProjectMemberType memberType);

    /**
     * 여러 사용자의 프로젝트 참여 목록 조회 (상태별)
     */
    @Query("SELECT pu FROM ProjectUser pu " +
            "JOIN FETCH pu.project " +
            "WHERE pu.userId IN :userIds " +
            "AND pu.memberStatus = :memberStatus")
    List<ProjectUser> findByUserIdInAndMemberStatus(
            @Param("userIds") List<Long> userIds,
            @Param("memberStatus") ProjectMemberStatus memberStatus);

    /**
     * 여러 프로젝트의 모든 참여자 조회 (상태별) - 최적화용
     */
    @Query("SELECT pu FROM ProjectUser pu " +
            "JOIN FETCH pu.project " +
            "WHERE pu.project.id IN :projectIds " +
            "AND pu.memberStatus = :memberStatus")
    List<ProjectUser> findByProjectIdInAndMemberStatus(
            @Param("projectIds") List<Long> projectIds,
            @Param("memberStatus") ProjectMemberStatus memberStatus);

    /**
     * 여러 프로젝트의 리더 조회 - 최적화용
     */
    @Query("SELECT pu FROM ProjectUser pu " +
            "JOIN FETCH pu.project " +
            "WHERE pu.project.id IN :projectIds " +
            "AND pu.memberType = :memberType")
    List<ProjectUser> findByProjectIdInAndMemberType(
            @Param("projectIds") List<Long> projectIds,
            @Param("memberType") ProjectMemberType memberType);
}
