package org.example.aioschedulingservice.infrastructure.repositories;

import org.example.aioschedulingservice.domain.entities.ScheduleJob;
import org.example.aioschedulingservice.domain.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleJobRepository extends JpaRepository<ScheduleJob, String> {
    List<ScheduleJob> findByStatus(JobStatus status);
}
