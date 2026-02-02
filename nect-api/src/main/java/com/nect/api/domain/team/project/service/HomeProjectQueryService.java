package com.nect.api.domain.team.project.service;

import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.enums.RecruitmentStatus;
import com.nect.core.entity.user.User;
import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 홈화면에 필요한 정보를 가져오는 service입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeProjectQueryService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;

    public record HomeProjectBatch(
            Map<Long, User> authorByProjectId,
            Map<Long, Integer> activeCountByProjectId,
            Map<Long, Integer> maxMemberCountByProjectId,
            Map<Long, Map<String, Integer>> partCountsByProjectId
    ) {}

    public HomeProjectBatch loadHomeProjectBatch(List<Project> projects) {
        if (projects.isEmpty()) {
            return new HomeProjectBatch(Map.of(), Map.of(), Map.of(), Map.of());
        }

        List<Long> projectIds = projects.stream().map(Project::getId).toList();

        Map<Long, Long> leaderUserIdByProjectId = projectUserRepository.findLeadersByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        ProjectUserRepository.ProjectLeaderRow::getProjectId,
                        ProjectUserRepository.ProjectLeaderRow::getLeaderUserId
                ));

        List<Long> leaderUserIds = leaderUserIdByProjectId.values().stream().distinct().toList();
        Map<Long, User> userById = userRepository.findByUserIdIn(leaderUserIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<Long, User> authorByProjectId = leaderUserIdByProjectId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> userById.get(e.getValue())
                ));

        Map<Long, Integer> activeCountByProjectId = projectUserRepository.countActiveMembersByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        ProjectUserRepository.ProjectActiveCountRow::getProjectId,
                        r -> r.getActiveCount().intValue()
                ));

        Map<Long, Integer> maxMemberCountByProjectId = recruitmentRepository.sumCapacityByProjectIds(projectIds).stream()
                .collect(Collectors.toMap(
                        RecruitmentRepository.ProjectCapacityRow::getProjectId,
                        r -> r.getCapacitySum() == null ? 0 : r.getCapacitySum()
                ));

        Map<Long, Map<String, Integer>> partCountsByProjectId = new HashMap<>();
//        for (RecruitmentRepository.ProjectRoleCapacityRow row : recruitmentRepository.sumRoleCapacityByProjectIds(projectIds)) { // TODO: Recruitmnet Field 바뀌면 적용
//            partCountsByProjectId
//                    .computeIfAbsent(row.getProjectId(), k -> new HashMap<>())
//                    .put(row.getRoleName(), row.getCapacitySum() == null ? 0 : row.getCapacitySum());
//        }

        return new HomeProjectBatch(
                authorByProjectId,
                activeCountByProjectId,
                maxMemberCountByProjectId,
                null // TODO: Recruitmnet Field 바뀌면 적용
//                partCountsByProjectId
        );
    }

    public List<Project> getProjects(Long userId, PageRequest pageRequest){
        return (userId == null)
                ? projectRepository.findHomeProjectsWithoutUser(RecruitmentStatus.OPEN, pageRequest)
                : projectRepository.findHomeProjects(userId, RecruitmentStatus.OPEN, pageRequest);
    }

    public List<Project> getProjects(Long userId) {
        return (userId == null)
                ? projectRepository.findHomeProjectsWithoutUser(RecruitmentStatus.OPEN)
                : projectRepository.findHomeProjects(userId, RecruitmentStatus.OPEN);
    }

    public Integer getDDay(Project project) {
        LocalDateTime endedAt = project.getEndedAt();
        LocalDate today = LocalDate.now();
        LocalDate endDate = endedAt.toLocalDate();
        return (int) ChronoUnit.DAYS.between(today, endDate);
    }
}




