package com.pms.projectmanagement.service;

import com.pms.projectmanagement.dto.ProjectRequest;
import com.pms.projectmanagement.entity.Project;
import com.pms.projectmanagement.entity.User;
import com.pms.projectmanagement.exception.ResourceNotFoundException;
import com.pms.projectmanagement.exception.UnauthorizedAccessException;
import com.pms.projectmanagement.repository.ProjectRepository;
import com.pms.projectmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public Project createProject(ProjectRequest request, User owner) {
        List<User> members = request.getMemberIds() != null
                ? userRepository.findAllById(request.getMemberIds()) : new ArrayList<>();

        return projectRepository.save(Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Project.Status.PLANNING)
                .priority(request.getPriority() != null ? request.getPriority() : Project.Priority.MEDIUM)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .owner(owner)
                .members(members)
                .build());
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() { return projectRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Project> getProjectsForUser(User user) { return projectRepository.findAllByUser(user); }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    @Transactional
    public Project updateProject(Long id, ProjectRequest request, User currentUser) {
        Project project = getProjectById(id);
        checkOwnerOrAdmin(project.getOwner(), currentUser, "update");

        project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStatus() != null)      project.setStatus(request.getStatus());
        if (request.getPriority() != null)    project.setPriority(request.getPriority());
        if (request.getStartDate() != null)   project.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)     project.setEndDate(request.getEndDate());
        if (request.getMemberIds() != null)
            project.setMembers(userRepository.findAllById(request.getMemberIds()));

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id, User currentUser) {
        Project project = getProjectById(id);
        checkOwnerOrAdmin(project.getOwner(), currentUser, "delete");
        projectRepository.delete(project);
    }

    @Transactional
    public Project addMember(Long projectId, Long userId, User currentUser) {
        Project project = getProjectById(projectId);
        checkOwnerOrAdmin(project.getOwner(), currentUser, "add members to");
        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!project.getMembers().contains(newMember)) project.getMembers().add(newMember);
        return projectRepository.save(project);
    }

    @Transactional
    public Project removeMember(Long projectId, Long userId, User currentUser) {
        Project project = getProjectById(projectId);
        checkOwnerOrAdmin(project.getOwner(), currentUser, "remove members from");
        project.getMembers().removeIf(m -> m.getId().equals(userId));
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<Project> searchProjects(String keyword) { return projectRepository.searchByName(keyword); }

    private void checkOwnerOrAdmin(User owner, User currentUser, String action) {
        if (!owner.getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN)
            throw new UnauthorizedAccessException("Only the project owner or admin can " + action + " this project");
    }
}