package com.nect.api.domain.mypage.service;

import com.nect.api.domain.mypage.converter.ProjectListConverter;
import com.nect.api.domain.team.project.enums.code.ProjectErrorCode;
import com.nect.api.domain.team.project.exception.ProjectException;
import com.nect.api.domain.team.file.util.FileUploadValidator;
import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.global.exception.CustomException;
import com.nect.api.global.infra.S3Service;
import com.nect.core.entity.team.Project;
import com.nect.core.entity.team.ProjectInterest;
import com.nect.core.entity.team.ProjectPlanFile;
import com.nect.core.entity.team.enums.FileExt;
import com.nect.core.entity.team.enums.PlanFileType;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectInterestFieldRepository;
import com.nect.core.repository.team.ProjectPlanFileRepository;
import com.nect.core.repository.team.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.function.BiConsumer;

// 마이페이지-프로젝트 데이터 추가•수정•삭제 service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class MyPageProjectCommandService {

    private final ProjectRepository projectRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final ProjectPlanFileRepository planFileRepository;
    private final ProjectInterestFieldRepository projectInterestFieldRepository;
    private final S3Service s3Service;

    private final ProjectListConverter listConverter;

    // 프로젝트 분야 수정
    public void changeProjectInterest(Long projectId, InterestField interestField){

        // 검증
        if (projectId == null) {
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }
        if (interestField == null) {
            throw new CustomException(CommonResponseCode.REQUEST_BODY_MISSING_ERROR);
        }

        findProject(projectId);

        ProjectInterest projectInterest = projectInterestFieldRepository.findByProjectIdAndInterestField(projectId, interestField)
                .orElseThrow(() -> new CustomException(CommonResponseCode.NOT_FOUND_ERROR));

        projectInterest.changeSelected(!projectInterest.getSelected());
        projectInterestFieldRepository.save(projectInterest);
    }

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
    public void addPlanFile(Long projectId, String name, PlanFileType planFileType, MultipartFile file, String link) {

        String fileName = null;
        FileExt fileExt = null;

        if (projectId == null) { // 필수 항목
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);
        }
        if (name == null || planFileType == null) // 필수 항목들
            throw new CustomException(CommonResponseCode.REQUEST_BODY_MISSING_ERROR);


        // FILE일 경우와 LINK일 경우를 나눔
        if (planFileType == PlanFileType.FILE) { // 파일일 경우

            // 파일 존재하는지 검증
            FileUploadValidator.validateNotEmpty(file);

            String originalName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                    ? "file"
                    : file.getOriginalFilename();
            fileExt = FileUploadValidator.resolveExtOrThrow(originalName);

            // 파일 사이즈 검증
            FileUploadValidator.validateSizeOrThrow(fileExt, file.getSize());

            try {
                fileName = s3Service.uploadFile(file); // 업로드 후 DB key 받기
            } catch (Exception e) {
                throw new CustomException(CommonResponseCode.INTERNAL_SERVER_ERROR);
            }

        } else if (planFileType == PlanFileType.LINK) { // LINK일 경우
            if (link == null || link.isBlank())
                throw new CustomException(CommonResponseCode.REQUEST_BODY_MISSING_ERROR);

            fileName = link;
        }

        Project project = findProject(projectId);
        ProjectPlanFile planFile = ProjectPlanFile.builder()
                .name(name)
                .fileName(fileName)
                .planFileType(planFileType)
                .fileExt(fileExt)
                .project(project)
                .build();

        planFileRepository.save(planFile);


    }

    // 프로젝트 세부 기획 파일 수정
    public void editPlanFile(Long projectId, Long planFileId, String name, PlanFileType planFileType, MultipartFile file, String link) {

        // null 검증
        if (projectId == null || planFileId == null)
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);

        if (name == null || planFileType == null)
            throw new CustomException(CommonResponseCode.REQUEST_BODY_MISSING_ERROR);

        // 조회
        ProjectPlanFile planFile = planFileRepository.findByIdAndProjectId(planFileId, projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

        // FILE과 LINK에 대한 처리
        if (planFileType == PlanFileType.FILE) { // FILE 수정

            // 파일 비어있는지 확인
            FileUploadValidator.validateNotEmpty(file);

            String originalName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                    ? "file"
                    : file.getOriginalFilename();
            FileExt fileExt = FileUploadValidator.resolveExtOrThrow(originalName);

            // 파일 크기 검증
            FileUploadValidator.validateSizeOrThrow(fileExt, file.getSize());

            String fileName;
            try {
                fileName = s3Service.uploadFile(file);
            } catch (Exception e) {
                throw new CustomException(CommonResponseCode.INTERNAL_SERVER_ERROR);
            }

            if (planFile.getPlanFileType() == PlanFileType.FILE && planFile.getFileName() != null)
                s3Service.deleteByFileName(planFile.getFileName());

            // 수정
            planFile.changeFile(fileName);
            planFile.changePlanFileType(PlanFileType.FILE);
            planFile.changeFileExt(fileExt);

        } else if (planFileType == PlanFileType.LINK) { // LINK 수정

            if (link == null || link.isBlank())
                throw new CustomException(CommonResponseCode.REQUEST_BODY_MISSING_ERROR);


            if (planFile.getPlanFileType() == PlanFileType.FILE && planFile.getFileName() != null)
                s3Service.deleteByFileName(planFile.getFileName());

            // 파일 수정
            planFile.changeFile(link);
            planFile.changePlanFileType(PlanFileType.LINK);
            planFile.changeFileExt(null);
        }

        // 수정 후 저장
        planFile.changeName(name);
        planFileRepository.save(planFile);
    }


    // 프로젝트 세부 기획 파일 삭제
    public void removePlanFile(Long projectId, Long planFileId) {
        if (projectId == null || planFileId == null)
            throw new CustomException(CommonResponseCode.MISSING_REQUEST_PARAMETER_ERROR);


        ProjectPlanFile planFile = planFileRepository.findByIdAndProjectId(planFileId, projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));

        if (planFile.getPlanFileType() == PlanFileType.FILE && planFile.getFileName() != null) {
            s3Service.deleteByFileName(planFile.getFileName());
        }

        planFileRepository.delete(planFile);
    }


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
