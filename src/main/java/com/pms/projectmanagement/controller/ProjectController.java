package com.pms.projectmanagement.controller;

import com.pms.projectmanagement.dto.ProjectRequest;
import com.pms.projectmanagement.entity.Project;
import com.pms.projectmanagement.entity.User;
import com.pms.projectmanagement.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService = new ProjectService();

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest request,
                                                  @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<Project>> getProjects(@AuthenticationPrincipal User currentUser) {
        List<Project> projects = currentUser.getRole() == User.Role.ADMIN
                ? projectService.getAllProjects()
                : projectService.getProjectsForUser(currentUser);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Project>> searchProjects(@RequestParam String keyword) {
        return ResponseEntity.ok(projectService.searchProjects(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id,
                                                  @Valid @RequestBody ProjectRequest request,
                                                  @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.updateProject(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id,
                                               @AuthenticationPrincipal User currentUser) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<Project> addMember(@PathVariable Long id,
                                              @PathVariable Long userId,
                                              @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.addMember(id, userId, currentUser));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Project> removeMember(@PathVariable Long id,
                                                 @PathVariable Long userId,
                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(projectService.removeMember(id, userId, currentUser));
    }
}