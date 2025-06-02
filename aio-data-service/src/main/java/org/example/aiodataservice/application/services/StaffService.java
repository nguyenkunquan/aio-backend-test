package org.example.aiodataservice.application.services;

import org.example.aiodataservice.domain.documents.Staff;
import org.example.aiodataservice.domain.documents.StaffGroup;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StaffService {
    List<Staff> getStaffs(int page, int size);
    Staff getStaffById(String id);
//    ResponseEntity<?> findByStaffId(String staffId);
//    ResponseEntity<?> findByEmail(String email);
    Staff createStaff(String staffCode, String name, String email);
    Staff updateStaff(String id, String staffCode, String name, String email);
    Staff deleteStaff(String id);
    List<StaffGroup> getStaffGroupsById(String id);
    List<Staff> importStaff(byte[] fileContent);
}
