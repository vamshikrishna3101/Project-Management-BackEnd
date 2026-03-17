package com.pms.projectmanagement.repository;

import com.pms.projectmanagement.entity.Task;
import com.pms.projectmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssignee(User assignee);
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByStatus(Task.Status status);
    List<Task> findByProjectIdAndStatus(Long projectId, Task.Status status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.assignee.id = :userId")
    List<Task> findByProjectAndAssignee(@Param("projectId") Long projectId,
                                        @Param("userId") Long userId);

    long countByProjectIdAndStatus(Long projectId, Task.Status status);
}