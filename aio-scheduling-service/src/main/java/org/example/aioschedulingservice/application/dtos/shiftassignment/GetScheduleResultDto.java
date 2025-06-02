package org.example.aioschedulingservice.application.dtos.shiftassignment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Getter
public class GetScheduleResultDto {
    private String scheduleId;
    private LocalDate weekBeginDate;
    private String staffGroupId;
    private List<ShiftAssignmentDto> assignments;
}
