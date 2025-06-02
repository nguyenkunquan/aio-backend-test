package org.example.aiodataservice.infrastructure.repositories;

import org.springframework.data.domain.Page;
import org.example.aiodataservice.domain.documents.Staff;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends ElasticsearchRepository<Staff, String> {
    Optional<Staff> findByStaffCode(String staffCode);
    Optional<Staff> findByEmail(String email);
    Page<Staff> findAll(Pageable pageable);
    Boolean existsByStaffCode(String staffCode);
}
