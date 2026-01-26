package com.nect.core.entity.user.enums;

public enum Job {
	EMPLOYEE("직장인"),
	STUDENT("학생"),
	JOB_SEEKER("구직자"),
	FREELANCER("프리랜서"),
	BUSINESS_OWNER("사업가"),
	OTHER("기타");

	private final String description;

	Job(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}