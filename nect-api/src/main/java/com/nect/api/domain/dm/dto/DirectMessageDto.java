package com.nect.api.domain.dm.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.dm.DirectMessage;
import com.nect.core.entity.user.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DirectMessageDto {

    private Long messageId;

    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private String content;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private Boolean isRead;

    // DM -> DTO
    public static DirectMessageDto fromDm(DirectMessage dm) {

        User sender = dm.getSender();
        return DirectMessageDto.builder()
                .messageId(dm.getId())
                .senderId(sender.getUserId())
                .senderName(sender.getName())
                .senderProfileImage(sender.getProfileImageName())
                .content(dm.getContent())
                .isPinned(false)
                .createdAt(dm.getCreatedAt())
                .isRead(dm.getIsRead())
                .build();
    }

    public void setImageUrl(String imageUrl) {
        this.senderProfileImage = imageUrl;
    }

}
