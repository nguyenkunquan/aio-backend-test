package org.example.aioschedulingservice.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.aioschedulingservice.domain.enums.JobStatus;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "schedule_job")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleJob {
    @Id
    private String id;

    @Column(nullable = false)
    private String staffGroupId;

    @Column(nullable = false)
    private LocalDate weekBeginDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @OneToMany(mappedBy = "scheduleJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ShiftAssignment> assignments;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

}
