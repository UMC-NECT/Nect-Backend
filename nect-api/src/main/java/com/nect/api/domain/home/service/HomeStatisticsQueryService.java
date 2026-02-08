package com.nect.api.domain.home.service;

import com.nect.core.entity.matching.enums.MatchingStatus;
import com.nect.core.repository.matching.MatchingRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeStatisticsQueryService {

    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MatchingRepository matchingRepository;

    public int getTotalProjectCount(){
        return projectRepository.findAll().size();
    }

    public int getMatchingSuccessRate(){
        return (int)matchingRepository.countByStatus(MatchingStatus.ACCEPTED);
    }

    public int getReParticipantRate() {
        long rate = projectUserRepository.countRejoinedUsers().size() / projectUserRepository.countDistinctUsers();
        return (int) rate;
    }

    public int getTotalUserCount(){
        return userRepository.findAll().size();
    }


}
