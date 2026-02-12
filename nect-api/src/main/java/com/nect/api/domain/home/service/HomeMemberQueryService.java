package com.nect.api.domain.home.service;

import com.nect.api.domain.home.dto.HomeHeaderResponse;
import com.nect.api.domain.user.exception.UserNotFoundException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.enums.MemberMatchable;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserRole;
import com.nect.core.entity.user.UserTeamRole;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.RoleField;
import com.nect.core.repository.user.ProjectUserRepositoryComplete;
import com.nect.core.repository.user.UserTeamRoleRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserInterestRepository;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.repository.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeMemberQueryService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserInterestRepository userInterestRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectUserRepositoryComplete projectUserRepositoryComplete;
    private final UserTeamRoleRepository userTeamRoleRepository;
    private final S3Service s3Service;

    public List<User> getFilteredMembers(Long userId, int count, Role role, InterestField interest) {
        PageRequest pageRequest = PageRequest.of(0, count);
        return userInterestRepository.findUsersByInterestAndRoleExcludingUser(interest, role, userId, pageRequest);
    }

    public List<User> getAllUsersWithoutUser(Long userId, int count) {
        PageRequest pageRequest = PageRequest.of(0, count);
        return (userId == null)
                ? userRepository.findAll(pageRequest).getContent()
                : userRepository.findByUserIdNot(userId, pageRequest);
    }

    public Map<Long, List<String>> partsByUsers(List<User> users) {
        if (users.isEmpty()) return Map.of();

        Map<Long, LinkedHashSet<String>> tmp = new HashMap<>();

        for (UserRoleRepository.UserRoleFieldRow row : userRoleRepository.findUserRoleFieldsByUsers(users)) {
            tmp.computeIfAbsent(row.getUserId(), k -> new LinkedHashSet<>())
                    .add(row.getRoleField().name());
        }

        Map<Long, List<String>> result = new HashMap<>();
        for (var e : tmp.entrySet()) {
            result.put(e.getKey(), List.copyOf(e.getValue()));
        }
        return result;
    }

    public List<String> parts(User user) {
        List<UserRole> roleUsers = userRoleRepository.findByUser(user);
        return roleUsers.stream()
                .map(UserRole::getRoleField)
                .map(RoleField::name)
                .distinct()
                .toList();
    }

    public HomeHeaderResponse getHeaderProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        // 역할들
        List<UserRole> userRoles = userRoleRepository.findByUser(user);

        // 역할 ( 개발자, 디자이너, 기획자 등 )
        Role role = userRoles.getFirst().getRoleField().getRole();

        return HomeHeaderResponse.of(
                user.getUserId(),
                s3Service.getPresignedGetUrl(user.getProfileImageName()),
                user.getName(),
                user.getEmail(),
                role
        );
    }


    // 매칭 가능 판단 후 MemberMatchable 반환
    public MemberMatchable getMemberMatchable(List<Long> projectIds, Long targetUserId) {
        if (projectIds == null || projectIds.isEmpty()) {
            return MemberMatchable.MATCH_COMPLETE;
        }

        int activeProjectCount = projectUserRepository.findActiveProjectsByUserId(targetUserId).size();
        if (activeProjectCount < 2) {
//            User targetUser = userRepository.findById(targetUserId)
//                    .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
//
//            List<UserRole> userRoles = userRoleRepository.findByUser(targetUser);
//            if (userRoles.isEmpty()) {
//                return MemberMatchable.MATCH_COMPLETE;
//            }
//
//            List<UserTeamRole> projectRoles = userTeamRoleRepository.findByProjectIdIn(projectIds);
//            Map<Long, List<UserTeamRole>> rolesByProjectId = new HashMap<>();
//            for (UserTeamRole role : projectRoles) {
//                if (role.isDeleted()) {
//                    continue;
//                }
//                Long projectId = role.getProject().getId();
//                rolesByProjectId.computeIfAbsent(projectId, k -> new java.util.ArrayList<>()).add(role);
//            }
//
//            var activeMembers = projectUserRepositoryComplete.findByProjectIdInAndMemberStatus(projectIds, ProjectMemberStatus.ACTIVE);
//            Map<Long, Map<String, Integer>> activeCountsByProjectId = new HashMap<>();
//            for (var member : activeMembers) {
//                Long projectId = member.getProject().getId();
//                Map<String, Integer> activeCounts = activeCountsByProjectId.computeIfAbsent(projectId, k -> new HashMap<>());
//                String key = roleKey(member.getRoleField(), member.getCustomRoleFieldName());
//                activeCounts.put(key, activeCounts.getOrDefault(key, 0) + 1);
//            }
//
//            for (Long projectId : projectIds) {
//                List<UserTeamRole> roles = rolesByProjectId.get(projectId);
//                if (roles == null || roles.isEmpty()) {
//                    continue;
//                }
//
//                Map<String, Integer> activeCounts = activeCountsByProjectId.getOrDefault(projectId, Map.of());
//                for (UserTeamRole projectRole : roles) {
//                    if (projectRole.getRequiredCount() == null || projectRole.getRequiredCount() < 1) {
//                        continue;
//                    }
//
//                    if (!userHasRole(userRoles, projectRole.getRoleField(), projectRole.getCustomRoleFieldName())) {
//                        continue;
//                    }
//
//                    String key = roleKey(projectRole.getRoleField(), projectRole.getCustomRoleFieldName());
//                    int currentCount = activeCounts.getOrDefault(key, 0);
//                    if (currentCount < projectRole.getRequiredCount()) {
//                        return MemberMatchable.MATCHABLE;
//                    }
//                }
//            }
            return MemberMatchable.MATCHABLE;
        }

        return MemberMatchable.MATCH_COMPLETE;

    }

    private static String roleKey(RoleField roleField, String customRoleFieldName) {
        String custom = (customRoleFieldName == null) ? "" : customRoleFieldName.trim().toLowerCase();
        return roleField.name() + ":" + custom;
    }

    private static boolean userHasRole(List<UserRole> userRoles, RoleField roleField, String customRoleFieldName) {
        if (roleField == RoleField.CUSTOM) {
            String custom = (customRoleFieldName == null) ? "" : customRoleFieldName.trim().toLowerCase();
            for (UserRole userRole : userRoles) {
                if (userRole.getRoleField() != RoleField.CUSTOM) {
                    continue;
                }
                String userCustom = (userRole.getCustomField() == null) ? "" : userRole.getCustomField().trim().toLowerCase();
                if (custom.equals(userCustom)) {
                    return true;
                }
            }
            return false;
        }

        for (UserRole userRole : userRoles) {
            if (userRole.getRoleField() == roleField) {
                return true;
            }
        }
        return false;
    }

}
