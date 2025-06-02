package org.example.aioschedulingservice.application.services;

import org.example.aioschedulingservice.application.dtos.schedulejob.CreateScheduleRequestDto;
import org.example.aioschedulingservice.application.dtos.schedulejob.ScheduleResponseDto;
import org.example.aioschedulingservice.application.dtos.shiftassignment.GetScheduleResultDto;
import org.example.aioschedulingservice.domain.entities.ScheduleJob;

import java.util.List;

public interface ScheduleService {
    List<ScheduleJob> getScheduleJobs(int page, int size);
    ScheduleResponseDto generateSchedule(CreateScheduleRequestDto requestDto);
    ScheduleResponseDto getJobStatus(String scheduleId);
    GetScheduleResultDto getGeneratedSchedule(String scheduleId);
}
