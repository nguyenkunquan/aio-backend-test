package org.example.aiodataservice.application.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aiodataservice.application.constants.CacheNameConstant;
import org.example.aiodataservice.application.dtos.staff.CreateStaffDto;
import org.example.aiodataservice.application.exceptions.DuplicateResourceException;
import org.example.aiodataservice.application.exceptions.ResourceNotFoundException;
import org.example.aiodataservice.application.services.StaffService;
import org.example.aiodataservice.domain.documents.Staff;
import org.example.aiodataservice.domain.documents.StaffGroup;
import org.example.aiodataservice.infrastructure.repositories.StaffGroupRepository;
import org.example.aiodataservice.infrastructure.repositories.StaffRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffGroupRepository staffGroupRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = CacheNameConstant.STAFF_LIST_CACHE)
    public List<Staff> getStaffs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var staffs = staffRepository.findAll(pageable).getContent();
        return staffs;
    }

    @Override
    @Cacheable(value = CacheNameConstant.STAFF_CACHE, key = "#id", unless = "#result == null")
    public Staff getStaffById(String id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        return staff;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_CACHE, key = "#result.id")
    })
    public Staff createStaff(String staffCode, String name, String email) {
       String lockKey = "lock:staff:create:" + staffCode;
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, staffCode, Duration.ofSeconds(10));
            if (locked == null || !locked) {
                throw new RuntimeException("Could not acquire lock for staffCode: " + staffCode);
            }
            if (staffRepository.existsByStaffCode(staffCode)) {
                throw new DuplicateResourceException("Staff Code already exists: " + staffCode);
            }
            Staff staff = Staff.builder()
                    .id(UUID.randomUUID().toString())
                    .staffCode(staffCode)
                    .name(name)
                    .email(email)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();
            staffRepository.save(staff);
            return staff;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_CACHE, key = "#id")
    })
    public Staff updateStaff(String id, String staffCode, String name, String email) {
        String lockKey = "lock:staff:" + id;
        try{
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, id, Duration.ofSeconds(10));
            if (locked == null || !locked) {
                throw new RuntimeException("Could not acquire lock for id: " + id);
            }
            Staff staff = staffRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
            staff.setStaffCode(staffCode);
            staff.setName(name);
            staff.setEmail(email);
            staff.setUpdatedAt(LocalDate.now());
            staffRepository.save(staff);
            return staff;
        }
        finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_CACHE, key = "#id"),
            @CacheEvict(value = CacheNameConstant.STAFF_DETAIL_CACHE, key = "#id"),
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_LIST_CACHE, key = "#id"),
            @CacheEvict(value = CacheNameConstant.RESOLVED_GROUP_MEMBERS_CACHE, allEntries = true)
    })
    public Staff deleteStaff(String id) {
        String lockKey = "lock:staff:" + id;
       try {
           Staff staff = staffRepository.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

           List<StaffGroup> groups = staffGroupRepository.findAllByMemberIds(id);
           for (StaffGroup group : groups) {
               group.getMemberIds().remove(id);
               group.setUpdatedAt(LocalDate.now());
               staffGroupRepository.save(group);
           }
           staffRepository.deleteById(id);
           return staff;
       } finally {
           redisTemplate.delete(lockKey);
       }
    }

    @Override
    @Cacheable(value = CacheNameConstant.STAFF_DETAIL_CACHE, key = "#id", unless = "#result == null")
    public List<StaffGroup> getStaffGroupsById(String id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        List<StaffGroup> groups = staffGroupRepository.findAllByMemberIds(id);
        return groups;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_CACHE, allEntries = true)
    })
    public List<Staff> importStaff(byte[] fileContent) {
       try{
           List<CreateStaffDto> importDTOs = objectMapper.readValue(fileContent, new TypeReference<List<CreateStaffDto>>() {});
           List<Staff> createdStaff = new ArrayList<>();
           for (CreateStaffDto dto : importDTOs) {
               String staffCode = dto.getStaffCode();
               String lockKey = "lock:staff:create:" + staffCode;

               try {
                   Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, staffCode, Duration.ofSeconds(10));
                   if (locked == null || !locked) {
                       throw new RuntimeException("Could not acquire lock for staffCode: " + staffCode);
                   }

                   if (staffRepository.existsByStaffCode(staffCode)) {
                       continue;
                   }

                   Staff staff = Staff.builder()
                           .id(UUID.randomUUID().toString())
                           .staffCode(staffCode)
                           .name(dto.getName())
                           .email(dto.getEmail())
                           .createdAt(LocalDate.now())
                           .updatedAt(LocalDate.now())
                           .build();

                   createdStaff.add(staff);
               } finally {
                   redisTemplate.delete(lockKey);
               }
           }

           if (!createdStaff.isEmpty()) {
               staffRepository.saveAll(createdStaff);
           }

           return createdStaff;
       } catch (Exception e) {
           throw new RuntimeException("Failed to import staff data: " + e.getMessage());
       }
    }
}
