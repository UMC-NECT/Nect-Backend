package com.nect.api.domain.user.oauth2.dto;

import java.util.Map;

public class GoogleOAuth2UserInfo {

	private final Map<String, Object> attributes;

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public String getSocialId() {
		Object sub = attributes.get("sub");
		return sub != null ? sub.toString() : null;
	}

	public String getEmail() {
		Object email = attributes.get("email");
		return email != null ? email.toString() : null;
	}

	public String getName() {
		Object name = attributes.get("name");
		return name != null ? name.toString() : null;
	}

	public String getProfileImage() {
		Object picture = attributes.get("picture");
		return picture != null ? picture.toString() : null;
	}
}