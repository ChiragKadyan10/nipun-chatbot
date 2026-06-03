package com.nipun.userschool.service;

import com.nipun.userschool.entity.School;
import com.nipun.userschool.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final DataSource dataSource;

    @Transactional
    public School createSchool(School school) {
        log.info("Creating school: {} with code: {}", school.getName(), school.getCode());
        School savedSchool = schoolRepository.save(school);

        String schemaName = "tenant_" + school.getCode().toLowerCase();

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            }
            log.info("Successfully created schema: {}", schemaName);

            // Dynamically apply Flyway migrations for this new school schema
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations("classpath:db/migration/tenant")
                    .baselineOnMigrate(true)
                    .load();
            flyway.migrate();
            log.info("Successfully completed Flyway migrations for schema: {}", schemaName);

        } catch (SQLException e) {
            log.error("Failed to provision schema and tables for tenant schema: {}", schemaName, e);
            throw new RuntimeException("Could not create schema for school tenant: " + schemaName, e);
        }

        return savedSchool;
    }

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }
}
