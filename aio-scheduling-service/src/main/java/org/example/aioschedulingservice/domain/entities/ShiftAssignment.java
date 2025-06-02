package org.example.aioschedulingservice.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.aioschedulingservice.domain.enums.ShiftType;

import java.time.LocalDate;

@Entity
@Table(name = "shift_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_job_id", nullable = false)
    private ScheduleJob scheduleJob;

    @Column(nullable = false)
    private String staffId;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftType shiftType;
}
