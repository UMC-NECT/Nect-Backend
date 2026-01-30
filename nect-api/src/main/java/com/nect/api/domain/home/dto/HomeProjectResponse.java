package com.nect.api.domain.home.dto;

import java.util.List;

public record HomeProjectResponse(
    List<HomeProjectItem> projects
){
}

