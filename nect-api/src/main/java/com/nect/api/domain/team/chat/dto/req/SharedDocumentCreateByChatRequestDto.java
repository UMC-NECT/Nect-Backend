package com.nect.api.domain.team.chat.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SharedDocumentCreateByChatRequestDto(
        @JsonProperty("chat_file_id")
        Long chatFileId
) {}