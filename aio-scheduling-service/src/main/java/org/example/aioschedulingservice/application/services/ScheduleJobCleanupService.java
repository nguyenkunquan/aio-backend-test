package org.example.aioschedulingservice.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aioschedulingservice.domain.entities.ScheduleJob;
import org.example.aioschedulingservice.domain.enums.JobStatus;
import org.example.aioschedulingservice.infrastructure.repositories.ScheduleJobRepository;
import org.example.aioschedulingservice.infrastructure.repositories.ShiftAssignmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleJobCleanupService {
    private final ScheduleJobRepository scheduleJobRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    @Scheduled(cron = "${scheduler.job-cleanup-cron}")
    @Transactional
    public void processProcessingAndCompletedJobs() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Running daily job status updater for date: {}", yesterday);

        List<ScheduleJob> pendingJobs = scheduleJobRepository.findByStatus(JobStatus.PENDING);
        List<ScheduleJob> updatedPendingJobs = new ArrayList<>();
        for (ScheduleJob job : pendingJobs) {
            LocalDate beginDate = shiftAssignmentRepository.findMinDateByJobId(job.getId());
            if (yesterday.equals(beginDate)) {
                job.setStatus(JobStatus.PROCESSING);
                updatedPendingJobs.add(job);
                log.info("Set job {} to PROCESSING", job.getId());
            }
        }
        if (!updatedPendingJobs.isEmpty()) {
            scheduleJobRepository.saveAll(updatedPendingJobs);
        }

        List<ScheduleJob> processingJobs = scheduleJobRepository.findByStatus(JobStatus.PROCESSING);
        List<ScheduleJob> updatedProcessingJobs = new ArrayList<>();
        for (ScheduleJob job : processingJobs) {
            LocalDate finishDate = shiftAssignmentRepository.findMaxDateByJobId(job.getId());
            if (yesterday.equals(finishDate)) {
                job.setStatus(JobStatus.COMPLETED);
                updatedProcessingJobs.add(job);
                log.info("Set job {} to COMPLETED", job.getId());
            }
        }
        if (!updatedProcessingJobs.isEmpty()) {
            scheduleJobRepository.saveAll(updatedProcessingJobs);
        }
    }
}
