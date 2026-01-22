package com.nect.api.domain.team.chat.controller;


import com.nect.api.global.response.ApiResponse;
import com.nect.api.domain.team.chat.dto.res.ChatFileUploadResponseDTO;
import com.nect.api.domain.team.chat.service.ChatFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class ChatFileController {

    private final ChatFileService chatFileService;

    @PostMapping("/chats/files") //TODO PostMan 테스트에선 성공하는데 문서에서 Try it Out 해보면 Header문제 때문에 500번대 네트워크 에러 -> 수정필요
    public ApiResponse<ChatFileUploadResponseDTO>uploadFile(@RequestParam("file") MultipartFile file){
        ChatFileUploadResponseDTO response =chatFileService.uploadFile(file);
        return ApiResponse.ok(response);
    }


}