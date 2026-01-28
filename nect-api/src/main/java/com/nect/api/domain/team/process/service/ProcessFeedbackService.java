package com.nect.api.domain.team.process.service;

import com.nect.api.domain.team.process.dto.req.ProcessFeedbackCreateReqDto;
import com.nect.api.domain.team.process.dto.req.ProcessFeedbackUpdateReqDto;
import com.nect.api.domain.team.process.dto.res.FeedbackCreatedByResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackCreateResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackDeleteResDto;
import com.nect.api.domain.team.process.dto.res.ProcessFeedbackUpdateResDto;
import com.nect.api.domain.team.process.enums.ProcessErrorCode;
import com.nect.api.domain.team.process.exception.ProcessException;
import com.nect.core.entity.team.process.Process;
import com.nect.core.entity.team.process.ProcessFeedback;
import com.nect.core.repository.team.process.ProcessFeedbackRepository;
import com.nect.core.repository.team.process.ProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessFeedbackService {

    private final ProcessRepository processRepository;
    private final ProcessFeedbackRepository processFeedbackRepository;

    // TODO(인증/인가): Security/User 붙이면 CurrentUserProvider(또는 AuthFacade), ProjectUserRepository(멤버십 검증용) 주입 예정
    // private final Auth
    // private final UserRepository userRepository;
    // private final ProjectUserRepository projectUserRepository;

    private Process getActiveProcess(Long projectId, Long processId) {
        return processRepository.findByIdAndProjectIdAndDeletedAtIsNull(processId, projectId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.PROCESS_NOT_FOUND,
                        "projectId=" + projectId + ", processId=" + processId
                ));
    }

    private ProcessFeedback getFeedback(Long processId, Long feedbackId) {
        return processFeedbackRepository.findByIdAndProcessIdAndDeletedAtIsNull(feedbackId, processId)
                .orElseThrow(() -> new ProcessException(
                        ProcessErrorCode.FEEDBACK_NOT_FOUND,
                        "feedbackId=" + feedbackId + ", processId=" + processId
                ));
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ProcessException(ProcessErrorCode.INVALID_FEEDBACK_CONTENT);
        }
    }

    // 피드백 생성 서비스
    @Transactional
    public ProcessFeedbackCreateResDto createFeedback(Long projectId, Long processId, ProcessFeedbackCreateReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증 (프로젝트 참여자만 피드백 생성 가능)
        // TODO(작성자): createdByUserId = currentUserId 로 저장되도록 엔티티/필드 설계 필요
        // TODO(작성자 응답): createdByUserName / createdByFields 는 User/ProjectUser 연동 후 채우기

        validateContent(req.content());

        Process process = getActiveProcess(projectId, processId);

        // TODO : 로그인 유저 조회 + 프로젝트 멤버 검증 + 분야 아이디 조회하기
        Long createdByUserId = 1L; // 임시
        String createdByUserName = "임시유저"; // 임시
        List<Long> createdByFields = List.of();

        ProcessFeedback feedback = ProcessFeedback.builder()
                .process(process)
                .content(req.content())
                .build();

        ProcessFeedback saved = processFeedbackRepository.save(feedback);

        Long actorUserId = createdByUserId; // TODO(인증) 붙으면 currentUserId 사용

        // TODO(Notification):
        // - "피드백 생성" 알림 트리거 지점
        // - 수신자: 프로젝트 멤버 전체 OR 해당 프로세스 관련자(assignee/mention) 우선 (유저/멤버십 연동 후 결정)
        // - NotificationType 예: PROCESS_FEEDBACK_CREATED
        // - targetId: saved.getId() (feedbackId) 또는 processId
        // - mainArgs/contentArgs 예: [processTitle], [작성자명, content 요약(짧게)]
        // - 현재 NotificationFacade는 즉시 SSE 전송까지 하므로, 정합성 위해 AFTER_COMMIT 이벤트 리스너로 전환 권장

        // TODO(HISTORY):
        // - ProjectHistoryEvent 발행 지점 (저장은 ProjectHistoryEventHandler가 AFTER_COMMIT에 처리)
        // - action 예: FEEDBACK_CREATED (or PROCESS_FEEDBACK_CREATED)
        // - targetType 예: PROCESS_FEEDBACK (또는 PROCESS)
        // - targetId: saved.getId()
        // - metaJson 예: { "processId": processId, "feedbackId": saved.getId(), "content": saved.getContent() }
        // - createdBy 정보 확정되면 metaJson에 actor/createdBy도 포함 가능

        // TODO(TEAM EVENT FACADE):
        // - 추후 Notification + History를 ActivityFacade(가칭)로 통합하여 activityFacade.recordAndNotify 호출로 변경 예정


        return new ProcessFeedbackCreateResDto(
            saved.getId(),
            saved.getContent(),
            saved.getStatus(),
//            new FeedbackCreatedByResDto(createdByUserId, createdByUserName, createdByFields),
            saved.getCreatedAt()
        );
    }


    // 피드백 수정
    @Transactional
    public ProcessFeedbackUpdateResDto updateFeedback(Long projectId, Long processId, Long feedbackId, ProcessFeedbackUpdateReqDto req) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증
        // TODO(인가-작성자): "피드백 작성자만 수정 가능" 정책이면 feedback.createdBy == currentUserId 검증 추가
        // TODO(작성자 응답): createdByUserName / createdByFields 연동 후 채우기


        validateContent(req.content());

        // 부모 프로세스가 살아있는지 + 프로젝트 소속인지 한 번에 검증
        getActiveProcess(projectId, processId);

        ProcessFeedback feedback = getFeedback(processId, feedbackId);

        feedback.updateContent(req.content());

        Long actorUserId = 1L; // TODO(인증)
        // TODO(Notification):
        // - "피드백 수정" 알림 트리거 지점
        // - 수신자: 프로젝트 멤버 전체 OR 해당 프로세스 관련자(assignee/mention) (유저/멤버십 연동 후)
        // - NotificationType 예: PROCESS_FEEDBACK_UPDATED
        // - meta: before/after 또는 "수정됨" 정도만
        // - 권장: AFTER_COMMIT 이후 알림 전송(이벤트 리스너)

        // TODO(HISTORY):
        // - action 예: FEEDBACK_UPDATED
        // - targetType 예: PROCESS_FEEDBACK
        // - targetId: feedbackId
        // - metaJson 예:
        //   { "processId": processId, "feedbackId": feedbackId, "before": {"content": beforeContent}, "after": {"content": feedback.getContent()} }
        // - beforeContent 스냅샷은 validate 직후, updateContent 전에 떠두는 방식 권장
        // - 저장은 ProjectHistoryEventHandler(AFTER_COMMIT)

        // TODO(TEAM EVENT FACADE): 추후 ActivityFacade로 통합

        Long createdByUserId = 1L; // 임시
        String createdByUserName = "임시유저"; // 임시
        List<Long> createdByFields = List.of();

        return new ProcessFeedbackUpdateResDto(
                feedback.getId(),
                feedback.getContent(),
                feedback.getStatus(),
                new FeedbackCreatedByResDto(createdByUserId, createdByUserName, createdByFields),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }


    // 피드백 삭제
    @Transactional
    public ProcessFeedbackDeleteResDto deleteFeedback(Long projectId, Long processId, Long feedbackId) {
        // TODO(인증): 현재 로그인 유저 userId 추출
        // TODO(인가): projectId 멤버십 검증
        // TODO(인가-작성자): "피드백 작성자만 삭제 가능" 정책이면 feedback.createdBy == currentUserId 검증 추가

        getActiveProcess(projectId, processId);

        ProcessFeedback feedback = getFeedback(processId, feedbackId);

        // TODO(HISTORY/NOTI): 삭제 전 스냅샷 확보 권장
        // - beforeContent = feedback.getContent()
        // - beforeCreatedBy = feedback.getCreatedByUserId() (필드 확정 후)
        // - beforeCreatedAt = feedback.getCreatedAt()

        feedback.softDelete();

        Long actorUserId = 1L; // TODO(인증)

        // TODO(Notification):
        // - "피드백 삭제" 알림 트리거 지점
        // - 수신자: 프로젝트 멤버 전체 OR 해당 프로세스 관련자
        // - NotificationType 예: PROCESS_FEEDBACK_DELETED
        // - meta: 삭제된 피드백의 content 요약/작성자 등(스냅샷 기반)
        // - 권장: AFTER_COMMIT 이후 알림 전송


        // TODO(HISTORY):
        // - action 예: FEEDBACK_DELETED
        // - targetType 예: PROCESS_FEEDBACK
        // - targetId: feedbackId
        // - metaJson 예: { "processId": processId, "feedbackId": feedbackId, "content": beforeContent }
        // - 저장은 ProjectHistoryEventHandler(AFTER_COMMIT)

        // TODO(TEAM EVENT FACADE): 추후 ActivityFacade로 통합

        return new ProcessFeedbackDeleteResDto(feedbackId);
    }
}
