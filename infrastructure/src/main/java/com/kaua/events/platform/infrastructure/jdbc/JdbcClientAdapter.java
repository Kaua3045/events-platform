package com.kaua.events.platform.infrastructure.jdbc;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class JdbcClientAdapter implements DatabaseClient {

    private final JdbcClient jdbcClient;

    public JdbcClientAdapter(final JdbcClient jdbcClient) {
        this.jdbcClient = Objects.requireNonNull(jdbcClient);
    }

    @Override
    public <T> Optional<T> queryOne(final String sql, final Map<String, Object> params, final RowMap<T> mapper) {
        return this.jdbcClient
                .sql(sql)
                .params(params)
                .query(new RowMapAdapter<>(mapper))
                .optional();
    }

    @Override
    public <T> List<T> query(final String sql, final RowMap<T> mapper) {
        return this.jdbcClient
                .sql(sql)
                .query(new RowMapAdapter<>(mapper))
                .list();
    }

    @Override
    public <T> List<T> query(final String sql, final Map<String, Object> params, final RowMap<T> mapper) {
        return this.jdbcClient
                .sql(sql)
                .params(params)
                .query(new RowMapAdapter<>(mapper))
                .list();
    }

    @Override
    public int count(final String sql, final Map<String, Object> params) {
        return this.jdbcClient
                .sql(sql)
                .params(params)
                .query((rs, rowNum) -> rs.getInt(1))
                .single();
    }

    @Override
    public int update(final String sql, final Map<String, Object> params) {
        try {
            return this.jdbcClient
                    .sql(sql)
                    .params(params)
                    .update();
        } catch (final DataIntegrityViolationException ex) {
            throw ex;
        }
    }

    @Override
    public Number insert(final String sql, final Map<String, Object> params) {
        try {
            final var aHolder = new GeneratedKeyHolder();
            this.jdbcClient.sql(sql).params(params).update(aHolder);
            return aHolder.getKey();
        } catch (final DataIntegrityViolationException ex) {
            throw ex;
        }
    }
}