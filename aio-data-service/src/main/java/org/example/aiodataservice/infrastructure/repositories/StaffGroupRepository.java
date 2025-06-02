package org.example.aiodataservice.infrastructure.repositories;

import org.example.aiodataservice.domain.documents.StaffGroup;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffGroupRepository extends ElasticsearchRepository<StaffGroup, String> {
    Optional<StaffGroup> findByGroupCode(String groupCode);
    List<StaffGroup> findByParentId(String parentId);
    Boolean existsByGroupCode(String groupCode);
    List<StaffGroup> findAllByMemberIds(String id);
}
