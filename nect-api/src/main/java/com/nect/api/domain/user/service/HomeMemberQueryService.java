package com.nect.api.domain.user.service;

import com.nect.core.entity.user.User;
import com.nect.core.entity.user.UserRole;
import com.nect.core.entity.user.enums.RoleField;
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

    public List<User> getAllUsersWithoutUser(Long userId, int count) {
        PageRequest pageRequest = PageRequest.of(0, count);
        return userRepository.findByUserIdNot(userId, pageRequest);
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
}

