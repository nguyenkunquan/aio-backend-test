package org.example.aioschedulingservice.application.dtos.schedulejob;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateScheduleRequestDto {
    @NotBlank(message = "Staff group ID cannot be blank")
    @Size(max = 100, message = "Staff group id must not exceed 100 characters")
    private String staffGroupId;

    @NotNull(message = "Week begin date cannot be null")
    @FutureOrPresent(message = "Week begin date must be in the present or future")
    private LocalDate weekBeginDate;
}
