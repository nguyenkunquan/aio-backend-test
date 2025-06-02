package org.example.aiodataservice.application.dtos.staffgroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffGroupDto {
    @NotBlank(message = "Group Code is required")
    @Size(max = 50, message = "Group Code must not exceed 50 characters")
    private String groupCode;

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    private String parentId;

    @Builder.Default
    private Set<String> childrenIds = new HashSet<>();
}
