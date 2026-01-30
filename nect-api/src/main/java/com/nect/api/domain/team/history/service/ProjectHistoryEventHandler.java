package com.nect.api.domain.team.history.service;


import com.nect.api.domain.team.history.event.ProjectHistoryEvent;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.history.ProjectHistory;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.history.ProjectHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectHistoryEventHandler {
    private final ProjectRepository projectRepository;
    private final ProjectHistoryRepository historyRepository;

    /**
     * 프로세스 CRUD 트랜잭션이 "커밋된 이후" 히스토리 저장
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProjectHistoryEvent e) {
        try {
            // DB 조회 없이 참조만 잡음 (프록시)
            Project projectRef = projectRepository.getReferenceById(e.projectId());

            ProjectHistory history = ProjectHistory.builder()
                    .project(projectRef)
                    .actorUserId(e.actorUserId())
                    .action(e.action())
                    .targetType(e.targetType())
                    .targetId(e.targetId())
                    .metaJson(e.metaJson())
                    .build();

            historyRepository.save(history);

        } catch (Exception ex) {
            log.error("Failed to save project history. event={}", e, ex);
        }
    }
}
