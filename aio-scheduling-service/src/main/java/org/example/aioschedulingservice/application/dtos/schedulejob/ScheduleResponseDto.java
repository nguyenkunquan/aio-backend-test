package org.example.aioschedulingservice.application.dtos.schedulejob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aioschedulingservice.domain.enums.JobStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {
    private String scheduleId;
    private JobStatus status;
    private String errorMessage;
}