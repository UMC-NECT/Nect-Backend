package com.nect.api.domain.mypage.service;


import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.repository.team.ProjectDeleteRepository;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectDeleteService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectDeleteRepository projectDeleteRepository;

    @Transactional
    public void deleteProject(Long projectId, Long requestUserId) {


        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));


        boolean isLeader = projectUserRepository
                .existsByProjectIdAndUserIdAndMemberType(projectId, requestUserId, ProjectMemberType.LEADER);
        if (!isLeader) {
            throw new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND);
        }



        List<Long> processIds = projectDeleteRepository.findProcessIdsByProjectId(projectId);
        projectDeleteRepository.deleteProcessMentions(processIds);
        projectDeleteRepository.deleteProcessSharedDocuments(processIds);
        projectDeleteRepository.deleteProcessUsers(processIds);
        projectDeleteRepository.deleteProcessFields(processIds);
        projectDeleteRepository.deleteProcessFeedbacks(processIds);
        projectDeleteRepository.deleteProcessTaskItems(processIds);


        projectDeleteRepository.deleteProcesses(projectId);


        projectDeleteRepository.deleteSharedDocuments(projectId);
        projectDeleteRepository.deletePosts(projectId);


        List<Long> chatRoomIds = projectDeleteRepository.findChatRoomIdsByProjectId(projectId);
        projectDeleteRepository.deleteChatFiles(chatRoomIds);
        projectDeleteRepository.deleteChatMessages(chatRoomIds);
        projectDeleteRepository.deleteChatRoomUsers(chatRoomIds);
        projectDeleteRepository.deleteChatRooms(projectId);


        projectDeleteRepository.deleteProjectSchedules(projectId);
        projectDeleteRepository.deleteProjectTeamRoles(projectId);
        projectDeleteRepository.deleteProjectPlanFiles(projectId);
        projectDeleteRepository.deleteProjectUserWorkDaily(projectId);
        projectDeleteRepository.deleteProjectHistory(projectId);
        projectDeleteRepository.deleteNotifications(projectId);
        projectDeleteRepository.deleteMatchings(projectId);
        projectDeleteRepository.deleteRecruitments(projectId);
        projectDeleteRepository.deleteProjectInterests(projectId);
        projectDeleteRepository.deleteUserTeamRoles(projectId);


        projectDeleteRepository.deleteProjectUsers(projectId);


        projectDeleteRepository.deleteProject(projectId);

    }
}