package com.nect.core.entity.notifications;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.notifications.enums.NotificationClassification;
import com.nect.core.entity.notifications.enums.NotificationScope;
import com.nect.core.entity.notifications.enums.NotificationType;
import com.nect.core.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    // ====== 필드 ======
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long targetId; // 클릭하면 호출할 api에 넣을 id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationClassification classification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationScope scope;

    @Column(name = "is_read", nullable = false)
    @ColumnDefault("false")
    private Boolean isRead;

    @Column(nullable = false, length = 100)
    private String mainMessage;

    @Column(length = 100)
    private String contentMessage; // null일 수 있음

    // ====== 연관관계 ======
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     private User receiver;

     // ====== 객체 생성 ======

    // create() 내에서 유연하게 수정할 수 있도록 작성
    @Builder(access = AccessLevel.PRIVATE)
    private Notification(
            NotificationType type,
            NotificationClassification classification,
            NotificationScope scope,
            String mainMessage,
            String contentMessage,
            Boolean isRead,
            Long targetId,
            User receiver,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.type = type;
        this.classification = classification;
        this.scope = scope;
        this.mainMessage = mainMessage;
        this.contentMessage = contentMessage;
        this.isRead = isRead != null ? isRead : false;
        this.targetId = targetId;
        this.receiver = receiver;
    }

    /**
     *
     *  Notification 객체 생성 메서드
     *  - 타입, 분류, 알림위치, 메시지 문자열 인자를 포함하여 Notification 객체를 생성합니다.
     *
     */
    public static Notification create(
            NotificationType type,
            NotificationClassification classification,
            NotificationScope scope,
            Long targetId,
            User receiver,
            Object[] mainArgs,
            Object... contentArgs
    ) {
        Notification.NotificationBuilder builder = Notification.builder()
                .type(type)
                .classification(classification)
                .scope(scope)
                .targetId(targetId)
                .receiver(receiver)
                .mainMessage(type.formatMainMessage(mainArgs));

        if (type.hasContent() && contentArgs.length > 0) {
            builder.contentMessage(type.formatContentMessage(contentArgs));
        }

        return builder.build();
    }

    // ====== 도메인 로직 ======
    public void markAsRead() {
        this.isRead = true;
    }

}
