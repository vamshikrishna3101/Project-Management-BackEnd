package com.pms.projectmanagement.service;

import com.pms.projectmanagement.dto.TaskRequest;
import com.pms.projectmanagement.entity.Task;
import com.pms.projectmanagement.entity.User;
import com.pms.projectmanagement.exception.ResourceNotFoundException;
import com.pms.projectmanagement.exception.UnauthorizedAccessException;
import com.pms.projectmanagement.repository.TaskRepository;
import com.pms.projectmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    @Transactional
    public Task createTask(TaskRequest request, User currentUser) {
        User assignee = request.getAssigneeId() != null
                ? userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()))
                : null;

        return taskRepository.save(Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Task.Status.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
                .dueDate(request.getDueDate())
                .project(projectService.getProjectById(request.getProjectId()))
                .assignee(assignee)
                .createdBy(currentUser)
                .build());
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks()                      { return taskRepository.findAll(); }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByProject(Long projectId) { return taskRepository.findByProjectId(projectId); }

    @Transactional(readOnly = true)
    public List<Task> getTasksByAssignee(Long userId)   { return taskRepository.findByAssigneeId(userId); }

    @Transactional(readOnly = true)
    public List<Task> getMyTasks(User currentUser)      { return taskRepository.findByAssignee(currentUser); }

    @Transactional
    public Task updateTask(Long id, TaskRequest request, User currentUser) {
        Task task = getTaskById(id);
        boolean canEdit = task.getCreatedBy().getId().equals(currentUser.getId())
                || (task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId()))
                || currentUser.getRole() == User.Role.ADMIN
                || task.getProject().getOwner().getId().equals(currentUser.getId());

        if (!canEdit) throw new UnauthorizedAccessException("You are not authorized to update this task");

        if (request.getTitle() != null)       task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null)      task.setStatus(request.getStatus());
        if (request.getPriority() != null)    task.setPriority(request.getPriority());
        if (request.getDueDate() != null)     task.setDueDate(request.getDueDate());
        if (request.getAssigneeId() != null) {
            task.setAssignee(userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId())));
        }
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTaskStatus(Long id, Task.Status status, User currentUser) {
        Task task = getTaskById(id);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id, User currentUser) {
        Task task = getTaskById(id);
        boolean canDelete = task.getCreatedBy().getId().equals(currentUser.getId())
                || currentUser.getRole() == User.Role.ADMIN
                || task.getProject().getOwner().getId().equals(currentUser.getId());
        if (!canDelete) throw new UnauthorizedAccessException("You are not authorized to delete this task");
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getTaskStatsByProject(Long projectId) {
        return Map.of(
                "TODO",        taskRepository.countByProjectIdAndStatus(projectId, Task.Status.TODO),
                "IN_PROGRESS", taskRepository.countByProjectIdAndStatus(projectId, Task.Status.IN_PROGRESS),
                "IN_REVIEW",   taskRepository.countByProjectIdAndStatus(projectId, Task.Status.IN_REVIEW),
                "DONE",        taskRepository.countByProjectIdAndStatus(projectId, Task.Status.DONE)
        );
    }
}