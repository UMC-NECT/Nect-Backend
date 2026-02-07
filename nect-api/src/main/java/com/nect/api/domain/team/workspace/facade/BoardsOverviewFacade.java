package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.workspace.dto.res.*;
import com.nect.api.domain.team.workspace.enums.PostSort;
import com.nect.core.entity.team.workspace.enums.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardsOverviewFacade {

    private final BoardsFacade boardsFacade;
    private final BoardsMemberBoardFacade memberBoardFacade;
    private final BoardsScheduleFacade scheduleFacade;
    private final BoardsSharedDocumentFacade sharedDocumentFacade;

    private final PostFacade postFacade;

    public BoardsOverviewResDto getOverview(
            Long projectId,
            Long userId,
            Integer year,
            Integer month,
            String from,
            int scheduleLimit,
            int docsLimit,
            int postsLimit,
            PostType postType
    ) {
        BoardsBasicInfoGetResDto basicInfo =
                boardsFacade.getBoardsBasicInfo(projectId, userId);

        MissionProgressResDto missionProgress =
                boardsFacade.getMissionProgress(projectId, userId);

        MemberBoardResDto members =
                memberBoardFacade.getMemberBoard(projectId, userId);

        ScheduleUpcomingResDto upcomingSchedules =
                scheduleFacade.getUpcoming(projectId, userId, from, scheduleLimit);

        SharedDocumentsPreviewResDto sharedDocumentsPreview =
                sharedDocumentFacade.getPreview(projectId, userId, docsLimit);

        // 기본값 : 공지로 설정
        PostType safeType = (postType == null) ? PostType.NOTICE : postType;
        PostListResDto postsPreview =
                postFacade.getPostList(projectId, userId, safeType,0, postsLimit);

        CalendarMonthIndicatorsResDto calendarIndicators = null;
        if (year != null && month != null) {
            calendarIndicators = scheduleFacade.getMonthIndicators(projectId, userId, year, month);
        }

        return BoardsOverviewResDto.of(
                basicInfo,
                missionProgress,
                members,
                upcomingSchedules,
                sharedDocumentsPreview,
                postsPreview,
                calendarIndicators
        );
    }
}
