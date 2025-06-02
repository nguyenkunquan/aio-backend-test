package org.example.aioschedulingservice.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.aioschedulingservice.application.dtos.schedulejob.CreateScheduleRequestDto;
import org.example.aioschedulingservice.application.dtos.schedulejob.ScheduleResponseDto;
import org.example.aioschedulingservice.application.dtos.shiftassignment.GetScheduleResultDto;
import org.example.aioschedulingservice.application.services.ScheduleService;
import org.example.aioschedulingservice.domain.entities.ScheduleJob;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule Management", description = "APIs for managing schedule and shift assignments")
public class ScheduleController {

    private final ScheduleService service;

    @GetMapping
    @Operation(summary = "Get all scheduled jobs",
               description = "Retrieves a paginated list of all scheduled jobs.")
    public ResponseEntity<List<ScheduleJob>> getAllScheduledJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ScheduleJob> response = service.getScheduleJobs(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scheduleId}/status")
    @Operation(summary = "Get the status of a scheduled job",
               description = "Retrieves the current status of a scheduled job by its ID.")
    public ResponseEntity<ScheduleResponseDto> getJobStatus(@PathVariable String scheduleId) {
        ScheduleResponseDto response = service.getJobStatus(scheduleId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Generate a new schedule",
               description = "Submits a request to generate a new schedule based on the provided parameters.")
    public ResponseEntity<ScheduleResponseDto> generateSchedule(@Valid @RequestBody CreateScheduleRequestDto requestDto) {
        ScheduleResponseDto response = service.generateSchedule(requestDto);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping(value = "/{scheduleId}/result")
    @Operation(summary = "Get the generated schedule",
               description = "Retrieves the generated schedule by its ID.")
    public ResponseEntity<GetScheduleResultDto> getGeneratedSchedule(@PathVariable String scheduleId) {
        GetScheduleResultDto response = service.getGeneratedSchedule(scheduleId);
        return ResponseEntity.ok(response);
    }

}
