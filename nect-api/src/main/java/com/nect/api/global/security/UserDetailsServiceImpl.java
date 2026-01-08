package com.nect.api.global.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // TODO: UserRepository와 User 엔티티 구현 후 사용
        return UserDetailsImpl.builder()
                .userId(Long.valueOf(userId))
                .roles(List.of("ROLE_USER"))
                .build();
    }

    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        // TODO: UserRepository와 User 엔티티 구현 후 사용
        return UserDetailsImpl.builder()
                .userId(userId)
                .roles(List.of("ROLE_USER"))
                .build();
    }
}