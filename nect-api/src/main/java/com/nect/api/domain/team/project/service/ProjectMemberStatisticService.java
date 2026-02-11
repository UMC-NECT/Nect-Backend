package com.nect.api.domain.team.project.service;

import com.nect.api.domain.team.project.dto.ProjectMemberStatisticResponse;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectUser;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 프로젝트 멤버 인원수 정보 service
@Service
@RequiredArgsConstructor
public class ProjectMemberStatisticService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;

    @Transactional(readOnly = true)
    public ProjectMemberStatisticResponse getStatistics(Long projectId) {
        // project 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

        // ProjectUser 조회
        List<ProjectUser> projectUsers = projectUserRepository.findByProject(project).stream()
                .filter(pu -> pu.getMemberStatus() == ProjectMemberStatus.ACTIVE)
                .toList();

        Map<Role, List<ProjectUser>> byRole = projectUsers.stream()
                .collect(Collectors.groupingBy(
                        pu -> {
                            Role role = pu.getRoleField().getRole();
                            return (role == null) ? Role.OTHER : role;
                        },
                        () -> new EnumMap<>(Role.class),
                        Collectors.toList()
                ));

        List<Role> roleOrder = List.of(
                Role.PLANNER,
                Role.DESIGNER,
                Role.DEVELOPER,
                Role.MARKETER,
                Role.OTHER
        );

        List<ProjectMemberStatisticResponse.RoleStatistic> roles = roleOrder.stream()
                .map(role -> {
                    List<ProjectUser> roleUsers = byRole.getOrDefault(role, List.of());
                    Map<RoleField, Long> roleFieldCounts = roleUsers.stream()
                            .collect(Collectors.groupingBy(ProjectUser::getRoleField, Collectors.counting()));

                    List<ProjectMemberStatisticResponse.RoleFieldStatistic> roleFields = Arrays.stream(RoleField.values())
                            .filter(rf -> {
                                Role rfRole = rf.getRole();
                                return ((rfRole == null) ? Role.OTHER : rfRole) == role;
                            })
                            .map(rf -> {
                                Long count = roleFieldCounts.get(rf);
                                if (count == null || count == 0) return null;
                                return new ProjectMemberStatisticResponse.RoleFieldStatistic(
                                        rf,
                                        rf.getLabelEn(),
                                        count.intValue()
                                );
                            })
                            .filter(rf -> rf != null)
                            .toList();

                    return new ProjectMemberStatisticResponse.RoleStatistic(
                            role,
                            roleUsers.size(),
                            roleFields
                    );
                })
                .toList();

        return new ProjectMemberStatisticResponse(roles);
    }
}
