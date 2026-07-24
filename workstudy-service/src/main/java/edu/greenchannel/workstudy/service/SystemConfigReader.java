package edu.greenchannel.workstudy.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SystemConfigReader {
    private final JdbcTemplate jdbcTemplate;

    public SystemConfigReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BigDecimal positiveDecimal(String key, BigDecimal defaultValue) {
        return jdbcTemplate.query("""
                        SELECT config_value FROM gc_system_config
                        WHERE config_key = ? AND is_deleted = 0
                        LIMIT 1
                        """,
                resultSet -> {
                    if (!resultSet.next()) {
                        return defaultValue;
                    }
                    try {
                        BigDecimal value = new BigDecimal(resultSet.getString(1));
                        return value.signum() > 0 ? value : defaultValue;
                    } catch (RuntimeException exception) {
                        return defaultValue;
                    }
                }, key);
    }
}
