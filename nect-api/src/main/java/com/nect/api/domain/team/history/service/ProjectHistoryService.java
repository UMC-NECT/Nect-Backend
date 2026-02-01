package com.nect.api.domain.team.history.service;

import com.nect.api.domain.team.history.dto.res.ProjectHistoryListResDto;
import com.nect.api.domain.team.history.dto.res.ProjectHistoryResDto;
import com.nect.api.domain.team.history.enums.HistoryErrorCode;
import com.nect.api.domain.team.history.exception.HistoryException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.history.ProjectHistory;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import com.nect.core.repository.team.history.ProjectHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectHistoryService {

    private static final int PAGE_SIZE = 10;

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectHistoryRepository historyRepository;


    private void assertActiveProjectMember(Long projectId, Long userId) {
        if (!projectUserRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new HistoryException(
                    HistoryErrorCode.FORBIDDEN,
                    "not an active project member. projectId=" + projectId + ", userId=" + userId
            );
        }
    }

    public ProjectHistoryListResDto getHistories(Long projectId, Long userId, Long cursor) {
        assertActiveProjectMember(projectId, userId);

        // 프로젝트 존재 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new HistoryException(
                        HistoryErrorCode.PROJECT_NOT_FOUND,
                        "projectId=" + projectId
                ));

        // 항상 최근 10개 고정
        PageRequest page = PageRequest.of(0, PAGE_SIZE);

        //  조회
        List<ProjectHistory> histories = (cursor == null)
                ? historyRepository.findLatest(project.getId(), page)
                : historyRepository.findLatestByCursor(project.getId(), cursor, page);

        // DTO 변환
        List<ProjectHistoryResDto> items = histories.stream()
                .map(h -> new ProjectHistoryResDto(
                        h.getId(),
                        h.getActorUserId(),
                        h.getAction(),
                        h.getTargetType(),
                        h.getTargetId(),
                        h.getMetaJson(),
                        h.getCreatedAt()
                ))
                .toList();

        // nextCursor 계산
        Long nextCursor = histories.isEmpty() ? null : histories.get(histories.size() - 1).getId();

        return new ProjectHistoryListResDto(nextCursor, items);
    }
}
