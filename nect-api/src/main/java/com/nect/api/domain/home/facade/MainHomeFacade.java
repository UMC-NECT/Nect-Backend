package com.nect.api.domain.home.facade;

import com.nect.api.domain.home.dto.HomeMemberItem;
import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectItem;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import com.nect.api.domain.home.dto.HomeHeaderResponse;
import com.nect.api.domain.home.exception.HomeInvalidParametersException;
import com.nect.api.domain.home.service.HomeMemberQueryService;
import com.nect.api.domain.home.service.HomeProjectQueryService;
import com.nect.api.global.infra.S3Service;
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

    private final HomeProjectQueryService homeProjectQueryService;
    private final HomeMemberQueryService homeMemberQueryService;
    private final S3Service s3Service;

    // 모집 중인 프로젝트
    public HomeProjectResponse getRecruitingProjects(Long userId, int count, Role role, InterestField interest){

        int safeCount = safeCount(count);

        // 페이징 정보
        PageRequest pageRequest = PageRequest.of(0, safeCount);

        // List<Project> 미리 생성
//        List<Project> projects = new ArrayList<>();

//        // 둘 중 하나가 null일 수는 없음
//        if ((role == null && interest != null) || (role != null && interest == null)) {
//            throw new HomeInvalidParametersException("role과 interest 중 하나만 null일 수 없습니다.");
//        }
//
//        // role이 null일 때
//        if (role == null) {
//
//        }else{
//
//        }

        List<Project> projects = homeProjectQueryService.getProjects(userId, pageRequest);

        return buildProjectResponse(projects);
    }

    // 홈화면 추천 프로젝트들
    public HomeProjectResponse getRecommendedProjects(Long userId, int count) {
        int safeCount = safeCount(count);
        List<Project> projects = homeProjectQueryService.getProjects(userId, PageRequest.of(0, safeCount));

        if (projects.isEmpty()) {
            return HomeProjectResponse.of(List.of());
        }

        Collections.shuffle(projects);
        List<Project> randomProjects = projects.subList(0, Math.min(safeCount, projects.size()));

        return buildProjectResponse(randomProjects);
    }

    // 홈화면 매칭 가능한 넥터
    public HomeMembersResponse getMatchableMembers(Long userId, int count, Role role, InterestField interest) {

        // 둘 중 하나가 null일 수는 없음
        if ((role == null && interest != null) || (role != null && interest == null)) {
            throw new HomeInvalidParametersException("role과 interest 중 하나만 null일 수 없습니다.");
        }

        // List 선언
        List<User> users;
        int safeCount = safeCount(count);

        if (role != null) { // 둘 다 null이 아니면 필터링해서 반환
            users = homeMemberQueryService.getFilteredMembers(userId, safeCount, role, interest);
        }
        else{ // 둘 모두 null이면 모두 조회하여 반환
            users = homeMemberQueryService.getAllUsersWithoutUser(userId, safeCount);
        }

        return buildMemberResponse(users);
    }

    // 홈화면 추천 넥터
    public HomeMembersResponse getRecommendedMembers(Long userId, int count) {
        int safeCount = safeCount(count);
        List<User> users = homeMemberQueryService.getAllUsersWithoutUser(userId, safeCount);

        List<HomeMemberItem> items = new ArrayList<>(responsesFromMembers(users));
        Collections.shuffle(items);

        return HomeMembersResponse.of(items);
    }

    // 홈화면 헤더 프로필
    public HomeHeaderResponse getHeaderProfile(Long userId) {
        return homeMemberQueryService.getHeaderProfile(userId);
    }

    // List<Project> -> List<HomeProjectItem>
    private List<HomeProjectItem> responsesFromProjects(List<Project> projects) {
        HomeProjectQueryService.HomeProjectBatch batch = homeProjectQueryService.loadHomeProjectBatch(projects);

        return projects.stream()
                .map(p -> {
                    Long projectId = p.getId();

                    User author = batch.authorByProjectId().get(projectId);
                    Integer dDay = homeProjectQueryService.getDDay(p);
                    Integer maxMemberCount = batch.maxMemberCountByProjectId().getOrDefault(projectId, 0);
                    Integer currentMemberCount = batch.activeCountByProjectId().getOrDefault(projectId, 0);
                    Map<String, Integer> partCounts = batch.partCountsByProjectId().getOrDefault(projectId, Map.of());

                    return HomeProjectItem.of(
                            projectId,
                            resolveProjectImage(p),
                            p.getTitle(),
                            author == null ? null : author.getName(),
                            author == null ? null : author.getRole().name(),
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

                    return HomeMemberItem.of(
                            user.getUserId(),
                            s3Service.getPresignedGetUrl(user.getProfileImageName()),
                            user.getName(),
                            user.getRole().name(),
                            null,
                            user.getUserStatus().name(),
                            false,
                            parts
                    );
                })
                .toList();
    }

    private HomeProjectResponse buildProjectResponse(List<Project> projects) {
        if (projects.isEmpty()) {
            return HomeProjectResponse.of(List.of());
        }

        return HomeProjectResponse.of(responsesFromProjects(projects));
    }

    private HomeMembersResponse buildMemberResponse(List<User> users) {
        return HomeMembersResponse.of(responsesFromMembers(users));
    }

    private int safeCount(int count) {
        return Math.max(1, count);
    }

    private String resolveProjectImage(Project project) {
        String imageName = project.getImageName();
        return imageName == null ? null : s3Service.getPresignedGetUrl(imageName);
    }


}
