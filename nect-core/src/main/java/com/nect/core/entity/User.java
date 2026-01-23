package com.nect.core.entity;

import com.nect.core.entity.enums.Occupation;
import com.nect.core.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users", uniqueConstraints = {
		@UniqueConstraint(columnNames = "email", name = "uk_users_email"),
		@UniqueConstraint(columnNames = "nickname", name = "uk_users_nickname")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password")
	private String password;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String nickname;

	@Column(name = "phone")
	private String phoneNumber;

	@Column(name = "birthDate")
	private LocalDate birthDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserType userType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Occupation job;

	@Column(name = "is_Agreed", nullable = false)
	private Boolean isAgreed = false;

	@Column(name = "socialProvider")
	private String socialProvider;

	@Column(name = "socialId")
	private String socialId;

	@Column(name = "isAutoLoginEnabled", nullable = false)
	private Boolean isAutoLoginEnabled = false;

	// 비밀번호 설정 (소셜로그인 -> 자체로그인으로 전환 등에 사용)
	public void setPassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	// 소셜 정보 설정
	public void setSocialInfo(String provider, String socialId) {
		this.socialProvider = provider;
		this.socialId = socialId;
	}

	// 자동로그인 설정 변경
	public void setAutoLoginEnabled(Boolean enabled) {
		this.isAutoLoginEnabled = enabled;
	}
}