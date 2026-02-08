package com.nect.api.domain.mypage.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.NectDocumentApiTester;
import com.nect.api.domain.matching.dto.RecruitmentReqDto;
import com.nect.api.domain.matching.dto.RecruitmentResDto;
import com.nect.api.domain.matching.service.RecruitmentService;
import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.service.MyPageProjectCommandService;
import com.nect.api.domain.mypage.service.MyPageProjectQueryService;
import com.nect.api.domain.mypage.service.MypageService;
import com.nect.api.domain.team.project.dto.ProjectUserFieldReqDto;
import com.nect.api.domain.team.project.dto.ProjectUserFieldResDto;
import com.nect.api.domain.team.project.dto.ProjectUserResDto;
import com.nect.api.domain.team.project.service.ProjectUserService;
import com.nect.core.entity.team.enums.PlanFileType;
import com.nect.core.entity.team.enums.ProjectMemberStatus;
import com.nect.core.entity.team.enums.ProjectMemberType;
import com.nect.core.entity.user.enums.InterestField;
import com.nect.core.entity.user.enums.RoleField;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MypageControllerTest extends NectDocumentApiTester {

    @MockitoBean
    private MypageService mypageService;

    @MockitoBean
    private ProjectUserService projectUserService;

    @MockitoBean
    private MyPageProjectCommandService projectCommandService;

    @MockitoBean
    private MyPageProjectQueryService projectQueryService;

    @MockitoBean
    private RecruitmentService recruitmentService;

    @Test
    void getProfile() throws Exception {
        ProfileSettingsDto.ProfileSettingsResponseDto mockResponse = new ProfileSettingsDto.ProfileSettingsResponseDto(
                1L,
                "김준혁",
                "juunbro",
                "test@example.com",
                "DEVELOPER",
                "https://example.com/profile.jpg",
                "안녕하세요! 백엔드 개발자입니다.",
                "Spring Boot, Java, REST API",
                "구직중",
                true,
                "6개월",  // careerDuration
                "백엔드 개발자",  // interestedJob
                "IT/웹모바일",  // interestedField
                new ArrayList<>(),  // careers
                new ArrayList<>(),  // portfolios
                new ArrayList<>(),  // projectHistories
                new ArrayList<>(),  // skills
                "기술 주도형 개발자",  // profileType
                List.of("#프로그래밍전문가", "#금융애플리케이션", "#백엔드개발자", "#효율적협업", "#기술적창의성")  // tags
        );
        given(mypageService.getProfile(1L)).willReturn(mockResponse);

        this.mockMvc.perform(get("/api/v1/mypage/profile")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andDo(document("mypage-get-profile",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("마이페이지")
                                        .summary("마이페이지 프로필 조회")
                                        .description("사용자의 마이페이지 프로필 정보를 조회합니다.")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
                                                fieldWithPath("body.name").type(JsonFieldType.STRING).description("이름"),
                                                fieldWithPath("body.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                                fieldWithPath("body.email").type(JsonFieldType.STRING).description("이메일"),
                                                fieldWithPath("body.role").type(JsonFieldType.STRING).description("역할 (DEVELOPER, DESIGNER, PLANNER, MARKETER)").optional(),
                                                fieldWithPath("body.profileImageUrl").type(JsonFieldType.STRING).description("프로필 사진 URL").optional(),
                                                fieldWithPath("body.bio").type(JsonFieldType.STRING).description("자기소개").optional(),
                                                fieldWithPath("body.coreCompetencies").type(JsonFieldType.STRING).description("핵심 역량").optional(),
                                                fieldWithPath("body.userStatus").type(JsonFieldType.STRING).description("사용자 상태 (예: 재학중, 구직중, 재직중)").optional(),
                                                fieldWithPath("body.isPublicMatching").type(JsonFieldType.BOOLEAN).description("공개 매칭 여부"),
                                                fieldWithPath("body.careerDuration").type(JsonFieldType.STRING).description("경력 기간").optional(),
                                                fieldWithPath("body.interestedJob").type(JsonFieldType.STRING).description("관심 직무").optional(),
                                                fieldWithPath("body.interestedField").type(JsonFieldType.STRING).description("관심 직종").optional(),
                                                fieldWithPath("body.careers").type(JsonFieldType.ARRAY).description("경력 목록"),
                                                fieldWithPath("body.portfolios").type(JsonFieldType.ARRAY).description("포트폴리오 목록"),
                                                fieldWithPath("body.projectHistories").type(JsonFieldType.ARRAY).description("프로젝트 히스토리 목록"),
                                                fieldWithPath("body.skills").type(JsonFieldType.ARRAY).description("스킬 목록"),
                                                fieldWithPath("body.profileType").type(JsonFieldType.STRING).description("AI 프로필 분석 타입 (예: 기술 주도형 개발자)").optional(),
                                                fieldWithPath("body.tags").type(JsonFieldType.ARRAY).description("프로필 분석 키워드 태그 (예: #프로그래밍전문가, #백엔드개발자)").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void updateProfile() throws Exception {
        // Mock 설정
        doNothing().when(mypageService).updateProfile(eq(1L), any(ProfileSettingsDto.ProfileSettingsRequestDto.class));

        // 요청 JSON (모든 필드 포함한 완전한 예시)
        String requestJson = "{"
                + "\"profileImageFileName\": \"kim-junhyeok.jpg\","
                + "\"bio\": \"안녕하세요! 3년차 백엔드 개발자 김준혁입니다. Spring Boot와 Java에 능숙하며 RESTful API 설계 및 구현을 전문으로 합니다.\","
                + "\"coreCompetencies\": \"Spring Boot, Java, REST API, MySQL, Redis, Docker, Kubernetes, AWS\","
                + "\"userStatus\": \"JOB_SEEKING\","
                + "\"isPublicMatching\": true,"
                + "\"careerDuration\": \"6개월\","
                + "\"interestedJob\": \"백엔드 개발자\","
                + "\"interestedField\": \"IT/웹모바일\","
                + "\"careers\": ["
                + "  {"
                + "    \"projectName\": \"대규모 전자상거래 플랫폼 백엔드\","
                + "    \"industryField\": \"E-commerce\","
                + "    \"startDate\": \"2023.1\","
                + "    \"endDate\": \"2024.6\","
                + "    \"isOngoing\": false,"
                + "    \"role\": \"백엔드 개발자\","
                + "    \"achievements\": ["
                + "      {"
                + "        \"title\": \"REST API 성능 최적화\","
                + "        \"content\": \"데이터베이스 쿼리 최적화 및 캐싱 전략 도입으로 응답 시간 60% 단축. Redis를 활용한 세션 관리로 서버 부하 50% 감소.\""
                + "      },"
                + "      {"
                + "        \"title\": \"마이크로서비스 아키텍처 구축\","
                + "        \"content\": \"모놀리식 아키텍처를 마이크로서비스로 리팩토링. 주문, 결제, 배송 서비스 분리로 배포 주기 단축 및 확장성 향상.\""
                + "      },"
                + "      {"
                + "        \"title\": \"Docker/Kubernetes 도입\","
                + "        \"content\": \"컨테이너화 및 쿠버네티스 오케스트레이션으로 배포 자동화 및 인프라 관리 효율화.\""
                + "      }"
                + "    ]"
                + "  },"
                + "  {"
                + "    \"projectName\": \"금융 시스템 API 개발\","
                + "    \"industryField\": \"Finance\","
                + "    \"startDate\": \"2022.3\","
                + "    \"endDate\": \"2022.12\","
                + "    \"isOngoing\": false,"
                + "    \"role\": \"시니어 백엔드 개발자\","
                + "    \"achievements\": ["
                + "      {"
                + "        \"title\": \"결제 시스템 구축\","
                + "        \"content\": \"신용카드, 계좌이체, 디지털 지갑 등 다양한 결제 수단 통합 API 개발. 거래량 1,000만 건/일 처리 가능.\""
                + "      },"
                + "      {"
                + "        \"title\": \"보안 강화\","
                + "        \"content\": \"SSL/TLS, OAuth 2.0, JWT 토큰 기반 인증 시스템 구현. PCI DSS 준수 및 데이터 암호화 적용.\""
                + "      }"
                + "    ]"
                + "  }"
                + "],"
                + "\"portfolios\": ["
                + "  {"
                + "    \"title\": \"Nect - 팀 협업 플랫폼\","
                + "    \"link\": \"https://github.com/example/nect\","
                + "    \"fileUrl\": \"https://example.com/portfolio/nect-presentation.pdf\""
                + "  },"
                + "  {"
                + "    \"title\": \"Spring Boot REST API 튜토리얼\","
                + "    \"link\": \"https://github.com/example/spring-boot-tutorial\","
                + "    \"fileUrl\": null"
                + "  },"
                + "  {"
                + "    \"title\": \"마이크로서비스 아키텍처 가이드\","
                + "    \"link\": null,"
                + "    \"fileUrl\": \"https://example.com/portfolio/microservices-guide.pdf\""
                + "  }"
                + "],"
                + "\"projectHistories\": ["
                + "  {"
                + "    \"projectName\": \"Nect 프로젝트\","
                + "    \"projectImage\": \"https://example.com/project/nect-image.jpg\","
                + "    \"projectDescription\": \"팀 협업 플랫폼 개발 프로젝트입니다.\","
                + "    \"startYearMonth\": \"2024.1\","
                + "    \"endYearMonth\": \"2024.12\""
                + "  },"
                + "  {"
                + "    \"projectName\": \"Spring Boot 마이크로서비스\","
                + "    \"projectImage\": \"https://example.com/project/microservices.jpg\","
                + "    \"projectDescription\": \"마이크로서비스 아키텍처 기반의 백엔드 시스템 구축.\","
                + "    \"startYearMonth\": \"2023.6\","
                + "    \"endYearMonth\": \"2023.11\""
                + "  }"
                + "]"
                + "}";

        this.mockMvc.perform(patch("/api/v1/mypage/profile/save")
                        .header("Authorization", "Bearer mock-token")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(document("mypage-patch-profile",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("마이페이지")
                                        .summary("마이페이지 프로필 수정")
                                        .description("마이페이지 프로필 정보를 부분 수정합니다.\n\n" +
                                                "**수정 가능한 필드**\n" +
                                                "- 기본정보: 프로필 사진 파일명 (profileImageFileName), 자기소개 (bio), 핵심 역량 (coreCompetencies), 사용자 상태 (userStatus), 공개 매칭 여부 (isPublicMatching), 경력 기간 (careerDuration), 관심 직무 (interestedJob), 관심 직종 (interestedField)\n" +
                                                "- 경력관리: 경력 목록 (careers) - 프로젝트명, 산업분야, 기간, 역할, 주요 성과 저장 (projectName, industryField, startDate, endDate, isOngoing, role, achievements)\n" +
                                                "- 포트폴리오: 포트폴리오 목록 (portfolios) - 제목, 외부 링크, 파일 URL 관리 (title, link, fileUrl)\n" +
                                                "- 프로젝트 히스토리: 프로젝트 히스토리 목록 (projectHistories) - 프로젝트명, 이미지, 설명, 기간 관리\n\n" +
                                                "**부분 수정 규칙**\n" +
                                                "- null이거나 작성하지 않은 필드는 기존 값을 유지합니다.\n" +
                                                "- careers, portfolios, projectHistories는 제공된 배열로 완전히 교체됩니다. (기존 데이터 삭제 후 새로운 데이터만 저장)\n" +
                                                "- userStatus는 Enum 값으로 입력받지만 (ENROLLED, JOB_SEEKING, EMPLOYED), 응답에서는 한국어로 변환됩니다 (재학중, 구직중, 재직중).\n" +
                                                "- role도 응답에서 한국어로 변환됩니다 (개발자, 디자이너, 기획자, 마케터).\n" +
                                                "- 유효하지 않은 userStatus 값이면 M002 에러가 반환됩니다.")
                                        .requestFields(
                                                fieldWithPath("profileImageFileName").type(JsonFieldType.STRING).description("프로필 사진 파일명. S3 업로드 후 반환받은 파일명 (예: 550e8400-e29b-41d4-a716-446655440000_profile.jpg)").optional(),
                                                fieldWithPath("bio").type(JsonFieldType.STRING).description("자기소개. 사용자가 작성한 자유로운 형식의 소개글 (예: 안녕하세요! 3년차 백엔드 개발자입니다)").optional(),
                                                fieldWithPath("coreCompetencies").type(JsonFieldType.STRING).description("핵심 역량. 보유 중인 주요 기술 및 역량을 쉼표로 구분하여 작성 (예: Spring Boot, Java, REST API, MySQL)").optional(),
                                                fieldWithPath("userStatus").type(JsonFieldType.STRING).description("사용자 상태. 현재 상태를 나타내는 한국어 값 (재학중, 구직중, 재직중)").optional(),
                                                fieldWithPath("isPublicMatching").type(JsonFieldType.BOOLEAN).description("공개 매칭 여부. true면 다른 사용자에게 프로필 공개, false면 비공개").optional(),
                                                fieldWithPath("careerDuration").type(JsonFieldType.STRING).description("경력 기간. 현재 직무의 경력 기간 (예: 6개월, 1년, 3년)").optional(),
                                                fieldWithPath("interestedJob").type(JsonFieldType.STRING).description("관심 직무. 관심있는 직무/역할을 자유롭게 입력 (예: 백엔드 개발자, 풀스택 개발자)").optional(),
                                                fieldWithPath("interestedField").type(JsonFieldType.STRING).description("관심 직종. 관심있는 산업/분야를 자유롭게 입력 (예: IT/웹모바일, 핀테크)").optional(),
                                                fieldWithPath("careers").type(JsonFieldType.ARRAY).description("경력 목록. 사용자의 과거 프로젝트/직무 경력 정보 배열").optional(),
                                                fieldWithPath("careers[].projectName").type(JsonFieldType.STRING).description("프로젝트/직무명. 진행했던 프로젝트 또는 역할 이름 (예: 대규모 전자상거래 플랫폼 백엔드)").optional(),
                                                fieldWithPath("careers[].industryField").type(JsonFieldType.STRING).description("산업분야. 해당 프로젝트/직무의 산업 (예: E-commerce, Finance, Healthcare)").optional(),
                                                fieldWithPath("careers[].startDate").type(JsonFieldType.STRING).description("시작일. 경력 시작 년월 (형식: YYYY.M, 예: 2023.1)").optional(),
                                                fieldWithPath("careers[].endDate").type(JsonFieldType.STRING).description("종료일. 경력 종료 년월 (형식: YYYY.M, 예: 2024.6). isOngoing이 true면 null 가능").optional(),
                                                fieldWithPath("careers[].isOngoing").type(JsonFieldType.BOOLEAN).description("진행중 여부. true면 현재 진행 중, false면 완료된 경력").optional(),
                                                fieldWithPath("careers[].role").type(JsonFieldType.STRING).description("역할/직책. 해당 경력에서의 역할 (예: 백엔드 개발자, 시니어 소프트웨어 엔지니어)").optional(),
                                                fieldWithPath("careers[].achievements").type(JsonFieldType.ARRAY).description("주요 성과 목록. 해당 경력에서 이룬 주요 성과/업적 배열").optional(),
                                                fieldWithPath("careers[].achievements[].title").type(JsonFieldType.STRING).description("성과 제목. 성과를 한 문장으로 요약한 제목 (예: REST API 성능 최적화)").optional(),
                                                fieldWithPath("careers[].achievements[].content").type(JsonFieldType.STRING).description("성과 내용. 성과에 대한 상세한 설명 (예: 데이터베이스 쿼리 최적화로 응답시간 60% 단축)").optional(),
                                                fieldWithPath("portfolios").type(JsonFieldType.ARRAY).description("포트폴리오 목록. 사용자가 작성/완성한 프로젝트, 글, 파일 등의 포트폴리오 배열").optional(),
                                                fieldWithPath("portfolios[].title").type(JsonFieldType.STRING).description("포트폴리오 제목. 포트폴리오 아이템의 이름 (예: Nect - 팀 협업 플랫폼)").optional(),
                                                fieldWithPath("portfolios[].link").type(JsonFieldType.STRING).description("포트폴리오 링크. 포트폴리오로 연결되는 외부 URL (예: GitHub 링크, 블로그 링크 등). null 가능").optional(),
                                                fieldWithPath("portfolios[].fileUrl").type(JsonFieldType.STRING).description("포트폴리오 파일 URL. 서버에 업로드된 포트폴리오 파일 (PDF, 이미지 등) URL. null 가능").optional(),
                                                fieldWithPath("projectHistories").type(JsonFieldType.ARRAY).description("프로젝트 히스토리 목록. 사용자가 진행한 프로젝트들의 정보 배열").optional(),
                                                fieldWithPath("projectHistories[].projectName").type(JsonFieldType.STRING).description("프로젝트 이름. 진행한 프로젝트의 이름 (예: Nect 프로젝트)").optional(),
                                                fieldWithPath("projectHistories[].projectImage").type(JsonFieldType.STRING).description("프로젝트 이미지 URL. 프로젝트 사진 URL (예: https://example.com/project/nect.jpg). null 가능").optional(),
                                                fieldWithPath("projectHistories[].projectDescription").type(JsonFieldType.STRING).description("프로젝트 설명. 프로젝트에 대한 상세한 설명 (예: 팀 협업 플랫폼 개발)").optional(),
                                                fieldWithPath("projectHistories[].startYearMonth").type(JsonFieldType.STRING).description("시작 년월. 프로젝트 시작 시간 (형식: YYYY.MM, 예: 2024.1)").optional(),
                                                fieldWithPath("projectHistories[].endYearMonth").type(JsonFieldType.STRING).description("종료 년월. 프로젝트 종료 시간 (형식: YYYY.MM, 예: 2024.12)").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void editProjectField() throws Exception {
        long projectId = 1L;

        doNothing().when(projectCommandService)
                .changeProjectInterest(eq(projectId), eq(InterestField.IT_WEB_MOBILE));

        mockMvc.perform(
                        patch("/api/v1/mypage/projects/{projectId}/project-field?field=IT_WEB_MOBILE", projectId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("mypage-project-field-edit",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("프로젝트 분야 수정")
                                .description("프로젝트 관심 분야 선택 상태를 변경합니다.")
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .queryParameters(
                                        parameterWithName("field").description("프로젝트 관심 분야(InterestField)")
                                )
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer AccessToken")
                                )
                                .responseFields(
                                        fieldWithPath("status").type(JsonFieldType.OBJECT).description("응답 상태"),
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").optional().type(JsonFieldType.STRING).description("상태 설명"),
                                        fieldWithPath("body").type(JsonFieldType.NULL).optional().description("응답 바디 (없음)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void uploadPlanFile_FILE() throws Exception {
        long projectId = 1L;

        MockMultipartFile name = new MockMultipartFile(
                "name",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "기획서".getBytes()
        );
        MockMultipartFile planFileType = new MockMultipartFile(
                "planFileType",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "\"FILE\"".getBytes()
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy pdf bytes".getBytes()
        );

        doNothing().when(projectCommandService)
                .addPlanFile(eq(projectId), anyString(), eq(PlanFileType.FILE), any(), any());

        mockMvc.perform(
                        multipart("/api/v1/mypage/projects/{projectId}/plan-file", projectId)
                                .file(name)
                                .file(planFileType)
                                .file(file)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("mypage-plan-file-upload",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(
                                partWithName("name").description("파일 표시명"),
                                partWithName("planFileType").description("파일 타입 (FILE 또는 LINK)"),
                                partWithName("file").description("업로드할 파일(MultipartFile)"),
                                partWithName("link").description("링크 URL (planFileType=LINK일 때만 사용)").optional()
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("프로젝트 세부 기획 파일 추가")
                                .description(
                                        "프로젝트의 세부 기획 파일(업로드 또는 링크)을 추가합니다.\n\n" +
                                        "**설명 작성 가이드(Description 텍스트 규칙)**\n" +
                                        "- 1줄 요약: 무엇을 하는 API인지 간단히 서술\n" +
                                        "- 입력 규칙: planFileType별 필수 파트를 명시\n" +
                                        "- 제약/예외: 파일 확장자/용량 제한 등 핵심 제약을 적기\n\n" +
                                        "**입력 규칙**\n" +
                                        "- planFileType=FILE: name, planFileType, file 필수 (link는 무시)\n" +
                                        "- planFileType=LINK: name, planFileType, link 필수 (file은 무시)\n"
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                )
                                .responseFields(
                                        fieldWithPath("status").type(JsonFieldType.OBJECT).description("응답 상태"),
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").optional().type(JsonFieldType.STRING).description("상태 설명"),
                                        fieldWithPath("body").type(JsonFieldType.NULL).optional().description("응답 바디 (없음)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void editPlanFile_LINK() throws Exception {
        long projectId = 1L;
        long planFileId = 10L;

        MockMultipartFile name = new MockMultipartFile(
                "name",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "Figma 링크".getBytes()
        );
        MockMultipartFile planFileType = new MockMultipartFile(
                "planFileType",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "\"LINK\"".getBytes()
        );
        MockMultipartFile link = new MockMultipartFile(
                "link",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "https://figma.com/file/abc".getBytes()
        );

        doNothing().when(projectCommandService)
                .editPlanFile(eq(projectId), eq(planFileId), anyString(), eq(PlanFileType.LINK), any(), anyString());

        mockMvc.perform(
                        multipart("/api/v1/mypage/projects/{projectId}/plan-file/{planFileId}", projectId, planFileId)
                                .file(name)
                                .file(planFileType)
                                .file(link)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                })
                )
                .andExpect(status().isOk())
                .andDo(document("mypage-plan-file-edit",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(
                                partWithName("name").description("파일 표시명"),
                                partWithName("planFileType").description("파일 타입 (FILE 또는 LINK)"),
                                partWithName("file").description("업로드할 파일(MultipartFile) - planFileType=FILE일 때만 사용").optional(),
                                partWithName("link").description("링크 URL - planFileType=LINK일 때만 사용").optional()
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("프로젝트 세부 기획 파일 수정")
                                .description(
                                        "프로젝트 세부 기획 파일의 내용을 수정합니다.\n\n" +
                                        "**설명 작성 가이드(Description 텍스트 규칙)**\n" +
                                        "- 1줄 요약으로 변경 범위를 먼저 설명\n" +
                                        "- planFileType 변경 가능 여부와 필수 파트를 명시\n" +
                                        "- 기존 FILE ↔ LINK 전환 시 처리(기존 파일 삭제 등) 요약\n\n" +
                                        "**입력 규칙**\n" +
                                        "- planFileType=FILE: name, planFileType, file 필수\n" +
                                        "- planFileType=LINK: name, planFileType, link 필수\n"
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID"),
                                        parameterWithName("planFileId").description("세부 기획 파일 ID")
                                )
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                )
                                .responseFields(
                                        fieldWithPath("status").type(JsonFieldType.OBJECT).description("응답 상태"),
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").optional().type(JsonFieldType.STRING).description("상태 설명"),
                                        fieldWithPath("body").type(JsonFieldType.NULL).optional().description("응답 바디 (없음)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void removePlanFile() throws Exception {
        long projectId = 1L;
        long planFileId = 10L;

        doNothing().when(projectCommandService).removePlanFile(eq(projectId), eq(planFileId));

        mockMvc.perform(
                        delete("/api/v1/mypage/projects/{projectId}/plan-file/{planFileId}", projectId, planFileId)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("mypage-plan-file-remove",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("프로젝트 세부 기획 파일 삭제")
                                .description(
                                        "프로젝트 세부 기획 파일을 삭제합니다.\n\n" +
                                        "**설명 작성 가이드(Description 텍스트 규칙)**\n" +
                                        "- 1줄 요약으로 삭제 대상과 범위를 명확히\n" +
                                        "- 삭제 시 파일 스토리지 제거 여부를 간단히 명시\n"
                                )
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID"),
                                        parameterWithName("planFileId").description("세부 기획 파일 ID")
                                )
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).description("Bearer Access Token")
                                )
                                .responseFields(
                                        fieldWithPath("status").type(JsonFieldType.OBJECT).description("응답 상태"),
                                        fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                        fieldWithPath("status.description").optional().type(JsonFieldType.STRING).description("상태 설명"),
                                        fieldWithPath("body").type(JsonFieldType.NULL).optional().description("응답 바디 (없음)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getProfileAnalysis() throws Exception {
        // given
        ProfileSettingsDto.ProfileAnalysisResponseDto mockResponse = new ProfileSettingsDto.ProfileAnalysisResponseDto(
                "기술 주도형 개발자",
                List.of("#프로그래밍전문가", "#금융애플리케이션", "#백엔드개발자", "#효율적협업", "#기술적창의성")
        );
        given(mypageService.getProfileAnalysis(1L)).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(get("/api/v1/mypage/profile-analysis")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andDo(document("mypage-get-profile-analysis",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("마이페이지")
                                        .summary("마이페이지 프로필 분석 불러오기")
                                        .description("데이터베이스에 저장된 AI 프로필 분석 결과를 조회합니다. 분석 결과가 없으면 profileType과 tags는 null입니다.")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.profileType").type(JsonFieldType.STRING).description("AI 프로필 분석 타입 (예: 기술 주도형 개발자)").optional(),
                                                fieldWithPath("body.tags").type(JsonFieldType.ARRAY).description("프로필 분석 키워드 태그 (예: #프로그래밍전문가, #백엔드개발자)").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void updateProjectUserField() throws Exception {
        Long projectUserId = 1L;

        ProjectUserFieldResDto resDto = ProjectUserFieldResDto.builder()
                .projectUserId(projectUserId)
                .field(RoleField.CUSTOM)
                .customField("Designer")
                .build();

        given(projectUserService.changeProjectUserFieldInProject(eq(projectUserId), any(ProjectUserFieldReqDto.class)))
                .willReturn(resDto);

        String requestJson = """
                {
                  "field": "CUSTOM",
                  "customField": "Designer"
                }
                """;

        mockMvc.perform(patch("/api/v1/mypage/{projectUserId}/field", projectUserId)
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(document("patch-project-user-field",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("프로젝트 멤버 필드(파트) 변경")
                                .description("프로젝트 내 멤버의 필드(파트) 및 커스텀 필드를 변경합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .requestFields(
                                        fieldWithPath("field").description("변경할 필드 식별자"),
                                        fieldWithPath("customField").description("커스텀 필드명 (RoleField.CUSTOM 인 경우 필수)").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.projectUserId").description("프로젝트 유저 ID"),
                                        fieldWithPath("body.field").description("적용된 필드"),
                                        fieldWithPath("body.customField").description("적용된 커스텀 필드명").optional()
                                )
                                .build()
                        )
                ));
    }

    @Test
    void kickProjectUser() throws Exception {
        ProjectUserResDto resDto = ProjectUserResDto.builder()
                .id(1L)
                .userId(1L)
                .projectId(1L)
                .field(RoleField.BACKEND)
                .memberType(ProjectMemberType.MEMBER)
                .memberStatus(ProjectMemberStatus.KICKED)
                .build();

        given(projectUserService.kickProjectUser(anyLong(), eq(1L)))
                .willReturn(resDto);

        mockMvc.perform(patch("/api/v1/mypage/{projectUserId}/kick", 1L)
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("patch-project-user-kick",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("프로젝트 멤버 강퇴")
                                .description("프로젝트에서 특정 멤버를 강퇴합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("프로젝트 유저 ID"),
                                        fieldWithPath("body.userId").description("유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.memberType").description("멤버 타입"),
                                        fieldWithPath("body.memberStatus").description("멤버 상태 (KICK)")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void updateProjectUserType() throws Exception {
        ProjectUserResDto resDto = ProjectUserResDto.builder()
                .id(1L)
                .userId(1L)
                .projectId(1L)
                .field(RoleField.BACKEND)
                .memberType(ProjectMemberType.LEAD)
                .memberStatus(ProjectMemberStatus.ACTIVE)
                .build();

        given(projectUserService.changeProjectUserTypeInProject(anyLong(), eq(1L), eq(ProjectMemberType.LEAD)))
                .willReturn(resDto);

        String requestJson = """
                {
                  "memberType": "LEAD"
                }
                """;

        mockMvc.perform(patch("/api/v1/mypage/{projectUserId}/type", 1L)
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(document("patch-project-user-type",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("프로젝트 멤버 타입 변경")
                                .description("프로젝트에서 특정 멤버의 타입을 변경합니다. (LEADER | LEAD | MEMBER)")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .requestFields(
                                        fieldWithPath("memberType").description("변경할 프로젝트 멤버 타입")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body").description("응답 데이터"),
                                        fieldWithPath("body.id").description("프로젝트 유저 ID"),
                                        fieldWithPath("body.userId").description("유저 ID"),
                                        fieldWithPath("body.projectId").description("프로젝트 ID"),
                                        fieldWithPath("body.field").description("분야"),
                                        fieldWithPath("body.memberType").description("멤버 타입 (변경된 타입)"),
                                        fieldWithPath("body.memberStatus").description("멤버 상태")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void createRecruitment() throws Exception {
        Long projectId = 1L;

        RecruitmentResDto.EnrollRecruitmentResDto resDto = RecruitmentResDto.EnrollRecruitmentResDto.builder()
                .recruitmentId(10L)
                .roleField(RoleField.BACKEND)
                .customField(null)
                .capacity(3)
                .requirements(List.of("Spring Boot 프레임워크를 사용한 경험이 있어야 합니다.", "Java언어를 능숙하게 다룰 수 있어야 합니다."))
                .build();

        given(recruitmentService.enrollRecruitment(anyLong(), eq(projectId), any(RecruitmentReqDto.EnrollRecruitmentReqDto.class)))
                .willReturn(resDto);

        String requestJson = """
            {
              "roleField": "BACKEND",
              "capacity": 3,
              "requirements": ["Spring Boot 프레임워크를 사용한 경험이 있어야 합니다.", "Java언어를 능숙하게 다룰 수 있어야 합니다."]
            }
            """;

        mockMvc.perform(post("/api/v1/mypage/{projectId}/recruitments", projectId)
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(document("mypage-post-recruitment",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("모집정보 생성")
                                .description("프로젝트에 대한 모집정보를 추가합니다. 작성자는 프로젝트 리더여야 합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .requestFields(
                                        fieldWithPath("roleField").description("모집 분야 식별자 (RoleField enum)"),
                                        fieldWithPath("customField").type(JsonFieldType.STRING)
                                                .description("커스텀 필드명 (roleField가 CUSTOM일 때 필수)").optional(),
                                        fieldWithPath("capacity").description("모집 인원 수"),
                                        fieldWithPath("requirements").type(JsonFieldType.ARRAY)
                                                .description("요구사항 목록").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body.recruitmentId").description("생성된 모집 ID"),
                                        fieldWithPath("body.roleField").description("모집 분야"),
                                        fieldWithPath("body.customField").description("커스텀 필드명").optional(),
                                        fieldWithPath("body.capacity").description("모집 인원 수"),
                                        fieldWithPath("body.requirements").description("요구사항 목록")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void updateRecruitment() throws Exception {
        Long projectId = 1L;
        Long recruitmentId = 10L;

        RecruitmentResDto.EnrollRecruitmentResDto resDto = RecruitmentResDto.EnrollRecruitmentResDto.builder()
                .recruitmentId(recruitmentId)
                .roleField(RoleField.BACKEND)
                .customField(null)
                .capacity(2)
                .requirements(List.of("Django 프레임워크를 사용한 경험이 있어야 합니다.", "python언어를 능숙하게 다룰 줄 알아야 합니다."))
                .build();

        given(recruitmentService.updateRecruitment(anyLong(), eq(projectId), eq(recruitmentId), any(RecruitmentReqDto.EnrollRecruitmentReqDto.class)))
                .willReturn(resDto);

        String requestJson = """
            {
              "roleField": "BACKEND",
              "capacity": 2,
              "requirements": ["Django 프레임워크를 사용한 경험이 있어야 합니다.", "python언어를 능숙하게 다룰 줄 알아야 합니다."]
            }
            """;

        mockMvc.perform(put("/api/v1/mypage/{projectId}/recruitments/{recruitmentId}", projectId, recruitmentId)
                        .header("Authorization", "Bearer AccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(document("mypage-put-recruitment",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("모집정보 수정")
                                .description("기존 모집정보를 수정합니다. 작성자는 프로젝트 리더여야 합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .requestFields(
                                        fieldWithPath("roleField").description("모집 분야 식별자 (RoleField enum)"),
                                        fieldWithPath("customField").type(JsonFieldType.STRING)
                                                .description("커스텀 필드명 (roleField가 CUSTOM일 때 필수)").optional(),
                                        fieldWithPath("capacity").description("모집 인원 수"),
                                        fieldWithPath("requirements").type(JsonFieldType.ARRAY)
                                                .description("요구사항 목록").optional()
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body.recruitmentId").description("모집 ID"),
                                        fieldWithPath("body.roleField").description("모집 분야"),
                                        fieldWithPath("body.customField").description("커스텀 필드명").optional(),
                                        fieldWithPath("body.capacity").description("모집 인원 수"),
                                        fieldWithPath("body.requirements").description("요구사항 목록")
                                )
                                .build()
                        )
                ));
    }

    @Test
    void getRecruitmentsByProject() throws Exception {
        Long projectId = 1L;

        List<RecruitmentResDto.EnrollRecruitmentResDto> resList = List.of(
                RecruitmentResDto.EnrollRecruitmentResDto.builder()
                        .recruitmentId(10L)
                        .roleField(RoleField.BACKEND)
                        .customField(null)
                        .capacity(3)
                        .requirements(List.of("Django 프레임워크를 사용한 경험이 있어야 합니다.", "python언어를 능숙하게 다룰 줄 알아야 합니다."))
                        .build(),
                RecruitmentResDto.EnrollRecruitmentResDto.builder()
                        .recruitmentId(11L)
                        .roleField(RoleField.FRONTEND)
                        .customField(null)
                        .capacity(1)
                        .requirements(List.of("디자인 경험이 있으신 분을 선호합니다.", "모두 환영해요."))
                        .build()
        );

        given(recruitmentService.getRecruitmentsByProject(projectId)).willReturn(resList);

        mockMvc.perform(get("/api/v1/mypage/{projectId}/recruitments", projectId)
                        .header("Authorization", "Bearer AccessToken"))
                .andExpect(status().isOk())
                .andDo(document("mypage-get-recruitments",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("마이페이지")
                                .summary("프로젝트 모집정보 조회")
                                .description("프로젝트에 등록된 모든 모집정보를 조회합니다.")
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),

                                        fieldWithPath("body[].recruitmentId").description("모집 ID"),
                                        fieldWithPath("body[].roleField").description("모집 분야"),
                                        fieldWithPath("body[].customField").description("커스텀 필드").optional(),
                                        fieldWithPath("body[].capacity").description("모집 인원 수"),
                                        fieldWithPath("body[].requirements").description("요구사항 목록")
                                )
                                .build()
                        )
                ));
    }
}
