package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.workspace.dto.req.BoardsBasicInfoUpdateReqDto;
import com.nect.api.domain.team.workspace.dto.res.BoardsBasicInfoGetResDto;
import com.nect.api.domain.team.workspace.dto.res.MissionProgressResDto;
import com.nect.api.domain.team.workspace.service.BoardsBasicInfoService;
import com.nect.api.domain.team.workspace.service.BoardsMissionProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardsFacade {
    private final BoardsBasicInfoService boardsBasicInfoService;
    private final BoardsMissionProgressService missionProgressService;

    public BoardsBasicInfoGetResDto getBoardsBasicInfo(Long projectId, Long userId) {
        return boardsBasicInfoService.getBasicInfo(projectId, userId);
    }

    public void updateBasicInfo(Long projectId, Long userId, BoardsBasicInfoUpdateReqDto req) {
        boardsBasicInfoService.updateBasicInfo(projectId, userId, req);
    }

    public MissionProgressResDto getMissionProgress(Long projectId, Long userId) {
        return missionProgressService.getMissionProgress(projectId, userId);
    }
}
