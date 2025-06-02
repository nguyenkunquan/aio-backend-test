package org.example.aioschedulingservice.infrastructure.repositories;

import org.example.aioschedulingservice.domain.entities.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, String> {
    List<ShiftAssignment> findByScheduleJobId(String scheduleJobId);
    @Query("SELECT MIN(sa.date) FROM ShiftAssignment sa WHERE sa.scheduleJob.id = :jobId")
    LocalDate findMinDateByJobId(@Param("jobId") String jobId);
    @Query("SELECT MAX(sa.date) FROM ShiftAssignment sa WHERE sa.scheduleJob.id = :jobId")
    LocalDate findMaxDateByJobId(@Param("jobId") String jobId);
}
