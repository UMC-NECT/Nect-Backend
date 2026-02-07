package com.nect.api.domain.upload.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.nect.api.NectDocumentApiTester;
import com.nect.api.domain.upload.dto.UploadDto;
import com.nect.api.domain.upload.service.UploadService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UploadControllerTest extends NectDocumentApiTester {

    @MockitoBean
    private UploadService uploadService;

    @Test
    void uploadProfileImage() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        UploadDto.ImageUploadResponseDto mockResponse = new UploadDto.ImageUploadResponseDto(
                "550e8400-e29b-41d4-a716-446655440000_profile.jpg",
                "https://s3.example.com/nect/550e8400-e29b-41d4-a716-446655440000_profile.jpg?X-Amz-Algorithm=..."
        );
        when(uploadService.uploadProfileImage(any())).thenReturn(mockResponse);

        // when
        this.mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andDo(document("file-upload-image",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("files")
                                        .summary("이미지 업로드")
                                        .description("프로필 이미지를 업로드합니다. 업로드된 파일명과 Presigned URL을 반환합니다.")
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body.fileName").type(JsonFieldType.STRING).description("업로드된 파일명 (DB 저장 값)"),
                                                fieldWithPath("body.fileUrl").type(JsonFieldType.STRING).description("S3 Presigned URL (5분 유효)")
                                        )
                                        .build()
                        )
                ));
    }
}