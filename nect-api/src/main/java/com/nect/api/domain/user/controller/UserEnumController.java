package com.nect.api.domain.user.controller;

import com.nect.api.domain.user.dto.EnumResponseDto.*;
import com.nect.api.domain.user.service.UserEnumService;
import com.nect.api.global.response.ApiResponse;
import com.nect.core.entity.user.enums.Role;
import com.nect.core.entity.user.enums.SkillCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enums")
@RequiredArgsConstructor
public class UserEnumController {

    private final UserEnumService userEnumService;

    @GetMapping("/jobs")
    public ApiResponse<List<EnumValueDto>> getJobs() {
        return ApiResponse.ok(userEnumService.getJobs());
    }

    @GetMapping("/roles")
    public ApiResponse<List<EnumValueDto>> getRoles() {
        return ApiResponse.ok(userEnumService.getRoles());
    }

    @GetMapping("/role-fields")
    public ApiResponse<RoleFieldsResponseDto> getRoleFields(
            @RequestParam Role role
    ) {
        return ApiResponse.ok(userEnumService.getRoleFields(role));
    }

    @GetMapping("/fields")
    public ApiResponse<List<EnumValueDto>> getAllFields() {
        return ApiResponse.ok(userEnumService.getAllFields());
    }

    @GetMapping("/skill-categories")
    public ApiResponse<List<EnumValueDto>> getSkillCategories() {
        return ApiResponse.ok(userEnumService.getSkillCategories());
    }

    @GetMapping("/category-skills")
    public ApiResponse<CategorySkillsResponseDto> getCategorySkills(
            @RequestParam SkillCategory category
    ) {
        return ApiResponse.ok(userEnumService.getCategorySkills(category));
    }

    @GetMapping("/skills")
    public ApiResponse<List<EnumValueDto>> getAllSkills() {
        return ApiResponse.ok(userEnumService.getAllSkills());
    }

    @GetMapping("/interest-fields")
    public ApiResponse<List<EnumValueDto>> getInterestFields() {
        return ApiResponse.ok(userEnumService.getInterestFields());
    }

    @GetMapping("/goals")
    public ApiResponse<List<EnumValueDto>> getGoals() {
        return ApiResponse.ok(userEnumService.getGoals());
    }
}
