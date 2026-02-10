package com.nect.api.domain.team.project.service;

import com.nect.api.domain.home.dto.HomeProjectMembersResponse;
import com.nect.api.domain.team.project.dto.ProjectUsersResDto;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberQueryService {
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    private String toPresignedUserImage(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) return null;
        return s3Service.getPresignedGetUrl(fileKey);
    }

    private String resolveLabel(RoleField rf, String customName) {
        if (rf == null) return null;
        return (rf == RoleField.CUSTOM) ? customName : rf.getLabelEn();
    }

    private <T> List<T> buildMembers(
            Long projectId,
            Long requesterUserId,
            boolean requireActiveMember,
            Function<MemberContext, T> mapper
    ) {
        if (requireActiveMember) {
            assertActiveProjectMember(projectId, requesterUserId);
        }

        List<ProjectUserRepository.MemberBoardRow> rows =
                projectUserRepository.findActiveMemberBoardRows(projectId);

        List<Long> ids = rows.stream()
                .map(ProjectUserRepository.MemberBoardRow::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        return rows.stream()
                .map(r -> {
                    User u = userMap.get(r.getUserId());
                    String profileUrl = (u == null) ? null : toPresignedUserImage(u.getProfileImageName());

                    RoleField rf = r.getRoleField();
                    String customName = r.getCustomRoleFieldName();
                    String label = resolveLabel(rf, customName);

                    return mapper.apply(new MemberContext(r, profileUrl, label));
                })
                .toList();
    }

    public record MemberContext(
            ProjectUserRepository.MemberBoardRow row,
            String profileUrl,
            String roleLabel
    ) {}

    // 본인이 속한 프로젝트에 대해서만 조회
    @Transactional(readOnly = true)
    public ProjectUsersResDto readProjectUsers(Long projectId, Long requesterUserId) {
        List<ProjectUsersResDto.UserDto> users = buildMembers(
                projectId,
                requesterUserId,
                true,
                ctx -> {
                    var r = ctx.row();
                    return new ProjectUsersResDto.UserDto(
                            r.getUserId(),
                            r.getName(),
                            r.getNickname(),
                            ctx.profileUrl(),
                            r.getBio(),
                            r.getRoleField(),
                            r.getCustomRoleFieldName(),
                            ctx.roleLabel(),
                            r.getMemberType()
                    );
                }
        );

        return new ProjectUsersResDto(users);
    }

    // 홈화면 -> 프로젝트 유저 목록 조회
    @Transactional(readOnly = true)
    public HomeProjectMembersResponse homeReadProjectUsers(Long projectId) {
        List<HomeProjectMembersResponse.UserInfo> users = buildMembers(
                projectId,
                null, // user 없음 it's okay...
                false, // false이면 user 없어도 됨.
                ctx -> {
                    var r = ctx.row();
                    return new HomeProjectMembersResponse.UserInfo(
                            r.getUserId(),
                            r.getName(),
                            r.getNickname(),
                            ctx.profileUrl(),
                            r.getBio(),
                            r.getRoleField(),
                            r.getCustomRoleFieldName(),
                            ctx.roleLabel(),
                            r.getMemberType()
                    );
                }
        );

        return new HomeProjectMembersResponse(users);
    }

    private void assertActiveProjectMember(Long projectId, Long userId) {
        boolean ok = projectUserRepository.existsByProjectIdAndUserIdAndMemberStatus(
                projectId, userId, ProjectMemberStatus.ACTIVE
        );
        if (!ok) {
            throw new ProjectException(
                    ProjectErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId
            );
        }
    }
}
