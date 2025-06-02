package org.example.aiodataservice.domain.documents;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aiodataservice.application.dtos.staff.StaffGroupChildDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@environment.getProperty('app.elasticsearch.index.staff-group')}")
public class StaffGroup {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String groupCode;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String parentId;

    @Field(type = FieldType.Nested)
    private List<StaffGroupChildDto> children = new ArrayList<>();

    @Field(type = FieldType.Keyword)
    private Set<String> memberIds = new HashSet<>();

    @Field(type = FieldType.Date, format = DateFormat.date)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;
}
