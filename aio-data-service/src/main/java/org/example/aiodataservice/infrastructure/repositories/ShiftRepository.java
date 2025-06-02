package org.example.aiodataservice.infrastructure.repositories;

import org.example.aiodataservice.domain.documents.Shift;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShiftRepository extends ElasticsearchRepository<Shift, String> {
    Optional<Shift> findByShiftId(String shiftId);
}
