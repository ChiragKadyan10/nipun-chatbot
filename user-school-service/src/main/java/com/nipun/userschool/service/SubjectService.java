package com.nipun.userschool.service;

import com.nipun.userschool.entity.Subject;
import com.nipun.userschool.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public Subject saveSubject(Subject subject) {
        log.info("Saving subject: {} with code: {}", subject.getName(), subject.getCode());
        return subjectRepository.save(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectById(UUID id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));
    }

    public Subject getSubjectByCode(String code) {
        return subjectRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Subject not found with code: " + code));
    }
}