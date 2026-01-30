package com.nect.api.domain.home.service;

import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeRecommendService implements HomeService {

    @Override
    @Transactional(readOnly = true)
    public HomeProjectResponse getProjects(Long userId, Integer count) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public HomeMembersResponse getMembers(Long userId, Integer count) {
        return null;
    }
}
