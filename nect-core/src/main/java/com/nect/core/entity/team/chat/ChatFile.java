package com.nect.core.entity.team.chat;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_file")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메시지 ID
    @Column(nullable = false)
    private Long messageId;

    // 파일명
    @Column(nullable = false, length = 255)
    private String fileName;

    // 파일 URL
    @Column(nullable = false, length = 500)
    private String fileUrl;

    // 파일 크기
    private Long fileSize;

    // 파일 타입 (image/jpeg, application/pdf 등)
    @Column(length = 100)
    private String fileType;


}