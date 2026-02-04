package com.nect.api.domain.mypage.controller;

import com.nect.api.domain.mypage.dto.ProfileSettingsDto;
import com.nect.api.domain.mypage.service.MypageService;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.NectDocumentApiTester;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.ArrayList;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MypageControllerTest extends NectDocumentApiTester {

    @MockitoBean
    private MypageService mypageService;

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
                new ArrayList<>()   // skills
        );
        given(mypageService.getProfile(1L)).willReturn(mockResponse);

        this.mockMvc.perform(get("/api/v1/mypage/profile")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andDo(document("mypage-get-profile",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("mypage")
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
                                                fieldWithPath("body.skills").type(JsonFieldType.ARRAY).description("스킬 목록")
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
                + "\"profileImageUrl\": \"https://example.com/profile/kim-junhyeok.jpg\","
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
                                        .tag("mypage")
                                        .summary("마이페이지 프로필 수정")
                                        .description("마이페이지 프로필 정보를 부분 수정합니다.\n\n" +
                                                "**수정 가능한 필드**\n" +
                                                "- 기본정보: 프로필 사진 (profileImageUrl), 자기소개 (bio), 핵심 역량 (coreCompetencies), 사용자 상태 (userStatus), 공개 매칭 여부 (isPublicMatching), 경력 기간 (careerDuration), 관심 직무 (interestedJob), 관심 직종 (interestedField)\n" +
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
                                                fieldWithPath("profileImageUrl").type(JsonFieldType.STRING).description("프로필 사진 URL. 사용자 프로필 이미지 주소 (예: https://example.com/profile.jpg)").optional(),
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
}