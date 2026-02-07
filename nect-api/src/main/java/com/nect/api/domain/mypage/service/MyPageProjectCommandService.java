package com.nect.api.domain.mypage.service;

import com.nect.core.repository.matching.RecruitmentRepository;
import com.nect.core.repository.team.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 마이페이지-프로젝트 데이터 추가•수정•삭제 service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class MyPageProjectCommandService {

    private final ProjectRepository projectRepository;
    private final RecruitmentRepository recruitmentRepository;

    // 프로젝트 분야 수정

    // 모집정보 추가

    // 프로젝트 목표 추가

    // 프로젝트 목표 수정

    // 프로젝트 목표 삭제

    // 주요기능 추가

    // 주요기능 수정

    // 주요기능 삭제

    // 서비스 사용자 추가

    // 서비스 사용자 수정

    // 서비스 사용자 삭제

    // 프로젝트 세부 기획 파일 추가

    // 프로젝트 세부 기획 파일 삭제

}
