package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.workspace.dto.res.MemberBoardResDto;
import com.nect.api.domain.team.workspace.service.BoardsMemberBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardsMemberBoardFacade {
    private final BoardsMemberBoardService service;

    public MemberBoardResDto getMemberBoard(Long projectId, Long userId) {
        return service.getMemberBoard(projectId, userId);
    }

}
