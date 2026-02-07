package com.nect.api.domain.team.workspace.facade;

import com.nect.api.domain.team.file.dto.res.FileUploadResDto;
import com.nect.api.domain.team.file.service.FileService;
import com.nect.api.domain.team.workspace.dto.req.PostLinkCreateReqDto;
import com.nect.api.domain.team.workspace.dto.res.PostAttachmentResDto;
import com.nect.api.domain.team.workspace.service.PostAttachmentService;
import com.nect.api.domain.team.workspace.enums.PostErrorCode;
import com.nect.api.domain.team.workspace.exception.PostException;
import com.nect.core.entity.user.User;
import com.nect.core.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostAttachmentFacade {

    private final FileService fileService;
    private final PostAttachmentService postAttachmentService;
    private final UserRepository userRepository;

    // 파일 업로드, 첨부
    @Transactional
    public PostAttachmentResDto uploadAndAttachFile(Long projectId, Long userId, Long postId, MultipartFile file) {
        // SharedDocument(FILE) 생성 + 업로드
        FileUploadResDto uploaded = fileService.upload(projectId, userId, file);

        // Post에 attach
        PostAttachmentResDto attached = postAttachmentService.attachFile(projectId, userId, postId, uploaded.fileId());

        return new PostAttachmentResDto(
                attached.documentId(),
                attached.documentType(),
                attached.title(),
                null,
                uploaded.fileName(),
                uploaded.fileType(),
                uploaded.fileSize(),
                uploaded.downloadUrl()
        );
    }

    // 링크 생성 및 첨부
    @Transactional
    public PostAttachmentResDto createAndAttachLink(Long projectId, Long userId, Long postId, PostLinkCreateReqDto req) {
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new PostException(PostErrorCode.USER_NOT_FOUND, "userId=" + userId));

        return postAttachmentService.createAndAttachLink(projectId, userId, postId, req, actor);
    }

    // 파일 or 링크 첨부 해제
    @Transactional
    public void detach(Long projectId, Long userId, Long postId, Long documentId) {
        postAttachmentService.detach(projectId, userId, postId, documentId);
    }
}
