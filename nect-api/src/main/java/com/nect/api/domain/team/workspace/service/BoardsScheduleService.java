package com.nect.api.domain.team.workspace.service;


import com.nect.api.domain.team.history.service.ProjectHistoryPublisher;
import com.nect.api.domain.team.workspace.dto.req.ScheduleCreateReqDto;
import com.nect.api.domain.team.workspace.dto.req.ScheduleUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.CalendarMonthIndicatorsResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleCreateResDto;
import com.nect.api.domain.team.workspace.dto.res.ScheduleUpcomingResDto;
import com.nect.api.domain.team.workspace.enums.ScheduleErrorCode;
import com.nect.api.domain.team.workspace.exception.ScheduleException;
import com.nect.api.global.code.DateConstants;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectSchedule;
import com.nect.core.entity.team.history.enums.HistoryAction;
import com.nect.core.entity.team.history.enums.HistoryTargetType;
import com.nect.core.repository.team.ProjectRepository;
import com.nect.core.repository.team.ProjectScheduleRepository;
import com.nect.core.repository.team.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardsScheduleService {

    private static final ZoneId KST = ZoneId.of(DateConstants.TIME_ZONE);

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectScheduleRepository scheduleRepository;

    private final ProjectHistoryPublisher historyPublisher;

    // 캘린더 월간 인디케이터 조회 서비스
    @Transactional(readOnly = true)
    public CalendarMonthIndicatorsResDto getMonthIndicators(Long projectId, Long userId, int year, int month) {
        // 프로젝트 검증 + 멤버 확인
        Project project = validateProjectAndMember(projectId, userId);

        // month 값 검증 (1~12만 허용)
        if(month < 1 || month > 12) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "invalid month" + month);
        }

        LocalDate first =  LocalDate.of(year, month, 1);
        LocalDate last = first.withDayOfMonth(first.lengthOfMonth());

        LocalDateTime rangeStart = first.atStartOfDay();
        LocalDateTime rangeEndExclusive = last.plusDays(1).atStartOfDay();

        // 해당 월과 겹치는 일정들 조회
        // 월 범위 안에 완전히 포함되지 않아도 시작/종료가 걸쳐서 겹치면 조회되도록 조건을 잡는다.
        List<ProjectSchedule> schedules = scheduleRepository
                .findOverlappingSchedules(projectId, rangeStart, rangeEndExclusive);

        // 날짜별 일정 개수 집계
        Map<LocalDate, Integer> counts = new HashMap<>();

        // 일정을 날짜 단위로 펼쳐서 count 누적
        for(ProjectSchedule schedule : schedules) {
            LocalDate start = schedule.getStartAt().toLocalDate();
            LocalDate end = schedule.getEndAt().toLocalDate();

            LocalDate cur = start.isBefore(first) ? first : start;
            LocalDate until = end.isAfter(last) ? last : end;

            while (!cur.isAfter(until)) {
                counts.put(cur, counts.getOrDefault(cur, 0) + 1);
                cur = cur.plusDays(1);
            }
        }

        List<CalendarMonthIndicatorsResDto.DayIndicator> days = counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new CalendarMonthIndicatorsResDto.DayIndicator(e.getKey(), e.getValue()))
                .toList();

        return new CalendarMonthIndicatorsResDto(year, month, days);
    }

    // 다가오는 팀 일정(프리뷰) 조회 서비스
    // - 팀보드 우측 하단 "다가오는 일정" 영역에 보여줄 일정 목록을 가져온다.
    @Transactional(readOnly = true)
    public ScheduleUpcomingResDto getUpcoming(Long projectId, Long userId, String from, int limit) {
        // 프로젝트 멤버인지 검증
        validateProjectAndMember(projectId, userId);

        // limit 안전 처리 (1~20)
        int safeLimit = Math.max(1, Math.min(limit, 20));

        // 기준 날짜(from) 파싱
        // - 없으면 오늘 00:00(KST)
        // - 있으면 yyyy-MM-dd를 00:00으로 변환
        LocalDateTime fromDt;
        if (from == null || from.isBlank()) {
            fromDt = LocalDate.now(KST).atStartOfDay();
        } else {
            LocalDate d = LocalDate.parse(from);
            fromDt = d.atStartOfDay();
        }

        // fromDt 이후 시작하는 일정만, 시작 시간 오름차순으로 limit개 조회
        List<ProjectSchedule> list = scheduleRepository
                .findAllByProjectIdAndDeletedAtIsNullAndStartAtGreaterThanEqualOrderByStartAtAsc(
                        projectId,
                        fromDt,
                        PageRequest.of(0, safeLimit)
                );


        List<ScheduleUpcomingResDto.Item> items = list.stream()
                .map(s -> new ScheduleUpcomingResDto.Item(
                        s.getId(),
                        s.getTitle(),
                        s.getStartAt(),
                        s.getEndAt(),
                        s.isAllDay(),
                        isMultiDay(s.getStartAt(), s.getEndAt())
                ))
                .toList();

        return new ScheduleUpcomingResDto(items);
    }

    // 팀 일정 생성
    @Transactional
    public ScheduleCreateResDto create(Long projectId, Long userId, ScheduleCreateReqDto req) {

        Project project = validateProjectAndMember(projectId, userId);

        if (req == null)
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "request body is null");

        if (req.title() == null || req.title().isBlank())
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "title is blank");

        if (req.startAt() == null || req.endAt() == null)
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "start_at/end_at is null");

        if (req.endAt().isBefore(req.startAt())) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "end_at < start_at");
        }

        boolean allDay = Boolean.TRUE.equals(req.allDay());

        ProjectSchedule schedule = ProjectSchedule.builder()
                .project(project)
                .creatorUserId(userId)
                .title(req.title())
                .description(req.description())
                .startAt(req.startAt())
                .endAt(req.endAt())
                .allDay(allDay)
                .build();

        ProjectSchedule saved = scheduleRepository.save(schedule);

        // 커밋 이후 저장
        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.SCHEDULE_CREATED,
                HistoryTargetType.SCHEDULE,
                saved.getId(),
                Map.of(
                        "title", saved.getTitle(),
                        "description", saved.getDescription(),
                        "start_at", saved.getStartAt(),
                        "end_at", saved.getEndAt(),
                        "all_day", saved.isAllDay()
                )
        );

        return new ScheduleCreateResDto(saved.getId());
    }


    // 팀 일정 수정
    @Transactional
    public void update(Long projectId, Long userId, Long scheduleId, ScheduleUpdateReqDto req) {
        validateProjectAndMember(projectId, userId);

        if (req == null)
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "request body is null");

        ProjectSchedule schedule = scheduleRepository.findByIdAndProjectIdAndDeletedAtIsNull(scheduleId, projectId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND,
                        "projectId=" + projectId + ", scheduleId=" + scheduleId));

        if (req.title() != null && req.title().isBlank()) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "title is blank");
        }

        // Before 스냅샷
        final String beforeTitle = schedule.getTitle();
        final String beforeDesc = schedule.getDescription();
        final LocalDateTime beforeStart = schedule.getStartAt();
        final LocalDateTime beforeEnd = schedule.getEndAt();
        final boolean beforeAllDay = schedule.isAllDay();

        // 시간 역전 방지용 검증(부분 수정 고려)
        LocalDateTime newStart = (req.startAt() != null) ? req.startAt() : schedule.getStartAt();
        LocalDateTime newEnd = (req.endAt() != null) ? req.endAt() : schedule.getEndAt();

        if (newEnd.isBefore(newStart)) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "end_at < start_at");
        }

        // 실제 업데이트
        schedule.update(req.title(), req.description(), req.startAt(), req.endAt(), req.allDay());

        // changed meta
        Map<String, Object> changed = new LinkedHashMap<>();


        if (!Objects.equals(beforeTitle, schedule.getTitle())) {
            changed.put("title", Map.of("before", beforeTitle, "after", schedule.getTitle()));
        }
        if (!Objects.equals(beforeDesc, schedule.getDescription())) {
            changed.put("description", Map.of("before", beforeDesc, "after", schedule.getDescription()));
        }
        if (!Objects.equals(beforeStart, schedule.getStartAt())) {
            changed.put("start_at", Map.of("before", beforeStart, "after", schedule.getStartAt()));
        }
        if (!Objects.equals(beforeEnd, schedule.getEndAt())) {
            changed.put("end_at", Map.of("before", beforeEnd, "after", schedule.getEndAt()));
        }
        if (beforeAllDay != schedule.isAllDay()) {
            changed.put("all_day", Map.of("before", beforeAllDay, "after", schedule.isAllDay()));
        }

        if (changed.isEmpty()) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_REQUEST, "no changes");
        }

        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.SCHEDULE_UPDATED,
                HistoryTargetType.SCHEDULE,
                schedule.getId(),
                Map.of("changed", changed)
        );

    }

    // 일정 삭제
    @Transactional
    public void delete(Long projectId, Long userId, Long scheduleId) {

        validateProjectAndMember(projectId, userId);

        ProjectSchedule schedule = scheduleRepository.findByIdAndProjectIdAndDeletedAtIsNull(scheduleId, projectId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND,
                        "projectId=" + projectId + ", scheduleId=" + scheduleId));

        // before snapshot
        final String beforeTitle = schedule.getTitle();
        final LocalDateTime beforeStart = schedule.getStartAt();
        final LocalDateTime beforeEnd = schedule.getEndAt();

        schedule.softDelete();

        // HISTORY
        historyPublisher.publish(
                projectId,
                userId,
                HistoryAction.SCHEDULE_DELETED,
                HistoryTargetType.SCHEDULE,
                schedule.getId(),
                Map.of(
                        "title", beforeTitle,
                        "start_at", beforeStart,
                        "end_at", beforeEnd,
                        "deleted_at", LocalDateTime.now(KST)
                )
        );

    }


    private Project validateProjectAndMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.PROJECT_NOT_FOUND, "projectId=" + projectId));

        boolean isMember = projectUserRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new ScheduleException(ScheduleErrorCode.PROJECT_MEMBER_FORBIDDEN,
                    "projectId=" + projectId + ", userId=" + userId);
        }
        return project;
    }

    private boolean isMultiDay(LocalDateTime startAt, LocalDateTime endAt) {
        return !startAt.toLocalDate().equals(endAt.toLocalDate());
    }
}
