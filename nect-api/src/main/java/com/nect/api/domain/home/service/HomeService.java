package com.nect.api.domain.home.service;

import com.nect.api.domain.home.dto.HomeMembersResponse;
import com.nect.api.domain.home.dto.HomeProjectResponse;

public interface HomeService {
    public HomeProjectResponse getProjects(Long userId, Integer count);

    public HomeMembersResponse getMembers(Long userId, Integer count);
}
