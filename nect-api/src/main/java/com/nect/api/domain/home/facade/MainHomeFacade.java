package com.nect.api.domain.home.facade;

import com.nect.api.domain.home.dto.HomeMemberItem;
import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectItem;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import com.nect.api.domain.home.exception.HomeInvalidParametersException;
import com.nect.api.domain.home.service.HomeMemberQueryService;
import com.nect.api.domain.home.service.HomeProjectQueryService;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.entity.user.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 메인화면에 나오는 부분에 대한 Facade입니다.
 * team에 있는 service에서 정보를 가져와 조합합니다.
 */
@Service
@RequiredArgsConstructor
public class MainHomeFacade {

    private final HomeProjectQueryService homeQueryService;
    private final HomeMemberQueryService homeMemberQueryService;

    // 모집 중인 프로젝트
    public HomeProjectResponse getRecruitingProjects(Long userId, int count){
        PageRequest pageRequest = PageRequest.of(0, count);
        List<Project> projects = homeQueryService.getProjects(userId, pageRequest);

        if (projects.isEmpty()) {
            return new HomeProjectResponse(List.of());
        }

        return new HomeProjectResponse(responsesFromProjects(projects));
    }

    // 홈화면 추천 프로젝트들
    public HomeProjectResponse getRecommendedProjects(Long userId, int count) {
        List<Project> projects = homeQueryService.getProjects(userId);

        if (projects.isEmpty()) {
            return new HomeProjectResponse(List.of());
        }

        Collections.shuffle(projects);
        List<Project> randomProjects = projects.subList(0, Math.min(count, projects.size()));

        return new HomeProjectResponse(responsesFromProjects(randomProjects));
    }

    // 홈화면 매칭 가능한 넥터
    public HomeMembersResponse getMatchableMembers(Long userId, int count, Role role, InterestField interset) {

        // 둘 중 하나가 null일 수는 없음
        if ((role == null && interset != null) || (role != null && interset == null)) {
            throw new HomeInvalidParametersException("role과 interest 중 하나만 null일 수 없습니다.");
        }

        // List 선언
        List<User> users;

        if (role != null) { // 둘 다 null이 아니면 필터링해서 반환
            users = homeMemberQueryService.getFilteredMembers(userId, count, role, interset);
        }
        else{ // 둘 모두 null이면 모두 조회하여 반환
            users = homeMemberQueryService.getAllUsersWithoutUser(userId, count);
        }

        return new HomeMembersResponse(responsesFromMembers(users));
    }

    // 홈화면 추천 넥터
    public HomeMembersResponse getRecommendedMembers(Long userId, int count) {
        List<User> users = homeMemberQueryService.getAllUsersWithoutUser(userId, count);

        List<HomeMemberItem> items = new ArrayList<>(responsesFromMembers(users));
        Collections.shuffle(items);

        return new HomeMembersResponse(items);
    }

    // List<Project> -> List<HomeProjectItem>
    private List<HomeProjectItem> responsesFromProjects(List<Project> projects) {
        HomeProjectQueryService.HomeProjectBatch batch = homeQueryService.loadHomeProjectBatch(projects);

        return projects.stream()
                .map(p -> {
                    Long projectId = p.getId();

                    User author = batch.authorByProjectId().get(projectId);
                    Integer dDay = homeQueryService.getDDay(p);
                    Integer maxMemberCount = batch.maxMemberCountByProjectId().getOrDefault(projectId, 0);
                    Integer currentMemberCount = batch.activeCountByProjectId().getOrDefault(projectId, 0);
                    Map<String, Integer> partCounts = batch.partCountsByProjectId().getOrDefault(projectId, Map.of());

                    return new HomeProjectItem(
                            projectId,
                            null,
                            p.getTitle(),
                            author == null ? null : author.getName(),
                            null,
                            p.getDescription(),
                            dDay,
                            maxMemberCount,
                            currentMemberCount,
                            false,
                            p.getRecruitmentStatus().getStatus(),
                            partCounts
                    );
                })
                .toList();
    }

    // List<User> users -> List<HomeMemberItem>
    private List<HomeMemberItem> responsesFromMembers(List<User> users) {
        Map<Long, List<String>> partsByUserId = homeMemberQueryService.partsByUsers(users);

        return users.stream()
                .map(user -> {
                    List<String> parts = partsByUserId.getOrDefault(user.getUserId(), List.of());

                    return new HomeMemberItem(
                            user.getUserId(),
                            null,
                            user.getName(),
                            user.getRole().name(),
                            null,
                            null,
                            false,
                            parts
                    );
                })
                .toList();
    }
}