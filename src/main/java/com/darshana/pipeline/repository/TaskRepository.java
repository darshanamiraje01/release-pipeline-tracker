package com.darshana.pipeline.repository;

import com.darshana.pipeline.entity.Task;
import com.darshana.pipeline.entity.TaskPriority;
import com.darshana.pipeline.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // All tasks for a release
    List<Task> findByReleaseId(Long releaseId);

    // Tasks by status within a release
    List<Task> findByReleaseIdAndStatus(Long releaseId, TaskStatus status);

    // Tasks assigned to a person
    List<Task> findByAssignee(String assignee);

    // Tasks by priority within a release
    List<Task> findByReleaseIdAndPriority(Long releaseId, TaskPriority priority);

    // Count tasks by status for a release (used in summary)
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.release.id = :releaseId GROUP BY t.status")
    List<Object[]> countByStatusForRelease(@Param("releaseId") Long releaseId);

    // Count tasks by priority for a release (used in summary)
    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.release.id = :releaseId GROUP BY t.priority")
    List<Object[]> countByPriorityForRelease(@Param("releaseId") Long releaseId);

    // Count total tasks for a release
    long countByReleaseId(Long releaseId);

    // Count done tasks for a release
    long countByReleaseIdAndStatus(Long releaseId, TaskStatus status);
}
