package com.nect.api.domain.user.oauth2.dto;

import java.util.Map;

public class KakaoOAuth2UserInfo {

	private final Map<String, Object> attributes;

	public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public String getSocialId() {
		Object id = attributes.get("id");
		return id != null ? id.toString() : null;
	}

	public String getNickname() {
		Map<String, Object> properties = getProperties();
		if (properties != null) {
			Object nickname = properties.get("nickname");
			return nickname != null ? nickname.toString() : null;
		}
		return null;
	}

	public String getProfileImage() {
		Map<String, Object> properties = getProperties();
		if (properties != null) {
			Object profileImage = properties.get("profile_image");
			return profileImage != null ? profileImage.toString() : null;
		}
		return null;
	}

	public String getEmail() {
		Map<String, Object> kakaoAccount = getKakaoAccount();
		if (kakaoAccount != null) {
			Object email = kakaoAccount.get("email");
			return email != null ? email.toString() : null;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getProperties() {
		return (Map<String, Object>) attributes.get("properties");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getKakaoAccount() {
		return (Map<String, Object>) attributes.get("kakao_account");
	}
}