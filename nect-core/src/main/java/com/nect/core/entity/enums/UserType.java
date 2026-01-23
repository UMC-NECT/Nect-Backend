package com.nect.core.entity.enums;

public enum UserType {
	MEMBER("일반 멤버"),
	LEADER("리더");

	private final String description;

	UserType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}