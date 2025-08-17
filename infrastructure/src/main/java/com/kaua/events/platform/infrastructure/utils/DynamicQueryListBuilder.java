package com.kaua.events.platform.infrastructure.utils;

import com.kaua.events.platform.domain.pagination.SearchQuery;

import java.util.*;

public final class DynamicQueryListBuilder {

    private DynamicQueryListBuilder() {
    }

    public static SqlQuery build(
            final String table,
            final SearchQuery query,
            final Specification specification,
            final List<String> allowedSortFields
    ) {
        Objects.requireNonNull(table, "table must not be null");
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(allowedSortFields, "allowedSortFields must not be null");

        final var sql = new StringBuilder("SELECT * FROM ").append(table).append(" ");
        final Map<String, Object> params = new HashMap<>();

        if (specification != null) {
            specification.applyJoins(sql);
        }

        sql.append("WHERE 1=1");

        if (specification != null) {
            specification.apply(sql, params);
        }

        appendOrderBy(sql, query, allowedSortFields);
        appendPagination(sql, params, query);

        return new SqlQuery(sql.toString(), params);
    }

    public static SqlQuery build(
            final String table,
            final SearchQuery query,
            final Specification specification,
            final List<String> allowedSortFields,
            final List<String> selectColumns
    ) {
        Objects.requireNonNull(table, "table must not be null");
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(allowedSortFields, "allowedSortFields must not be null");

        final var sql = new StringBuilder("SELECT ");
        if (selectColumns != null && !selectColumns.isEmpty()) {
            sql.append(String.join(", ", selectColumns));
        } else {
            sql.append("*");
        }
        sql.append(" FROM ").append(table).append(" ");

        final Map<String, Object> params = new HashMap<>();

        if (specification != null) {
            specification.applyJoins(sql);
        }

        sql.append("WHERE 1=1");

        if (specification != null) {
            specification.apply(sql, params);
        }

        appendOrderBy(sql, query, allowedSortFields);
        appendPagination(sql, params, query);

        return new SqlQuery(sql.toString(), params);
    }

    private static void appendOrderBy(
            final StringBuilder sql,
            final SearchQuery query,
            final List<String> allowedSortFields
    ) {
        final var sortField = allowedSortFields.contains(query.sort()) ? query.sort() : allowedSortFields.getFirst();
        final var sortDirection = Optional.ofNullable(query.direction())
                .map(String::toUpperCase)
                .filter(dir -> dir.equals("ASC") || dir.equals("DESC"))
                .orElse("ASC");
        sql.append(" ORDER BY ").append(sortField).append(" ").append(sortDirection);
    }

    private static void appendPagination(
            final StringBuilder sql,
            final Map<String, Object> params,
            final SearchQuery query
    ) {
        sql.append(" LIMIT :limit OFFSET :offset");
        params.put("limit", query.perPage());
        params.put("offset", Math.max(0, (query.page() - 1) * query.perPage()));
    }

    @FunctionalInterface
    public interface Specification {
        void apply(final StringBuilder sql, final Map<String, Object> params);

        default void applyJoins(final StringBuilder sql) {
            // default no-op
        }

        default Specification and(final Specification other) {
            return new Specification() {
                @Override
                public void apply(final StringBuilder sql, final Map<String, Object> params) {
                    Specification.this.apply(sql, params);
                    if (other != null) other.apply(sql, params);
                }

                @Override
                public void applyJoins(final StringBuilder sql) {
                    Specification.this.applyJoins(sql);
                    if (other != null) other.applyJoins(sql);
                }
            };
        }

        default Specification or(final Specification other) {
            return new Specification() {
                @Override
                public void apply(final StringBuilder sql, final Map<String, Object> params) {
                    if (other == null) {
                        Specification.this.apply(sql, params);
                        return;
                    }

                    final var leftSql = new StringBuilder();
                    final var rightSql = new StringBuilder();
                    final Map<String, Object> leftParams = new HashMap<>();
                    final Map<String, Object> rightParams = new HashMap<>();

                    Specification.this.apply(leftSql, leftParams);
                    other.apply(rightSql, rightParams);

                    var left = cleanWherePart(leftSql);
                    var right = cleanWherePart(rightSql);

                    if (!left.isEmpty() && !right.isEmpty()) {
                        sql.append(" AND (").append(left).append(" OR ").append(right).append(")");
                        params.putAll(leftParams);
                        params.putAll(rightParams);
                    } else if (!left.isEmpty()) {
                        sql.append(" AND (").append(left).append(")");
                        params.putAll(leftParams);
                    } else if (!right.isEmpty()) {
                        sql.append(" AND (").append(right).append(")");
                        params.putAll(rightParams);
                    }
                }

                @Override
                public void applyJoins(final StringBuilder sql) {
                    Specification.this.applyJoins(sql);
                    if (other != null) other.applyJoins(sql);
                }
            };
        }

        static String cleanWherePart(final StringBuilder sql) {
            return sql.toString().trim().replaceFirst("^AND\\s*", "");
        }

        static Specification where(final Specification spec) {
            return spec != null ? spec : (sql, params) -> {
            };
        }
    }

    public static Specification like(
            final String column,
            final String paramName,
            final String value
    ) {
        return applyIfNotBlank(value, (sql, params) -> {
            sql.append(" AND LOWER(").append(column).append(") LIKE :").append(paramName);
            params.put(paramName, "%" + value.toLowerCase() + "%");
        });
    }

    public static Specification equal(
            final String column,
            final String paramName,
            final Object value
    ) {
        return applyIfNotNull(value, (sql, params) -> {
            sql.append(" AND ").append(column).append(" = :").append(paramName);
            params.put(paramName, value);
        });
    }

    public static Specification notEqual(
            final String column,
            final String paramName,
            final Object value
    ) {
        return applyIfNotNull(value, (sql, params) -> {
            sql.append(" AND ").append(column).append(" != :").append(paramName);
            params.put(paramName, value);
        });
    }

    public static Specification lessThan(
            final String column,
            final String paramName,
            final Comparable<?> value
    ) {
        return applyIfNotNull(value, (sql, params) -> {
            sql.append(" AND ").append(column).append(" < :").append(paramName);
            params.put(paramName, value);
        });
    }

    public static Specification lessThanOrEqualTo(
            final String column,
            final String paramName,
            final Comparable<?> value
    ) {
        return applyIfNotNull(value, (sql, params) -> {
            sql.append(" AND ").append(column).append(" <= :").append(paramName);
            params.put(paramName, value);
        });
    }

    public static Specification greaterThan(
            final String column,
            final String paramName,
            final Comparable<?> value
    ) {
        return applyIfNotNull(value, (sql, params) -> {
            sql.append(" AND ").append(column).append(" > :").append(paramName);
            params.put(paramName, value);
        });
    }

    public static Specification greaterThanOrEqualTo(
            final String column,
            final String paramName,
            final Comparable<?> value
    ) {
        return applyIfNotNull(value, (sql, params) -> {
            sql.append(" AND ").append(column).append(" >= :").append(paramName);
            params.put(paramName, value);
        });
    }

    public static Specification in(
            final String column,
            final String paramName,
            final Collection<?> values
    ) {
        return applyIfNotEmpty(values, (sql, params) -> {
            sql.append(" AND ").append(column).append(" IN (:").append(paramName).append(")");
            params.put(paramName, values);
        });
    }

    public static Specification between(
            final String column,
            final String startParam,
            final String endParam,
            final Object start,
            final Object end
    ) {
        return (sql, params) -> {
            if (start != null && end != null) {
                sql.append(" AND ").append(column).append(" BETWEEN :")
                        .append(startParam).append(" AND :").append(endParam);
                params.put(startParam, start);
                params.put(endParam, end);
            }
        };
    }

    public static Specification isNull(final String column) {
        return (sql, params) -> sql.append(" AND ").append(column).append(" IS NULL");
    }

    public static Specification isNotNull(final String column) {
        return (sql, params) -> sql.append(" AND ").append(column).append(" IS NOT NULL");
    }

    public static Specification innerJoin(
            final String table,
            final String alias,
            final String condition
    ) {
        return join("INNER JOIN", table, alias, condition);
    }

    public static Specification leftJoin(
            final String table,
            final String alias,
            final String condition
    ) {
        return join("LEFT JOIN", table, alias, condition);
    }

    public static Specification rightJoin(
            final String table,
            final String alias,
            final String condition
    ) {
        return join("RIGHT JOIN", table, alias, condition);
    }

    private static Specification join(
            final String type,
            final String table,
            final String alias,
            final String condition
    ) {
        return new Specification() {
            @Override
            public void apply(final StringBuilder sql, final Map<String, Object> params) {
                // no-op in the WHERE clause
            }

            @Override
            public void applyJoins(final StringBuilder sql) {
                sql.append(type).append(" ").append(table).append(" ").append(alias)
                        .append(" ON ").append(condition).append(" ");
            }
        };
    }

    private static Specification applyIfNotNull(final Object value, final Specification spec) {
        return value != null ? spec : (sql, params) -> {
        };
    }

    private static Specification applyIfNotBlank(final String value, final Specification spec) {
        return value != null && !value.isBlank() ? spec : (sql, params) -> {
        };
    }

    private static Specification applyIfNotEmpty(final Collection<?> values, final Specification spec) {
        return values != null && !values.isEmpty() ? spec : (sql, params) -> {
        };
    }

    public record SqlQuery(String sql, Map<String, Object> params) {
    }
}
