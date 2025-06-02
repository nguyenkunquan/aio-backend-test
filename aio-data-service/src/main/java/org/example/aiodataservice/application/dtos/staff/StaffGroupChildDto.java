package org.example.aiodataservice.application.dtos.staff;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffGroupChildDto {
    private String id;
    private String groupCode;
    private String name;
    private String parentId;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;
}
