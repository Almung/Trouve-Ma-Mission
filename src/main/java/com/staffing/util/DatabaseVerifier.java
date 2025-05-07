package com.staffing.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class DatabaseVerifier implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        String sql = new String(Files.readAllBytes(
            Paths.get("src/main/resources/db/verify_columns.sql")));

        System.out.println("\n=== Database Structure Verification ===\n");
        jdbcTemplate.query(sql, (rs) -> {
            System.out.printf("Table: %-20s Column: %-20s Type: %-15s Default: %s%n",
                rs.getString("table_name"),
                rs.getString("column_name"),
                rs.getString("data_type"),
                rs.getString("column_default"));
        });
        System.out.println("\n=====================================\n");
    }
} 