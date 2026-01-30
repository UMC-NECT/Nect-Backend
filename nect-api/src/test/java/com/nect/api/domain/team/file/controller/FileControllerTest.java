package com.nect.api.domain.team.file.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.service.FileService;
import com.nect.core.entity.team.enums.FileExt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@Transactional
class FileControllerTest {

    protected static final String AUTH_HEADER = "Authorization";
    protected static final String TEST_ACCESS_TOKEN = "Bearer testAccessToken";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Test
    @DisplayName("프로젝트 파일 업로드")
    void uploadFile() throws Exception {
        long projectId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy pdf bytes".getBytes()
        );

        FileUploadResDto res = new FileUploadResDto(
                123L,
                "sample.pdf",
                "https://cdn.example.com/projects/1/files/123_sample.pdf",
                FileExt.PDF,
                1024L
        );

        given(fileService.upload(eq(projectId), any()))
                .willReturn(res);

        mockMvc.perform(
                        multipart("/api/v1/projects/{projectId}/files/upload", projectId)
                                .file(file)
                                .header(AUTH_HEADER, TEST_ACCESS_TOKEN)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("file-upload",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParts(
                                partWithName("file").description("업로드할 파일(MultipartFile)")
                        ),
                        resource(ResourceSnippetParameters.builder()
                                .tag("File")
                                .summary("프로젝트 파일 업로드")
                                .description("프로젝트 파일을 업로드합니다. 업로드 성공 시 file_id/file_url 등을 반환합니다.")
                                .pathParameters(
                                        parameterWithName("projectId").description("프로젝트 ID")
                                )
                                .requestHeaders(
                                        headerWithName(AUTH_HEADER).optional().description("Bearer Access Token")
                                )
                                .responseFields(
                                        fieldWithPath("status").type(OBJECT).description("응답 상태"),
                                        fieldWithPath("status.statusCode").type(STRING).description("상태 코드"),
                                        fieldWithPath("status.message").type(STRING).description("메시지"),
                                        fieldWithPath("status.description").optional().type(STRING).description("상세 설명"),

                                        fieldWithPath("body").type(OBJECT).description("응답 바디"),
                                        fieldWithPath("body.file_id").type(NUMBER).description("업로드된 파일 ID"),
                                        fieldWithPath("body.file_name").type(STRING).description("원본 파일명"),
                                        fieldWithPath("body.file_url").type(STRING).description("파일 URL"),
                                        fieldWithPath("body.file_type").type(STRING).description("파일 확장자(FileExt)"),
                                        fieldWithPath("body.file_size").type(NUMBER).description("파일 크기(bytes)")
                                )
                                .build()
                        )
                ));
    }
}
