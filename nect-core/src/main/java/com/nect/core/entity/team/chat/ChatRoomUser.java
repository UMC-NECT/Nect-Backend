package com.nect.core.entity.team.chat;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

//매핑 테이블
@Entity
@Table(name = "chat_room_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime lastReadAt;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(name = "is_notification_enabled", nullable = false)
    private Boolean isNotificationEnabled = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }

    // 알림 설정 업데이트 편의 메서드
    public void updateNotification(Boolean isEnabled) {
        this.isNotificationEnabled = isEnabled;
    }

}
