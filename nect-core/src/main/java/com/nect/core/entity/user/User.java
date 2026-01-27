package com.nect.core.entity.user;

import com.nect.core.entity.BaseEntity;
import com.nect.core.entity.user.enums.Job;
import com.nect.core.entity.user.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_users_email"),
        @UniqueConstraint(columnNames = {"socialProvider", "socialId"}, name = "uk_users_social")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "phone")
    private String phoneNumber;

    @Column(name = "birthdate")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Job job;

    @Builder.Default
    @Column(name = "is_Agreed", nullable = false)
    private Boolean isAgreed = false;

    @Column(name = "socialProvider")
    private String socialProvider;

    @Column(name = "socialId")
    private String socialId;

    @Builder.Default
    @Column(name = "isAutoLoginEnabled", nullable = false)
    private Boolean isAutoLoginEnabled = false;
}