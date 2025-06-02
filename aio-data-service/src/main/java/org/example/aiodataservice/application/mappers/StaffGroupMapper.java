package org.example.aiodataservice.application.mappers;

import org.example.aiodataservice.application.dtos.staff.StaffGroupChildDto;
import org.example.aiodataservice.domain.documents.StaffGroup;

import java.util.List;
import java.util.stream.Collectors;

public class StaffGroupMapper {

    public static StaffGroupChildDto toChildDto(StaffGroup staffGroup) {
        return StaffGroupChildDto.builder()
                .id(staffGroup.getId())
                .groupCode(staffGroup.getGroupCode())
                .name(staffGroup.getName())
                .parentId(staffGroup.getParentId())
                .memberIds(staffGroup.getMemberIds())
                .createdAt(staffGroup.getCreatedAt())
                .updatedAt(staffGroup.getUpdatedAt())
                .build();
    }

    public static List<StaffGroupChildDto> toChildDtoList(List<StaffGroup> staffGroups) {
        return staffGroups.stream()
                .map(StaffGroupMapper::toChildDto)
                .collect(Collectors.toList());
    }
}