package com.nect.api.analysis;



import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.analysis.dto.res.ProjectCreateResponseDto;
import com.nect.api.domain.analysis.service.IdeaAnalysisService;
import com.nect.api.domain.team.project.service.ProjectService;
import com.nect.api.global.jwt.JwtUtil;
import com.nect.api.global.jwt.service.TokenBlacklistService;
import com.nect.api.global.security.UserDetailsImpl;
import com.nect.api.global.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
class IdeaAnalysisProjectTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IdeaAnalysisService ideaAnalysisService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String TEST_ACCESS_TOKEN = "Bearer AccessToken";

    @BeforeEach
    void setUpAuth() {
        // JWT 검증 모킹
        doNothing().when(jwtUtil).validateToken(anyString());
        given(tokenBlacklistService.isBlacklisted(anyString())).willReturn(false);
        given(jwtUtil.getUserIdFromToken(anyString())).willReturn(1L);
        given(userDetailsService.loadUserByUsername(anyString())).willReturn(
                UserDetailsImpl.builder()
                        .userId(1L)
                        .roles(List.of("ROLE_USER"))
                        .build()
        );
    }

    @Test
    @DisplayName("분석서 기반 프로젝트 생성 API")
    void 분석서_기반_프로젝트_생성_API() throws Exception {
        // given
        given(projectService.createProjectFromAnalysis(anyLong(), any()))
                .willReturn(mockProjectCreateResponse());

        // when & then
        mockMvc.perform(post("/api/v1/analysis/{analysisId}/project", 1L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("analysis-create-project",
                        resource(ResourceSnippetParameters.builder()
                                .tag("아이디어 분석")
                                .summary("분석서 기반 프로젝트 생성")
                                .description("""
                                       
                                     
                                        ## 프로젝트 생성 프로세스
                                        
                                        ### 1. 데이터 복사 
                                        분석서의 데이터를 프로젝트 전용 테이블로 복사합니다:
                                        - **Project**: 프로젝트 기본 정보 (제목, 상태, 모집 상태 등)
                                        - **ProjectTeamRole**: 팀 구성 (역할별 필요 인원)
                                        - **ProjectWeeklyPlan**: 주차별 계획
                                        - **ProjectWeeklyTask**: 주차별 역할 태스크
                                        - **ProjectImprovementPoint**: 개선점
                                        
                                        ### 2. 스냅샷 방식을 사용하는 이유
                                                                             
                                        - 분석서 삭제 시에도 프로젝트 데이터는 유지됨                                      
                                        - 두 단계는 생명주기가 다르므로 독립적으로 관리해야 함
                                                                                                                      
                                        - 분석서를 다시 실행하거나 수정해도 기존 프로젝트에 영향 없음                                                                                                                     
                                        - 생성 후 각 프로젝트마다 독립적으로 수정 가능
                                       
                                     
                                        
                                   
                                        """)
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("analysisId").description("분석서 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("응답 상태 코드"),
                                        fieldWithPath("status.message").description("응답 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional(),

                                        fieldWithPath("body.project_id").description("생성된 프로젝트 ID"),
                                        fieldWithPath("body.project_title").description("프로젝트 제목 (분석서의 추천명 1번)"),
                                        fieldWithPath("body.message").description("성공 메시지")
                                )
                                .build()
                        )
                ));
    }

    @Test
    @DisplayName("분석서 삭제 API")
    void 분석서_삭제_API() throws Exception {
        // given
        doNothing().when(ideaAnalysisService).deleteAnalysis(anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/analysis/{analysisId}", 1L)
                        .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                )
                .andExpect(status().isOk())
                .andDo(document("analysis-delete",
                        resource(ResourceSnippetParameters.builder()
                                .tag("아이디어 분석")
                                .summary("분석서 삭제")
                                .description("""
                                        사용자의 아이디어 분석서를 삭제합니다.
                                        
                                        ## 삭제 프로세스
                                        
                                        ### 1. 권한 검증
                                        - 본인의 분석서만 삭제 가능
                                        - 다른 사용자의 분석서 삭제 시도 시 404 또는 403 에러
                                        
                                        
                                        ### 2. 프로젝트 데이터는 삭제되지 않음 
                                        
                                        - 분석서로 생성한 프로젝트는 삭제되지 않습니다                                       
                                        - 분석서와 프로젝트는 **FK 관계가 없음**
                                        """)
                                .requestHeaders(
                                        headerWithName("Authorization").description("액세스 토큰 (Bearer 스키마)")
                                )
                                .pathParameters(
                                        parameterWithName("analysisId").description("삭제할 분석서 ID")
                                )
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("응답 상태 코드"),
                                        fieldWithPath("status.message").description("응답 메시지"),
                                        fieldWithPath("status.description").description("상세 설명").optional()
                                )
                                .build()
                        )
                ));
    }

    /**
     * 프로젝트 생성 응답 Mock 데이터
     */
    private ProjectCreateResponseDto mockProjectCreateResponse() {
        return new ProjectCreateResponseDto(
                1L,
                "지능형 학습 메이트",
                "프로젝트가 성공적으로 생성되었습니다."
        );
    }
}