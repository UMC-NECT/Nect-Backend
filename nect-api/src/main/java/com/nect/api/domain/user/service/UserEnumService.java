package com.nect.api.domain.user.service;

import com.nect.api.domain.user.dto.EnumResponseDto.*;
import com.nect.core.entity.user.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserEnumService {

    /**
     * 직업(Job) 조회
     */
    public List<EnumValueDto> getJobs() {
        return Arrays.stream(Job.values())
                .map(job -> new EnumValueDto(job.name(), job.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * 역할(Role) 조회
     */
    public List<EnumValueDto> getRoles() {
        return Arrays.stream(Role.values())
                .map(role -> new EnumValueDto(role.name(), role.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * Role별 직종(RoleField) 조회
     */
    public RoleFieldsResponseDto getRoleFields(Role role) {
        List<EnumValueDto> fields = Arrays.stream(RoleField.values())
                .filter(field -> field.getRole() == null || field.getRole().equals(role))
                .map(field -> new EnumValueDto(field.name(), field.getDescription()))
                .collect(Collectors.toList());

        return new RoleFieldsResponseDto(
                role.name(),
                role.getDescription(),
                fields
        );
    }

    /**
     * 직종(RoleField) 전체 조회
     */
    public List<EnumValueDto> getAllFields() {
        return Arrays.stream(RoleField.values())
                .map(field -> new EnumValueDto(field.name(), field.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * 스킬 카테고리(SkillCategory) 조회
     */
    public List<EnumValueDto> getSkillCategories() {
        return Arrays.stream(SkillCategory.values())
                .map(category -> new EnumValueDto(category.name(), category.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 스킬(Skill) 조회
     */
    public CategorySkillsResponseDto getCategorySkills(SkillCategory category) {
        List<EnumValueDto> skills = Arrays.stream(Skill.values())
                .filter(skill -> skill.getCategory().equals(category))
                .map(skill -> new EnumValueDto(skill.name(), skill.getDisplayName()))
                .collect(Collectors.toList());

        return new CategorySkillsResponseDto(
                category.name(),
                category.getDescription(),
                skills
        );
    }

    /**
     * 스킬(Skill) 전체 조회
     */
    public List<EnumValueDto> getAllSkills() {
        return Arrays.stream(Skill.values())
                .map(skill -> new EnumValueDto(skill.name(), skill.getDisplayName()))
                .collect(Collectors.toList());
    }

    /**
     * 관심분야(InterestField) 조회
     */
    public List<EnumValueDto> getInterestFields() {
        return Arrays.stream(InterestField.values())
                .map(interest -> new EnumValueDto(interest.name(), interest.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * 목표(Goal) 조회
     */
    public List<EnumValueDto> getGoals() {
        return Arrays.stream(Goal.values())
                .map(goal -> new EnumValueDto(goal.name(), goal.getDescription()))
                .collect(Collectors.toList());
    }
}
