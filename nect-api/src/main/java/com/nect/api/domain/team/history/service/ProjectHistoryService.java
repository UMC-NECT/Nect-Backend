package com.nect.api.domain.team.history.service;

import com.nect.api.domain.team.history.dto.res.ProjectHistoryListResDto;
import com.nect.api.domain.team.history.dto.res.ProjectHistoryResDto;
import com.nect.api.domain.team.history.enums.HistoryErrorCode;
import com.nect.api.domain.team.history.exception.HistoryException;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.history.ProjectHistory;
import com.nect.core.repository.team.ProjectRepository;
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
    private final ProjectRepository projectRepository;
    private final ProjectHistoryRepository historyRepository;

    public ProjectHistoryListResDto getHistories(Long projectId, Long cursor, Integer size) {

        // 프로젝트 존재 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new HistoryException(
                        HistoryErrorCode.PROJECT_NOT_FOUND,
                        "projectId=" + projectId
                ));

        // size 정규화 (기본 20, 최대 50)
        int pageSize = normalizeSize(size);

        //  조회
        List<ProjectHistory> histories = (cursor == null)
                ? historyRepository.findLatest(project.getId(), PageRequest.of(0, pageSize))
                : historyRepository.findLatestByCursor(project.getId(), cursor, PageRequest.of(0, pageSize));

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

    private int normalizeSize(Integer size) {
        if (size == null) return 20;
        if (size <= 0) {
            throw new HistoryException(HistoryErrorCode.INVALID_REQUEST, "size must be positive");
        }
        return Math.min(size, 50);
    }
}
