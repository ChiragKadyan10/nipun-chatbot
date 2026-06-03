package com.nipun.userschool.service;

import com.nipun.userschool.entity.Teacher;
import com.nipun.userschool.repository.SchoolRepository;
import com.nipun.userschool.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;

    public Teacher saveTeacher(Teacher teacher) {
        log.info("Saving teacher: {} in current tenant schema", teacher.getName());
        Teacher saved = teacherRepository.save(teacher);
        populateTenantId(saved);
        return saved;
    }

    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = teacherRepository.findAll();
        teachers.forEach(this::populateTenantId);
        return teachers;
    }

    public Teacher getTeacherByPhoneNumber(String phoneNumber) {
        Teacher teacher = teacherRepository.findByPhoneNumber(phoneNumber)
                .orElse(null);
        populateTenantId(teacher);
        return teacher;
    }

    public Teacher getTeacherById(UUID id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        populateTenantId(teacher);
        return teacher;
    }

    private void populateTenantId(Teacher teacher) {
        if (teacher != null && teacher.getSchoolId() != null) {
            schoolRepository.findById(teacher.getSchoolId()).ifPresent(school -> {
                teacher.setTenantId(school.getCode());
            });
        }
    }
}
