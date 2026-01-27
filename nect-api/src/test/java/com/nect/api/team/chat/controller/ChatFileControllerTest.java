package com.nect.api.team.chat.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDTO;
import com.nect.api.domain.team.chat.service.ChatFileService;
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
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Transactional
public class ChatFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatFileService chatFileService;

    @Test
    @DisplayName("파일 업로드 API 테스트")
    void uploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test file content".getBytes()
        );

        ChatFileUploadResponseDTO response = ChatFileUploadResponseDTO.builder()
                .fileId(1L)
                .fileName("test.png")
                .fileUrl("/files/uuid-test.png")
                .fileSize(1024L)
                .fileType("image/png")
                .build();

        given(chatFileService.uploadFile(any()))
                .willReturn(response);

        mockMvc.perform(multipart("/chats/files")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.file_id").value(1L))
                .andDo(document("chat-file-upload",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        // requestParts를 별도로 추가
                        requestParts(
                                partWithName("file").description("업로드할 이미지 또는 파일 (MultipartFile)")
                        ),
                        // resource는 별도로
                        resource(ResourceSnippetParameters.builder()
                                .tag("파일")
                                .summary("파일 업로드")
                                .description("채팅 메시지에 첨부할 파일을 업로드합니다.")
                                .responseFields(
                                        fieldWithPath("status.statusCode").description("상태 코드"),
                                        fieldWithPath("status.message").description("상태 메시지"),
                                        fieldWithPath("status.description").description("상태 설명").optional(),
                                        fieldWithPath("body.file_id").description("생성된 파일 고유 ID"),
                                        fieldWithPath("body.file_name").description("원본 파일 이름"),
                                        fieldWithPath("body.file_url").description("파일 접근 URL"),
                                        fieldWithPath("body.file_size").description("파일 크기 (bytes)"),
                                        fieldWithPath("body.file_type").description("파일 확장자/타입")
                                )
                                .build()
                        )
                ));
    }
}