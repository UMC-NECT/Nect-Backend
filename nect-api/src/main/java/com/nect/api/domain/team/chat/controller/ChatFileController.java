package com.nect.api.domain.team.chat.controller;


import com.nect.api.domain.team.chat.dto.res.ChatFileResponseDto;
import com.nect.api.domain.team.chat.dto.res.ChatRoomAlbumResponseDto;
import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDto;
import com.nect.api.domain.team.chat.service.ChatFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
public class ChatFileController {

    private final ChatFileService chatFileService;

    @PostMapping("/{roomId}/files")
    public ApiResponse<ChatFileUploadResponseDto> uploadFile(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file) {

        ChatFileUploadResponseDto response = chatFileService.uploadFile(roomId, file);
        return ApiResponse.ok(response);
    }

    @GetMapping("/projects/{projectId}/albums")
    public ApiResponse<List<ChatRoomAlbumResponseDto>> getChatAlbum(@PathVariable Long projectId) {
        List<ChatRoomAlbumResponseDto> response = chatFileService.getChatAlbum(projectId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/rooms/{roomId}/album")
    public ApiResponse<List<ChatFileResponseDto>> getChatRoomDetailAlbum(@PathVariable Long roomId) {
        List<ChatFileResponseDto> response = chatFileService.getChatRoomDetailAlbum(roomId);
        return ApiResponse.ok(response);
    }


}