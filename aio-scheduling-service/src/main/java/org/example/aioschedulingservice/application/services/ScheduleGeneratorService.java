package org.example.aioschedulingservice.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aioschedulingservice.application.exceptions.DataServiceException;
import org.example.aioschedulingservice.application.utils.DateUtil;
import org.example.aioschedulingservice.application.webclients.DataServiceClient;
import org.example.aioschedulingservice.domain.entities.ScheduleJob;
import org.example.aioschedulingservice.domain.entities.ShiftAssignment;
import org.example.aioschedulingservice.domain.enums.JobStatus;
import org.example.aioschedulingservice.domain.enums.ShiftType;
import org.example.aioschedulingservice.infrastructure.configs.SchedulingProperties;
import org.example.aioschedulingservice.infrastructure.repositories.ScheduleJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ScheduleGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleGeneratorService.class);

    private final ScheduleJobRepository scheduleJobRepository;
    private final DataServiceClient dataServiceClient;
    private final LockService lockService;

    private final int periodWeeks;
    private final boolean isAvoidMorningAfterEvening;
    private final boolean isBalanceShifts;
    private final boolean isDayOffEnabled;
    private final int offDaysPerWeek;

    public ScheduleGeneratorService(SchedulingProperties schedulingProperties,
                                    ScheduleJobRepository scheduleJobRepository,
                                    DataServiceClient dataServiceClient,
                                    LockService lockService) {
        this.scheduleJobRepository = scheduleJobRepository;
        this.dataServiceClient = dataServiceClient;
        this.lockService = lockService;

        this.periodWeeks = schedulingProperties.getRules().getPeriodWeeks();
        this.isAvoidMorningAfterEvening = schedulingProperties.getRules().isAvoidMorningAfterEvening();
        this.isBalanceShifts = schedulingProperties.getRules().isBalanceShifts();
        this.isDayOffEnabled = schedulingProperties.getRules().getDayOff().isEnabled();
        this.offDaysPerWeek = schedulingProperties.getRules().getDayOff().getDaysPerWeek();
    }

    public void generateScheduleAsync(String jobId) {
        String lockKey = "lock:job" + jobId;
        String lockValue = UUID.randomUUID().toString();

        if(!lockService.acquireLock(lockKey, lockValue, Duration.ofMinutes(1))) {
            logger.warn("Job {} already being processed or failed to acquire lock. Skipping.", jobId);
            return;
        }
        ScheduleJob job = null;
        try {
            job = scheduleJobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId + ". This should not happen if lock was acquired."));
            if (job.getStatus() != JobStatus.PENDING) {
                logger.warn("Job {} is not in PENDING state (current: {}). Skipping processing.", jobId, job.getStatus());
                return;
            }
            scheduleJobRepository.save(job);
            logger.info("Job {} status updated to PROCESSING.", jobId);

            Set<String> staffIds = dataServiceClient.getStaffForGroup(job.getStaffGroupId());
            if (staffIds.isEmpty()) {
                logger.warn("No staff members found for group {}. Job {} will be marked as FAILED.", job.getStaffGroupId(), jobId);
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("No staff members found for group " + job.getStaffGroupId() + " or Data Service unavailable.");
                scheduleJobRepository.save(job);
                return;
            }

            logger.info("Generating schedule for job {}, group {}, week starting {}. Staff count: {}",
                    jobId, job.getStaffGroupId(), job.getWeekBeginDate(), staffIds.size());
            List<ShiftAssignment> shiftAssignments = generateShiftAssignments(job, staffIds);
            job.setAssignments(shiftAssignments);
            logger.info("Job {} completed successfully.", jobId);
        } catch (Exception e) {
            logger.error("Error generating schedule for job {}: {}", jobId, e.getMessage(), e);
            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("Internal error during schedule generation: " + e.getMessage());
            }
        } finally {
            if (job != null) {
                scheduleJobRepository.save(job);
            }
            lockService.releaseLock(lockKey, lockValue);
            logger.info("Released lock for job {}", jobId);
        }
    }

    private List<ShiftAssignment> generateShiftAssignments(ScheduleJob job, Set<String> staffIds) {


        List<ShiftAssignment> allAssignments = new ArrayList<>();
        LocalDate startDate = job.getWeekBeginDate();
        Map<String, List<ShiftType>> staffShiftHistory = new HashMap<>();
        Map<String, ShiftType> staffLastShift = new HashMap<>();

        for (String staffId : staffIds) {
            staffShiftHistory.put(staffId, new ArrayList<>());
        }

        dayOffsetLoop:
        for(int dayOffset = 0; dayOffset < (7 * periodWeeks); dayOffset++) { //RULE 1: period-weeks
            LocalDate currentDate = startDate.plusDays(dayOffset);
            int currentWeek = DateUtil.getWeekOfYear(currentDate);

            for(String staffId : staffIds) {
                List<ShiftType> possibleShiftTypes = new ArrayList<>(Arrays.asList(ShiftType.MORNING, ShiftType.EVENING, ShiftType.DAY_OFF));

                // RULE 2: avoid-morning-after-evening
                if(isAvoidMorningAfterEvening) {
                    ShiftType lastShiftType = staffLastShift.get(staffId);
                    if(lastShiftType == ShiftType.EVENING) {
                        possibleShiftTypes.remove(ShiftType.MORNING);
                    }
                }

                // RULE 3: day-off
                if(isDayOffEnabled) {
                    int dayOffsThisWeek = (int) allAssignments.stream()
                            .filter(a -> a.getStaffId().equals(staffId) &&
                                    a.getShiftType() == ShiftType.DAY_OFF &&
                                    DateUtil.getWeekOfYear(a.getDate()) == currentWeek)
                            .count();
                    if(dayOffsThisWeek >= offDaysPerWeek)
                        possibleShiftTypes.remove(ShiftType.DAY_OFF);
                    if(currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        int missingDayOffs = offDaysPerWeek - dayOffsThisWeek;
                        if(missingDayOffs > 0) {

                            assignAndRecordShift(allAssignments, job, staffId, currentDate, ShiftType.DAY_OFF, staffShiftHistory, staffLastShift);
                            missingDayOffs--;

                            LocalDate updateDate = currentDate.minusDays(1);
                            while(missingDayOffs > 0 && updateDate.getDayOfWeek() != DayOfWeek.MONDAY){
                                LocalDate finalUpdateDate = updateDate;
                                Optional<ShiftAssignment> existingAssignment = allAssignments.stream()
                                        .filter(a -> a.getStaffId().equals(staffId) &&
                                                a.getDate().equals(finalUpdateDate))
                                        .findFirst();
                                if(existingAssignment.isPresent()) {
                                    ShiftAssignment assignment = existingAssignment.get();
                                    assignment.setShiftType(ShiftType.DAY_OFF);
                                    missingDayOffs--;
                                }
                                updateDate = updateDate.minusDays(1);
                            }
                            continue dayOffsetLoop;
                        }
                    }
                } else possibleShiftTypes.remove(ShiftType.DAY_OFF);

                // RULE 4: balance-shifts
                if(isBalanceShifts) {
                    long morningCount = staffShiftHistory.get(staffId).stream().filter(s -> s == ShiftType.MORNING).count();
                    long eveningCount = staffShiftHistory.get(staffId).stream().filter(s -> s == ShiftType.EVENING).count();
                        if (morningCount < eveningCount) {
                            possibleShiftTypes.remove(ShiftType.EVENING);
                        } else if (eveningCount < morningCount) {
                            possibleShiftTypes.remove(ShiftType.MORNING);
                        }
                    }

                if(possibleShiftTypes.isEmpty()) {
                    possibleShiftTypes.add(ShiftType.EVENING);
                }

                ShiftType randomShiftType = possibleShiftTypes.get(ThreadLocalRandom.current().nextInt(possibleShiftTypes.size()));
                assignAndRecordShift(allAssignments, job, staffId, currentDate, randomShiftType, staffShiftHistory, staffLastShift);
            }
        }
        logger.info("Generated {} assignments for job {}", allAssignments.size(), job.getId());
        return allAssignments;
    }

    private void assignAndRecordShift(List<ShiftAssignment> allAssignments, ScheduleJob job, String staffId,
                                      LocalDate currentDate, ShiftType shiftType,
                                      Map<String, List<ShiftType>> staffShiftHistory,
                                      Map<String, ShiftType> staffLastShift) {
        allAssignments.add(ShiftAssignment.builder()
                .scheduleJob(job)
                .staffId(staffId)
                .date(currentDate)
                .shiftType(shiftType)
                .build());
        staffShiftHistory.get(staffId).add(shiftType);
        staffLastShift.put(staffId, shiftType);
    }
}
