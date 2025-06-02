package org.example.aiodataservice.application.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aiodataservice.application.constants.CacheNameConstant;
import org.example.aiodataservice.application.dtos.staff.StaffGroupChildDto;
import org.example.aiodataservice.application.dtos.staffgroup.CreateStaffGroupDto;
import org.example.aiodataservice.application.dtos.staffgroup.ImportStaffGroupDto;
import org.example.aiodataservice.application.exceptions.DuplicateResourceException;
import org.example.aiodataservice.application.exceptions.ResourceNotFoundException;
import org.example.aiodataservice.application.mappers.StaffGroupMapper;
import org.example.aiodataservice.application.services.StaffGroupService;
import org.example.aiodataservice.application.services.StaffService;
import org.example.aiodataservice.domain.documents.Staff;
import org.example.aiodataservice.domain.documents.StaffGroup;
import org.example.aiodataservice.infrastructure.repositories.StaffGroupRepository;
import org.example.aiodataservice.infrastructure.repositories.StaffRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class StaffGroupServiceImpl implements StaffGroupService {

    private final StaffRepository staffRepository;
    private final StaffGroupRepository staffGroupRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = CacheNameConstant.STAFF_GROUP_LIST_CACHE)
    public List<StaffGroup> getStaffGroups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var staffGroups = staffGroupRepository.findAll(pageable).getContent();
        return staffGroups;
    }

    @Override
    @Cacheable(value = CacheNameConstant.STAFF_GROUP_CACHE, key = "#id", unless = "#result == null")
    public StaffGroup getStaffGroupById(String id) {
        return staffGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff group not found with id: " + id));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_CACHE, key = "#result.id"),
            @CacheEvict(value = CacheNameConstant.RESOLVED_GROUP_MEMBERS_CACHE, allEntries = true)
    })
    public StaffGroup createStaffGroup(String groupCode, String name, String parentId, Set<String> childrenIds) {
        String lockKey = "lock:staffgroup:create:" + groupCode;
        try{
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, groupCode, Duration.ofSeconds(10));
            if (locked == null || !locked) {
                throw new RuntimeException("Could not acquire lock for groupCode: " + groupCode);
            }
            if (staffGroupRepository.existsByGroupCode(groupCode)) {
                throw new DuplicateResourceException("StaffGroup already exist with code: " + groupCode);
            }

            List<StaffGroup> groupsToSave = new ArrayList<>();
            StaffGroup staffGroup = StaffGroup.builder()
                    .id(UUID.randomUUID().toString())
                    .groupCode(groupCode)
                    .name(name)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();
            groupsToSave.add(staffGroup);

            List<StaffGroupChildDto> children = new ArrayList<>();
            if (childrenIds != null && childrenIds.isEmpty()) {
                for (String childId : childrenIds) {
                    StaffGroup childGroup = staffGroupRepository.findById(childId)
                            .orElseThrow(() -> new ResourceNotFoundException("Child group not found with id: " + childId));
                    if (childGroup.getParentId() != null) {
                        throw new DuplicateResourceException("Child group already has a parent: " + childId);
                    }
                    childGroup.setParentId(staffGroup.getId());
                    childGroup.setUpdatedAt(LocalDate.now());
                    groupsToSave.add(childGroup);
                    children.add(StaffGroupMapper.toChildDto(childGroup));
                }
            }
            staffGroup.setChildren(children);
            if(parentId != null) {
                StaffGroup parentGroup = staffGroupRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Parent group not found with id: " + parentId));
                staffGroup.setParentId(parentGroup.getId());
                parentGroup.getChildren().add(StaffGroupMapper.toChildDto(staffGroup));
                parentGroup.setUpdatedAt(LocalDate.now());
                groupsToSave.add(parentGroup);
            }
            staffGroupRepository.saveAll(groupsToSave);
            return staffGroup;
        }
        finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_CACHE, key = "#result.id"),
            @CacheEvict(value = CacheNameConstant.RESOLVED_GROUP_MEMBERS_CACHE, allEntries = true)
    })
    public StaffGroup updateStaffGroup(String id, String groupCode, String name, String parentId, Set<String> childrenIds) {
        String lockKey = "lock:staffgroup:" + id;
        try{
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, id, Duration.ofSeconds(10));
            if (locked == null || !locked) {
                throw new RuntimeException("Could not acquire lock for id: " + id);
            }
            StaffGroup staffGroup = staffGroupRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff group not found with id: " + id));
            if(groupCode == null || groupCode.isEmpty()) {
                throw new IllegalArgumentException("Group ID cannot be null or empty");
            }
            if (staffGroupRepository.existsByGroupCode(groupCode) && !staffGroup.getGroupCode().equals(groupCode)) {
                throw new DuplicateResourceException("StaffGroup already exist with group code: " + groupCode);
            }

            List<StaffGroup> groupsToSave = new ArrayList<>();
            groupsToSave.add(staffGroup);

            staffGroup.setGroupCode(groupCode);
            staffGroup.setName(name);
            if(childrenIds != null){
                for (StaffGroupChildDto oldChildDto : staffGroup.getChildren()) {
                    if (!childrenIds.contains(oldChildDto.getId())) {
                        StaffGroup oldChild = staffGroupRepository.findById(oldChildDto.getId())
                                .orElse(null);
                        if (oldChild != null) {
                            oldChild.setParentId(null);
                            oldChild.setUpdatedAt(LocalDate.now());
                            groupsToSave.add(oldChild);
                        }
                    }
                }
                List<StaffGroupChildDto> newChildren = new ArrayList<>();
                for (String childId : childrenIds) {
                    StaffGroup childGroup = staffGroupRepository.findById(childId)
                            .orElseThrow(() -> new ResourceNotFoundException("Child group not found with id: " + childId));
                    if (childGroup.getParentId() != null && !childGroup.getParentId().equals(id)) {
                        throw new DuplicateResourceException("Child group already has a parent: " + childId);
                    }
                    if (childGroup.getParentId() == null) {
                        childGroup.setParentId(id);
                        childGroup.setUpdatedAt(LocalDate.now());
                        groupsToSave.add(childGroup);
                        newChildren.add(StaffGroupMapper.toChildDto(childGroup));
                    }
                }
                staffGroup.setChildren(newChildren);
            } else if((childrenIds == null || childrenIds.isEmpty()) && !staffGroup.getChildren().isEmpty()) {
                for (StaffGroupChildDto childDto : staffGroup.getChildren()) {
                    StaffGroup childGroup = staffGroupRepository.findById(childDto.getId())
                            .orElse(null);
                    if (childGroup != null) {
                        childGroup.setParentId(null);
                        childGroup.setUpdatedAt(LocalDate.now());
                        groupsToSave.add(childGroup);
                    }
                }
                staffGroup.setChildren(new ArrayList<>());
            }


            String oldParentId = staffGroup.getParentId();
            if(parentId != null && !parentId.equals(oldParentId)) {
                StaffGroup newParentGroup = staffGroupRepository.findById(parentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Parent group not found with id: " + parentId));
                StaffGroup oldParentGroup = staffGroupRepository.findById(staffGroup.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Old parent group not found with id: " + staffGroup.getParentId()));
                oldParentGroup.getChildren().removeIf(child -> child.getId().equals(id));
                oldParentGroup.setUpdatedAt(LocalDate.now());
                staffGroup.setParentId(newParentGroup.getId());
                newParentGroup.getChildren().add(StaffGroupMapper.toChildDto(staffGroup));
                newParentGroup.setUpdatedAt(LocalDate.now());
                groupsToSave.add(oldParentGroup);
                groupsToSave.add(newParentGroup);
            } else if (parentId == null && oldParentId != null){
                StaffGroup oldParent = staffGroupRepository.findById(oldParentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Old parent group not found with id: " + oldParentId));
                oldParent.getChildren().removeIf(child -> child.getId().equals(id));
                oldParent.setUpdatedAt(LocalDate.now());
                groupsToSave.add(oldParent);
                staffGroup.setParentId(null);
            }
            staffGroup.setUpdatedAt(LocalDate.now());
            staffGroupRepository.saveAll(groupsToSave);
            return staffGroup;
        }
        finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_CACHE, key = "#result.id"),
            @CacheEvict(value = CacheNameConstant.STAFF_DETAIL_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.RESOLVED_GROUP_MEMBERS_CACHE, allEntries = true)
    })
    public StaffGroup deleteStaffGroup(String id) {

        String lockKey = "lock:staffgroup:" + id;
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, id, Duration.ofSeconds(10));
            if (locked == null || !locked) {
                throw new RuntimeException("Could not acquire lock for group id: " + id);
            }

            StaffGroup staffGroup = staffGroupRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("StaffGroup not found with id: " + id));

            List<StaffGroup> groupsToUpdate = new ArrayList<>();

            if (staffGroup.getParentId() != null) {
                StaffGroup parentGroup = staffGroupRepository.findById(staffGroup.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Parent group not found with id: " + staffGroup.getParentId()));
                parentGroup.getChildren().removeIf(child -> child.getId().equals(id));
                parentGroup.setUpdatedAt(LocalDate.now());
                groupsToUpdate.add(parentGroup);
            }

            for (StaffGroupChildDto childGroupDto : staffGroup.getChildren()) {
                StaffGroup childGroup = staffGroupRepository.findById(childGroupDto.getId())
                        .orElse(null);
                if (childGroup != null) {
                    childGroup.setParentId(null);
                    childGroup.setUpdatedAt(LocalDate.now());
                    groupsToUpdate.add(childGroup);
                }
            }
            if (!groupsToUpdate.isEmpty()) {
                staffGroupRepository.saveAll(groupsToUpdate);
            }
            staffGroupRepository.deleteById(id);
            return staffGroup;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_CACHE, key = "#result.id"),
            @CacheEvict(value = CacheNameConstant.STAFF_DETAIL_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.RESOLVED_GROUP_MEMBERS_CACHE, allEntries = true)
    })
    public StaffGroup addStaffToGroup(String id, String staffId) {
        String lockey = "lock:staffgroup:" + id;
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockey, id, Duration.ofSeconds(10));
            if (locked == null || !locked) {
                throw new RuntimeException("Could not acquire lock for group id: " + id);
            }
            StaffGroup staffGroup = staffGroupRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff Group not found with id: " + id));
            if (!staffRepository.existsById(staffId)) {
                throw new ResourceNotFoundException("Staff not found with id: " + staffId);
            }
            staffGroup.getMemberIds().add(staffId);
            staffGroup.setUpdatedAt(LocalDate.now());
            staffGroupRepository.save(staffGroup);
            return staffGroup;
        } finally {
            redisTemplate.delete(lockey);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_CACHE, key = "#result.id"),
            @CacheEvict(value = CacheNameConstant.STAFF_DETAIL_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.RESOLVED_GROUP_MEMBERS_CACHE, allEntries = true)
    })
    public StaffGroup removeStaffFromGroup(String id, String staffId) {
        String lockey = "lock:staffgroup:" + id;
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockey, id, Duration.ofSeconds(10));
            if (locked == null || !locked) {
                throw new RuntimeException("Could not acquire lock for group id: " + id);
            }
            StaffGroup staffGroup = staffGroupRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff Group not found with id: " + id));
            if (!staffRepository.existsById(staffId)) {
                throw new ResourceNotFoundException("Staff not found with id: " + staffId);
            }
            staffGroup.getMemberIds().remove(staffId);
            staffGroup.setUpdatedAt(LocalDate.now());
            staffGroupRepository.save(staffGroup);
            return staffGroup;
        } finally {
            redisTemplate.delete(lockey);
        }
    }

    @Override
    @Cacheable(value = CacheNameConstant.RESOLVED_GROUP_MEMBERS_CACHE, key = "#id", unless = "#result == null")
    public Set<String> resolveGroupMembers(String id) {
        StaffGroup staffGroup = staffGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StaffGroup not found with id: " + id));
        return resolveGroupMembersRecursive(staffGroup);
    }

    @Override
    public Set<String> getGroupMembersById(String id) {
        StaffGroup staffGroup = staffGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StaffGroup not found with id: " + id));
        return staffGroup.getMemberIds();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.STAFF_GROUP_CACHE, allEntries = true),
            @CacheEvict(value = CacheNameConstant.RESOLVED_GROUP_MEMBERS_CACHE, allEntries = true)
    })
    public List<StaffGroup> importStaffGroups(byte[] fileContent) {
        try {
            List<ImportStaffGroupDto> importDTOs = objectMapper.readValue(fileContent, new TypeReference<List<ImportStaffGroupDto>>() {});
            List<StaffGroup> createdGroups = new ArrayList<>();
            Map<String, String> groupCodeToId = new HashMap<>();

            // Phase 1
            for (ImportStaffGroupDto dto : importDTOs) {
                String groupCode = dto.getGroupCode();
                String lockKey = "lock:staffgroup:import:" + groupCode;

                try {
                    Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, groupCode, Duration.ofSeconds(10));
                    if (locked == null || !locked) {
                        throw new RuntimeException("Could not acquire lock for groupCode: " + groupCode);
                    }

                    if (staffGroupRepository.existsByGroupCode(groupCode)) {
                        continue;
                    }

                    StaffGroup staffGroup = StaffGroup.builder()
                            .id(UUID.randomUUID().toString())
                            .groupCode(groupCode)
                            .name(dto.getName())
                            .createdAt(LocalDate.now())
                            .updatedAt(LocalDate.now())
                            .children(new ArrayList<>())
                            .build();

                    createdGroups.add(staffGroup);
                    groupCodeToId.put(groupCode, staffGroup.getId());
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }

            if (!createdGroups.isEmpty()) {
                staffGroupRepository.saveAll(createdGroups);
            }

            // Phase 2
            List<StaffGroup> updatedGroups = new ArrayList<>();

            for (ImportStaffGroupDto dto : importDTOs) {
                StaffGroup staffGroup = createdGroups.stream()
                        .filter(g -> g.getGroupCode().equals(dto.getGroupCode()))
                        .findFirst()
                        .orElseGet(() -> staffGroupRepository.findByGroupCode(dto.getGroupCode())
                                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + dto.getGroupCode())));

                String lockKey = "lock:staffgroup:import:" + dto.getGroupCode();
                try {
                    Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, dto.getGroupCode(), Duration.ofSeconds(10));
                    if (locked == null || !locked) {
                        throw new RuntimeException("Could not acquire lock for groupCode: " + dto.getGroupCode());
                    }

                    if (dto.getParentCode() != null) {
                        String parentId = groupCodeToId.get(dto.getParentCode());
                        StaffGroup parentGroup;

                        if (parentId != null) {
                            parentGroup = createdGroups.stream()
                                    .filter(g -> g.getId().equals(parentId))
                                    .findFirst()
                                    .orElseGet(() -> staffGroupRepository.findById(parentId)
                                            .orElseThrow(() -> new ResourceNotFoundException("Parent group not found with id: " + parentId)));
                        } else {
                            parentGroup = staffGroupRepository.findByGroupCode(dto.getParentCode())
                                    .orElseThrow(() -> new ResourceNotFoundException("Parent group not found with code: " + dto.getParentCode()));
                        }

                        staffGroup.setParentId(parentGroup.getId());

                        if (parentGroup.getChildren() == null) {
                            parentGroup.setChildren(new ArrayList<>());
                        }
                        parentGroup.getChildren().add(StaffGroupMapper.toChildDto(staffGroup));
                        parentGroup.setUpdatedAt(LocalDate.now());

                        updatedGroups.add(parentGroup);
                    }

                    staffGroup.setUpdatedAt(LocalDate.now());
                    updatedGroups.add(staffGroup);
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }

            if (!updatedGroups.isEmpty()) {
                staffGroupRepository.saveAll(updatedGroups);
            }

            return createdGroups;
        } catch (Exception e) {
            throw new RuntimeException("Failed to import staff groups: " + e.getMessage(), e);
        }
    }

    private Set<String> resolveGroupMembersRecursive(StaffGroup group) {
        Set<String> members = new HashSet<>(group.getMemberIds());

        for (StaffGroupChildDto childDto : group.getChildren()) {
            StaffGroup childGroup = staffGroupRepository.findById(childDto.getId())
                    .orElse(null);
            if (childGroup != null) {
                members.addAll(resolveGroupMembersRecursive(childGroup));
            }
        }

        return members;
    }
}
