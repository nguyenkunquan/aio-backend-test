package org.example.aiodataservice.application.services;

import org.example.aiodataservice.application.dtos.staffgroup.CreateStaffGroupDto;
import org.example.aiodataservice.domain.documents.Staff;
import org.example.aiodataservice.domain.documents.StaffGroup;

import java.util.List;
import java.util.Set;

public interface StaffGroupService {
    // Get all groups //add STAFF_GROUP_LIST_CACHE
    List<StaffGroup> getStaffGroups(int page, int size);

    // Get group by ID //add STAFF_GROUP_CACHE
    StaffGroup getStaffGroupById(String id);

    // Create group //remove STAFF_GROUP_LIST_CACHE - STAFF_GROUP_CACHE - RESOLVED_GROUP_MEMBERS_CACHE
    StaffGroup createStaffGroup(String groupCode, String name, String parentId, Set<String> childrenIds);

    // Update group //remove STAFF_GROUP_LIST_CACHE - STAFF_GROUP_CACHE - RESOLVED_GROUP_MEMBERS_CACHE
    StaffGroup updateStaffGroup(String id, String groupCode, String name, String parentId, Set<String> childrenIds);

    // Delete group //remove STAFF_GROUP_LIST_CACHE - STAFF_GROUP_CACHE - STAFF_DETAIL_CACHE - RESOLVED_GROUP_MEMBERS_CACHE
    StaffGroup deleteStaffGroup(String id);

    // Add staff to group //remove STAFF_GROUP_LIST_CACHE - STAFF_GROUP_CACHE - STAFF_DETAIL_CACHE - RESOLVED_GROUP_MEMBERS_CACHE
    StaffGroup addStaffToGroup(String id, String staffId);

    // Remove staff from group //remove STAFF_GROUP_LIST_CACHE - STAFF_GROUP_CACHE - STAFF_DETAIL_CACHE - RESOLVED_GROUP_MEMBERS_CACHE
    StaffGroup removeStaffFromGroup(String id, String staffId);

    // resolve group members by group id // add RESOLVED_GROUP_MEMBERS_CACHE
    Set<String> resolveGroupMembers(String id);

    Set<String> getGroupMembersById(String id);

    //remove STAFF_GROUP_LIST_CACHE - STAFF_GROUP_CACHE - RESOLVED_GROUP_MEMBERS_CACHE
    List<StaffGroup> importStaffGroups(byte[] fileContent);

}
