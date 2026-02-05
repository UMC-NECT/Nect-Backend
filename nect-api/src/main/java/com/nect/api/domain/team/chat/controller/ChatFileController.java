package com.nect.api.domain.team.chat.controller;


import com.nect.api.domain.team.chat.dto.req.ChatMessageDto;
import com.nect.api.domain.team.chat.dto.res.*;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.team.chat.service.ChatFileService;
import com.nect.api.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatFileController {

    private final ChatFileService chatFileService;

    @PostMapping("/{roomId}/files")
    public ApiResponse<ChatMessageDto> uploadFile(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ChatMessageDto response = chatFileService.uploadAndSendFile(
                roomId, file, userDetails.getUserId());



        return ApiResponse.ok(response);
    }

    //이미지 삭제(클라우드)
    @DeleteMapping("/files/{fileId}")
    public ApiResponse<Void> deleteFile(@PathVariable Long fileId,
                 @AuthenticationPrincipal UserDetailsImpl userDetails) {

        chatFileService.deleteFile(fileId, userDetails.getUserId());


        return ApiResponse.ok();
    }

    @GetMapping("/projects/{projectId}/albums")
    public ApiResponse<List<ChatRoomAlbumResponseDto>> getProjectAlbum(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "9") int limit,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ChatRoomAlbumResponseDto> response =
                chatFileService.getChatAlbum(projectId, limit, userDetails.getUserId());


        return ApiResponse.ok(response);
    }

    //TODO : WF 페이징처리가 없지만 채팅방별 클라우드 이미지 파일 조회 시 필요예상
    @GetMapping("/rooms/{roomId}/album")
    public ApiResponse<ChatRoomAlbumDetailDto> getChatRoomAlbumDetail(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ChatRoomAlbumDetailDto response =
                chatFileService.getChatRoomAlbumDetail(roomId, page, size, userDetails.getUserId());



        return ApiResponse.ok(response);
    }

    @GetMapping("/files/{fileId}")
    public ApiResponse<ChatFileDetailDto> getFileDetail(@PathVariable Long fileId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ChatFileDetailDto response =
                chatFileService.getFileDetail(fileId, userDetails.getUserId());


        return ApiResponse.ok(response);
    }

    @GetMapping("/files/{fileId}/download")
    public RedirectView downloadFile(@PathVariable Long fileId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String downloadUrl =
                chatFileService.getDownloadUrl(fileId, userDetails.getUserId());

        return new RedirectView(downloadUrl);
    }



}