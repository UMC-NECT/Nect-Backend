package com.nect.api.domain.dm.service;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 개인 채팅방 접속 정보를 관리합니다.
 */
@Component
public class DmPresenceRegistry {

    private final ConcurrentHashMap<String, Set<Long>> roomUsers = new ConcurrentHashMap<>();

    // 채팅방 입장
    public void enter(String roomId, Long userId) {
        roomUsers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    // 채팅방 나가기
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

    // 두 유저 모두 채팅방에 들어와있는지
    public boolean bothPresent(String roomId, Long userA, Long userB) {
        Set<Long> users = roomUsers.get(roomId);
        return users != null && users.contains(userA) && users.contains(userB);
    }
}
