package org.example.aioschedulingservice.application.dtos.shiftassignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aioschedulingservice.domain.enums.ShiftType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignmentDto {
    private String staffId;
    private LocalDate date;
    private ShiftType shift;
}