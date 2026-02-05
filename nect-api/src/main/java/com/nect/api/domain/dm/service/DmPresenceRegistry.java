package com.nect.api.domain.dm.service;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DmPresenceRegistry {
    private final ConcurrentHashMap<String, Set<Long>> roomUsers = new ConcurrentHashMap<>();

    public void enter(String roomId, Long userId) {
        roomUsers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public void leave(String roomId, Long userId) {
        Set<Long> users = roomUsers.get(roomId);
        if (users == null) {
            return;
        }
        users.remove(userId);
        if (users.isEmpty()) {
            roomUsers.remove(roomId);
        }
    }

    public boolean bothPresent(String roomId, Long userA, Long userB) {
        Set<Long> users = roomUsers.get(roomId);
        return users != null && users.contains(userA) && users.contains(userB);
    }
}
