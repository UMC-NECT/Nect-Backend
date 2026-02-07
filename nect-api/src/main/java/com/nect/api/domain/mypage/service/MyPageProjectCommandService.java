package com.nect.api.domain.mypage.service;

import com.nect.api.domain.mypage.converter.ProjectListConverter;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.global.exception.CustomException;
import com.nect.core.entity.team.Project;
import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.BiConsumer;

// 마이페이지-프로젝트 데이터 추가•수정•삭제 service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class MyPageProjectCommandService {

    private final ProjectRepository projectRepository;
    private final RecruitmentRepository recruitmentRepository;

    private final ProjectListConverter listConverter;

    // 프로젝트 분야 수정

    // 모집정보 추가

    // 프로젝트 목표 추가
    public void changePurpose(Long projectId, List<String> contentList) {
        changeProjectList(projectId, contentList, Project::setPurposes);
    }

    // 주요기능 추가
    public void changeMainFunctions(Long projectId, List<String> contentList) {
        changeProjectList(projectId, contentList, Project::setMainFunctions);
    }

    // 서비스 사용자 추가
    public void changeServiceUsers(Long projectId, List<String> contentList) {
        changeProjectList(projectId, contentList, Project::setServiceUsers);
    }

    // 프로젝트 세부 기획 파일 추가

    // 프로젝트 세부 기획 파일 삭제


    // 프로젝트 조회
    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    // 목차 형식으로 되어있는 데이터 수정
    // 프로젝트 목표, 주요기능, 서비스 사용자에서 사용
    private void changeProjectList(
            Long projectId,
            List<String> contentList,
            BiConsumer<Project, String> updater
    ) {
        if (projectId == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }
        if (contentList == null) { // 변동하지 않음
            throw new CustomException(CommonResponseCode.REQUEST_BODY_MISSING_ERROR);
        }

        Project project = findProject(projectId);
        String convertedList = listConverter.convertToDatabaseColumn(contentList);

        updater.accept(project, convertedList);
        projectRepository.save(project);
    }

}
