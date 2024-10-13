package com.chung.ai.ontology.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * The FlexibleDataAccessLayer class is a Spring repository that provides flexible data access methods.
 * It uses JdbcTemplate to execute SQL queries and retrieve results as a list of maps.
 * This class logs the executed SQL queries and their parameters for debugging purposes.
 *
 * Author: Chung Ha
 */
@Repository
@Slf4j
public class FlexibleDataAccessLayer {

    private final JdbcTemplate jdbcTemplate;

    public FlexibleDataAccessLayer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> executeQuery(String sql, Object... params) {
        log.info("Executing query: {}", sql);
        for (Object param : params) {
            log.info("Param value: {}", param);
        }
        return jdbcTemplate.queryForList(sql, params);
    }
}
