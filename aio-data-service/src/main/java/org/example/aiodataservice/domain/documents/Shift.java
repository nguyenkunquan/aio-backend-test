package org.example.aiodataservice.domain.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@environment.getProperty('app.elasticsearch.index.shift')}")
public class Shift {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String shiftId;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private ShiftType type;

    public enum ShiftType {
        MORNING, EVENING
    }

}
