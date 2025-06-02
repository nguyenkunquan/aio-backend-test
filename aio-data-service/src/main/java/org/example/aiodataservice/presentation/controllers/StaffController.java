package org.example.aiodataservice.presentation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.aiodataservice.application.dtos.staff.CreateStaffDto;
import org.example.aiodataservice.application.dtos.staff.UpdateStaffDto;
import org.example.aiodataservice.application.services.StaffService;
import org.example.aiodataservice.domain.documents.Staff;
import org.example.aiodataservice.domain.documents.StaffGroup;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/staffs")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "APIs for managing staffs")
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @Operation(summary = "Get all staff members")
    public ResponseEntity<List<Staff>> findAll(@RequestParam (defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.ok(staffService.getStaffs(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff by ID")
    public ResponseEntity<Staff> findById(@PathVariable String id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }

    @PostMapping
    @Operation(summary = "Create new staff")
    public ResponseEntity<Staff> createStaff(@Valid @RequestBody CreateStaffDto request) {
        var result = staffService.createStaff(request.getStaffCode(), request.getName(), request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update staff")
    public ResponseEntity<Staff> updateStaff(@PathVariable String id, @Valid @RequestBody UpdateStaffDto request) {
        var result = staffService.updateStaff(id, request.getStaffCode(), request.getName(), request.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete staff")
    public ResponseEntity<Staff> deleteStaff(@PathVariable String id) {
        var result = staffService.deleteStaff(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @GetMapping("/{id}/groups")
    @Operation(summary = "Get staff groups by staff ID")
    public ResponseEntity<List<StaffGroup>> getStaffGroups(@PathVariable String id) {
        var groups = staffService.getStaffGroupsById(id);
        return ResponseEntity.ok(groups);
    }

    @Operation(
            summary = "Import staff from JSON file",
            description = "Uploads a JSON file containing a list of staff records (staffCode, name, email). Returns created staff.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Staff imported successfully"
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid file or JSON format")
            }
    )
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Staff>> importStaff(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            List<Staff> importedStaff = staffService.importStaff(file.getBytes());
            return ResponseEntity.ok(importedStaff);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
