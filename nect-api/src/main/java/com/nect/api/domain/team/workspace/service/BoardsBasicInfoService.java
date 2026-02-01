package com.nect.api.domain.team.workspace.service;

import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.workspace.dto.req.BoardsBasicInfoUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.BoardsBasicInfoGetResDto;
import com.nect.api.domain.team.workspace.enums.BoardsErrorCode;
import com.nect.api.domain.team.workspace.exception.BoardsException;
import com.nect.api.global.code.DateConstants;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BoardsBasicInfoService {

    private static final ZoneId KST = ZoneId.of(DateConstants.TIME_ZONE);

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;

    private final ProjectHistoryPublisher historyPublisher;

    @Transactional(readOnly = true)
    public BoardsBasicInfoGetResDto getBasicInfo(Long projectId, Long userId) {

        // 프로젝트 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.PROJECT_NOT_FOUND, "projectId = " + projectId));


        // ACTIVE 멤버인지 확인
        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // 리더면 수정 가능
        boolean canEdit = projectUserRepository.existsActiveLeader(projectId, userId);

        // 남은 일자 계산 (plannedEndedOn 기준, endedAt x)
        LocalDate today = LocalDate.now(KST);
        LocalDate end = project.getPlannedEndedOn();

        long remainingDays = 0;
        if(end != null) {
            long diff = ChronoUnit.DAYS.between(today, end);
            remainingDays = Math.max(0, diff);
        }

        return new BoardsBasicInfoGetResDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getNoticeText(),
                project.getRegularMeetingText(),
                project.getPlannedStartedOn(),
                project.getPlannedEndedOn(),
                remainingDays,
                canEdit
        );

    }

    // 기본 정보 수정 서비스(리더만 가능)
    @Transactional
    public void updateBasicInfo(Long projectId, Long userId, BoardsBasicInfoUpdateReqDto req) {
        if (req == null) {
            throw new BoardsException(BoardsErrorCode.INVALID_REQUEST, "request body is null");
        }

        // 프로젝트 검증
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BoardsException(BoardsErrorCode.PROJECT_NOT_FOUND, "projectId = " + projectId));

        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if(!isMember) {
            throw new BoardsException(BoardsErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        boolean isLeader = projectUserRepository.existsActiveLeader(projectId, userId);
        if (!isLeader) {
            throw new BoardsException(BoardsErrorCode.PROJECT_LEADER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }

        // Before 스냅샷
        final String beforeNotice = project.getNoticeText();
        final String beforeRegularMeeting = project.getRegularMeetingText();

        boolean changed = false;

        // null : 변경 안 함, blank : invalid
        if(req.noticeText() != null) {
            if(req.noticeText().isBlank()) {
                throw new BoardsException(BoardsErrorCode.INVALID_REQUEST, "request notice text is empty");
            }
            project.updateNoticeText(req.noticeText());
            changed = true;
        }

        if (req.regularMeetingText() != null) {
            if (req.regularMeetingText().isBlank()) {
                throw new BoardsException(BoardsErrorCode.INVALID_REQUEST, "regularMeetingText is blank");
            }
            project.updateRegularMeetingText(req.regularMeetingText());
            changed = true;
        }

        if (!changed) {
            throw new BoardsException(BoardsErrorCode.INVALID_REQUEST, "no changes");
        }

        // After 스냅샷
        final String afterNotice = project.getNoticeText();
        final String afterRegularMeeting = project.getRegularMeetingText();

        // 바뀐 것만 수정
        Map<String, Object> changedMeta = new LinkedHashMap<>();

        if (!Objects.equals(beforeNotice, afterNotice)) {
            Map<String, Object> diff = new LinkedHashMap<>();
            diff.put("before", beforeNotice);
            diff.put("after", afterNotice);
            changedMeta.put("notice_text", diff);
        }

        if (!Objects.equals(beforeRegularMeeting, afterRegularMeeting)) {
            Map<String, Object> diff = new LinkedHashMap<>();
            diff.put("before", beforeRegularMeeting);
            diff.put("after", afterRegularMeeting);
            changedMeta.put("regular_meeting_text", diff);
        }

        // req는 들어왔지만 값이 동일해서 실질 변경이 0이면 예외 처리
        if (changedMeta.isEmpty()) {
            throw new BoardsException(BoardsErrorCode.INVALID_REQUEST, "no actual changes");
        }

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.PROJECT_BOARD_BASIC_INFO_UPDATED,
                HistoryTargetType.PROJECT,
                projectId,
                Map.of("changed", changedMeta)
        );

        // TODO: NotificationFacade를 통해 "프로젝트 공지/정기회의 수정" 알림 생성/발송
    }
}
