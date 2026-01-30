package com.nect.api.domain.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.NectDocumentApiTester;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.SkillCategory;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserEnumControllerTest extends NectDocumentApiTester {

    @Test
    void getJobs() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/jobs"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-jobs",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("직업(Job) 조회")
                                        .description("사용 가능한 모든 직업 목록을 조회합니다. (직장인, 학생, 구직자 등)")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body[].value").type(JsonFieldType.STRING).description("직업 Enum 값 (예: EMPLOYEE)"),
                                                fieldWithPath("body[].label").type(JsonFieldType.STRING).description("직업 한글명 (예: 직장인)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getRoles() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/roles"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-roles",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("역할(Role) 조회")
                                        .description("사용 가능한 모든 역할 목록을 조회합니다. (디자이너, 개발자, 기획자, 마케터, 기타)")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body[].value").type(JsonFieldType.STRING).description("역할 Enum 값 (예: DEVELOPER)"),
                                                fieldWithPath("body[].label").type(JsonFieldType.STRING).description("역할 한글명 (예: 개발자)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getRoleFields() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/role-fields")
                        .param("role", "DEVELOPER"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-role-fields",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("직종(Field) 조회 - Role별")
                                        .description("선택한 역할에 맞는 직종 목록을 조회합니다. (예: 개발자 선택 시 프론트엔드, 백엔드 등)")
                                        .queryParameters(
                                                parameterWithName("role").description("필터링할 역할 (Role Enum 값, 예: DEVELOPER)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.role").type(JsonFieldType.STRING).description("역할 Enum 값 (예: DEVELOPER)"),
                                                fieldWithPath("body.roleLabel").type(JsonFieldType.STRING).description("역할 한글명 (예: 개발자)"),
                                                fieldWithPath("body.fields[].value").type(JsonFieldType.STRING).description("직종 Enum 값 (예: FRONTEND)"),
                                                fieldWithPath("body.fields[].label").type(JsonFieldType.STRING).description("직종 한글명 (예: 프론트엔드)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getAllFields() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/fields"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-all-fields",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("직종(Field) 전체 조회")
                                        .description("모든 역할의 직종 목록을 조회합니다. (role 파라미터 없음)")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body[].value").type(JsonFieldType.STRING).description("직종 Enum 값"),
                                                fieldWithPath("body[].label").type(JsonFieldType.STRING).description("직종 한글명")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getSkillCategories() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/skill-categories"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-skill-categories",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("스킬 카테고리(SkillCategory) 조회")
                                        .description("사용 가능한 모든 스킬 카테고리 목록을 조회합니다. (디자인, 기술, 기획, 마케팅, 기타)")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body[].value").type(JsonFieldType.STRING).description("스킬 카테고리 Enum 값 (예: DESIGN)"),
                                                fieldWithPath("body[].label").type(JsonFieldType.STRING).description("스킬 카테고리 한글명 (예: 디자인)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getCategorySkills() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/category-skills")
                        .param("category", "DESIGN"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-category-skills",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("스킬(Skill) 조회 - 카테고리별")
                                        .description("선택한 카테고리의 스킬 목록을 조회합니다. (예: 디자인 선택 시 Figma, Photoshop 등)")
                                        .queryParameters(
                                                parameterWithName("category").description("필터링할 카테고리 (SkillCategory Enum 값, 예: DESIGN)")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.category").type(JsonFieldType.STRING).description("스킬 카테고리 Enum 값 (예: DESIGN)"),
                                                fieldWithPath("body.categoryLabel").type(JsonFieldType.STRING).description("스킬 카테고리 한글명 (예: 디자인)"),
                                                fieldWithPath("body.skills[].value").type(JsonFieldType.STRING).description("스킬 Enum 값 (예: FIGMA)"),
                                                fieldWithPath("body.skills[].label").type(JsonFieldType.STRING).description("스킬 이름 (예: Figma)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getAllSkills() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/skills"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-all-skills",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("스킬(Skill) 전체 조회")
                                        .description("모든 카테고리의 스킬 목록을 조회합니다. (category 파라미터 없음)")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body[].value").type(JsonFieldType.STRING).description("스킬 Enum 값"),
                                                fieldWithPath("body[].label").type(JsonFieldType.STRING).description("스킬 이름")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getInterestFields() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/interest-fields"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-interests",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("관심분야(InterestField) 조회")
                                        .description("사용 가능한 모든 관심분야 목록을 조회합니다. (IT·웹/모바일, 금융·핀테크 등)")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body[].value").type(JsonFieldType.STRING).description("관심분야 Enum 값 (예: IT_WEB_MOBILE)"),
                                                fieldWithPath("body[].label").type(JsonFieldType.STRING).description("관심분야 한글명 (예: IT·웹/모바일 서비스)")
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getGoals() throws Exception {
        this.mockMvc.perform(get("/api/v1/enums/goals"))
                .andExpect(status().isOk())
                .andDo(document("user-enum-get-goals",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("enums")
                                        .summary("목표(Goal) 조회")
                                        .description("사용 가능한 모든 목표 목록을 조회합니다. (포트폴리오 제작, 팀 협업 능력 향상 등)")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body[].value").type(JsonFieldType.STRING).description("목표 Enum 값 (예: PORTFOLIO)"),
                                                fieldWithPath("body[].label").type(JsonFieldType.STRING).description("목표 한글명 (예: 포트폴리오 제작)")
                                        )
                                        .build()
                        )
                ));
    }
}
