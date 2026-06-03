package com.nipun.userschool.repository;

import com.nipun.userschool.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    Optional<Teacher> findByPhoneNumber(String phoneNumber);
    Optional<Teacher> findByEmail(String email);
}
