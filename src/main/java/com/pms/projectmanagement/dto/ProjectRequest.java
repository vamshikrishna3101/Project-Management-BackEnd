package com.pms.projectmanagement.dto;

import com.pms.projectmanagement.entity.Project;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
    private Project.Status status;
    private Project.Priority priority;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> memberIds;
}