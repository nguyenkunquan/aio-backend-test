package org.example.aiodataservice.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.aiodataservice.application.dtos.staff.CreateStaffDto;
import org.example.aiodataservice.application.dtos.staff.UpdateStaffDto;
import org.example.aiodataservice.application.dtos.staffgroup.CreateStaffGroupDto;
import org.example.aiodataservice.application.dtos.staffgroup.UpdateStaffGroupDto;
import org.example.aiodataservice.application.services.StaffGroupService;
import org.example.aiodataservice.application.services.StaffService;
import org.example.aiodataservice.domain.documents.Staff;
import org.example.aiodataservice.domain.documents.StaffGroup;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/staff-groups")
@RequiredArgsConstructor
@Tag(name = "Staff Group Management", description = "APIs for managing staff groups")
public class StaffGroupController {
    private final StaffGroupService staffGroupService;

    @GetMapping
    @Operation(summary = "Get all staff groups")
    public ResponseEntity<List<StaffGroup>> findAll(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.ok(staffGroupService.getStaffGroups(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff group by ID")
    public ResponseEntity<StaffGroup> findById(@PathVariable String id) {
        return ResponseEntity.ok(staffGroupService.getStaffGroupById(id));
    }

    @PostMapping
    @Operation(summary = "Create new staff group")
    public ResponseEntity<StaffGroup> createStaffGroup(@Valid @RequestBody CreateStaffGroupDto request) {
        var result = staffGroupService.createStaffGroup(request.getGroupCode(),
                request.getName(),
                request.getParentId(),
                request.getChildrenIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update staff group")
    public ResponseEntity<StaffGroup> updateStaffGroup(@PathVariable String id, @Valid @RequestBody UpdateStaffGroupDto request) {
        var result = staffGroupService.updateStaffGroup(id, request.getGroupCode(), request.getName(), request.getParentId(), request.getChildrenIds());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete staff group")
    public ResponseEntity<StaffGroup> deleteStaff(@PathVariable String id) {
        var result = staffGroupService.deleteStaffGroup(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @PutMapping("/{id}/staff/{staffId}")
    @Operation(summary = "Add staff to group")
    public ResponseEntity<StaffGroup> addStaffToGroup(
            @PathVariable String id,
            @PathVariable String staffId) {
        var result = staffGroupService.addStaffToGroup(id, staffId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @DeleteMapping("/{id}/staff/{staffId}")
    @Operation(summary = "Remove staff from group")
    public ResponseEntity<StaffGroup> removeStaffFromGroup(
            @PathVariable String id,
            @PathVariable String staffId) {
        var result = staffGroupService.removeStaffFromGroup(id, staffId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get members by group ID")
    public ResponseEntity<Set<String>> getGroupMembers(@PathVariable String id) {
        var result = staffGroupService.getGroupMembersById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/resolve-members")
    @Operation(summary = "Resolve group members by group ID")
    public ResponseEntity<Set<String>> resolveGroupMembers(@PathVariable String id) {
        var result = staffGroupService.resolveGroupMembers(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Import staff groups from JSON file",
            description = "Uploads a JSON file containing a list of staff group records (groupCode, name, parentId, childrenIds). Returns created staff groups.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Staff imported successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid file or JSON format")
            }
    )
    public ResponseEntity<List<StaffGroup>> importStaffGroup(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            List<StaffGroup> importedStaffGroup = staffGroupService.importStaffGroups(file.getBytes());
            return ResponseEntity.ok(importedStaffGroup);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
