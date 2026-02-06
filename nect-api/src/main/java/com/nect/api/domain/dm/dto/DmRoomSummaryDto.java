package com.nect.api.domain.dm.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nect.core.entity.dm.DirectMessage;
import com.nect.core.entity.user.User;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDate;

@Builder(access = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DmRoomSummaryDto(

        Long otherUserId,
        Long otherUserName,
        String otherUserImageUrl,
        String otherUserRoleField,
        Long lastMessageId,
        String lastMessage,
        LocalDate lastMessageAt,
        Boolean isRead
) {

    // 로그인한 유저의 userId와 dm을 파리미터로 넣음
    public static DmRoomSummaryDto fromOtherUser(Long userId, DirectMessage message) {

        // 마지막 메시지를 보낸 사람이 누군지
        Long senderId = message.getSender().getUserId();
        User sender = userId.equals(senderId) ? message.getReceiver() : message.getSender();

        // 상대방이 보낸 메시지면 그 메시지를 읽었는지
        boolean isRead = userId.equals(senderId) || message.getIsRead();

        return DmRoomSummaryDto.builder()
                .otherUserId(sender.getUserId())
                .otherUserName(sender.getUserId())
                .otherUserImageUrl(sender.getProfileImageUrl())
                .otherUserRoleField(sender.getName())
                .lastMessageId(message.getId())
                .lastMessage(message.getContent())
                .lastMessageAt(message.getCreatedAt().toLocalDate())
                .isRead(isRead)
                .build();
    }

}
