package com.nect.core.entity.user.enums;

public enum SocialProvider {
	GOOGLE("구글"),
	KAKAO("카카오");

	private final String description;

	SocialProvider(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}