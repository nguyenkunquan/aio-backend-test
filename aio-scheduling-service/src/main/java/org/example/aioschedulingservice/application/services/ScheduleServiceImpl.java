package org.example.aioschedulingservice.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aioschedulingservice.application.dtos.schedulejob.CreateScheduleRequestDto;
import org.example.aioschedulingservice.application.dtos.schedulejob.ScheduleResponseDto;
import org.example.aioschedulingservice.application.dtos.shiftassignment.GetScheduleResultDto;
import org.example.aioschedulingservice.application.dtos.shiftassignment.ShiftAssignmentDto;
import org.example.aioschedulingservice.application.exceptions.ResourceNotFoundException;
import org.example.aioschedulingservice.application.webclients.DataServiceClient;
import org.example.aioschedulingservice.domain.entities.ScheduleJob;
import org.example.aioschedulingservice.domain.entities.ShiftAssignment;
import org.example.aioschedulingservice.domain.enums.JobStatus;
import org.example.aioschedulingservice.infrastructure.repositories.ScheduleJobRepository;
import org.example.aioschedulingservice.infrastructure.repositories.ShiftAssignmentRepository;
import org.hibernate.annotations.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Console;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    private final ScheduleJobRepository scheduleJobRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ScheduleGeneratorService scheduleGeneratorService;

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleJob> getScheduleJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var scheduleJobs = scheduleJobRepository.findAll(pageable).getContent();
        return scheduleJobs;
    }

    @Override
    @Transactional
    public ScheduleResponseDto generateSchedule(CreateScheduleRequestDto requestDto) {
        logger.info("Generate new schedule request. Staff Group ID: {}, Week Begin: {}",
                requestDto.getStaffGroupId(), requestDto.getWeekBeginDate());

        ScheduleJob job = ScheduleJob.builder()
                .id(UUID.randomUUID().toString())
                .staffGroupId(requestDto.getStaffGroupId())
                .weekBeginDate(requestDto.getWeekBeginDate())
                .status(JobStatus.PENDING)
                .build();
        scheduleJobRepository.save(job);
        scheduleGeneratorService.generateScheduleAsync(job.getId());
        var newestJob = scheduleJobRepository.findById(job.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule job not found with ID: " + job.getId()));
        return new ScheduleResponseDto(
                newestJob.getId(),
                newestJob.getStatus(),
                newestJob.getErrorMessage());
    }

    @Transactional(readOnly = true)
    public ScheduleResponseDto getJobStatus(String scheduleId) {
        logger.debug("Fetching status for job ID: {}", scheduleId);
        ScheduleJob job = scheduleJobRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule job not found with ID: " + scheduleId));

        return ScheduleResponseDto.builder()
                .scheduleId(job.getId())
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .build();
    }

    @Override
    public GetScheduleResultDto getGeneratedSchedule(String scheduleId) {
        logger.debug("Fetching result for job ID: {}", scheduleId);
        ScheduleJob job = scheduleJobRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule job not found with ID: " + scheduleId));
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByScheduleJobId(scheduleId);
        List<ShiftAssignmentDto> assignmentDtos = assignments.stream()
                .map(sa -> new ShiftAssignmentDto(sa.getStaffId(), sa.getDate(), sa.getShiftType()))
                .collect(Collectors.toList());
        return new GetScheduleResultDto(
                job.getId(),
                job.getWeekBeginDate(),
                job.getStaffGroupId(),
                assignmentDtos
        );
    }
}
