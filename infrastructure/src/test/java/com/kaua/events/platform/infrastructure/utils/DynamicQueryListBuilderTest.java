package com.kaua.events.platform.infrastructure.utils;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.pagination.SearchQuery;
import com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.Specification;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DynamicQueryListBuilderTest extends UnitTest {

    @Test
    void givenEqualAndLeftJoinSpec_whenBuild_thenReturnQuery() {
        final var query = SearchQuery.newSearchQuery(2, 10, null, "name", "DESC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("users.active", "active", true);
        final var join = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.leftJoin("profiles", "p", "p.user_id = users.id");

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec.and(join),
                List.of("name", "created_at")
        );

        assertTrue(sqlQuery.sql().contains("LEFT JOIN profiles p ON p.user_id = users.id"));
        assertTrue(sqlQuery.sql().contains("AND users.active = :active"));
        assertTrue(sqlQuery.sql().contains("ORDER BY name DESC"));
        assertEquals(true, sqlQuery.params().get("active"));
        assertEquals(10, sqlQuery.params().get("limit"));
        assertEquals(10, sqlQuery.params().get("offset"));
    }

    @Test
    void givenOrSpec_whenBuild_thenReturnQueryWithOr() {
        final var query = SearchQuery.newSearchQuery(1, 5, null, "id", "ASC");
        final var spec1 = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("status", "status", "PENDING");
        final var spec2 = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("priority", "priority", "HIGH");

        final var spec = spec1.or(spec2);

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "tasks",
                query,
                spec,
                List.of("id", "created_at")
        );

        assertTrue(sqlQuery.sql().contains("(status = :status OR priority = :priority)"));
        assertEquals("PENDING", sqlQuery.params().get("status"));
        assertEquals("HIGH", sqlQuery.params().get("priority"));
        assertEquals(5, sqlQuery.params().get("limit"));
        assertEquals(0, sqlQuery.params().get("offset"));
    }

    @Test
    void givenNotEqualSpec_whenBuild_thenReturnQueryWithNotEqual() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "created_at", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.notEqual("deleted", "deleted", true);

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "documents",
                query,
                spec,
                List.of("created_at")
        );

        assertTrue(sqlQuery.sql().contains("AND deleted != :deleted"));
        assertEquals(true, sqlQuery.params().get("deleted"));
    }

    @Test
    void givenGreaterAndLessSpecs_whenBuild_thenReturnQueryWithComparisons() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "amount", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.greaterThan("price", "minPrice", 100)
                .and(com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.lessThan("price", "maxPrice", 500));

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "products",
                query,
                spec,
                List.of("amount", "price")
        );

        assertTrue(sqlQuery.sql().contains("AND price > :minPrice"));
        assertTrue(sqlQuery.sql().contains("AND price < :maxPrice"));
        assertEquals(100, sqlQuery.params().get("minPrice"));
        assertEquals(500, sqlQuery.params().get("maxPrice"));
    }

    @Test
    void givenGreaterOrEqualAndLessOrEqualSpecs_whenBuild_thenReturnQuery() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "created_at", "DESC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.greaterThanOrEqualTo("score", "minScore", 80)
                .and(com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.lessThanOrEqualTo("score", "maxScore", 100));

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "tests",
                query,
                spec,
                List.of("created_at")
        );

        assertTrue(sqlQuery.sql().contains("AND score >= :minScore"));
        assertTrue(sqlQuery.sql().contains("AND score <= :maxScore"));
        assertEquals(80, sqlQuery.params().get("minScore"));
        assertEquals(100, sqlQuery.params().get("maxScore"));
    }

    @Test
    void givenBetweenSpec_whenBuild_thenReturnQueryWithBetween() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "date", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.between("date", "startDate", "endDate", "2023-01-01", "2023-12-31");

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "events",
                query,
                spec,
                List.of("date")
        );

        assertTrue(sqlQuery.sql().contains("AND date BETWEEN :startDate AND :endDate"));
        assertEquals("2023-01-01", sqlQuery.params().get("startDate"));
        assertEquals("2023-12-31", sqlQuery.params().get("endDate"));
    }

    @Test
    void givenIsNullAndIsNotNullSpecs_whenBuild_thenReturnQuery() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.isNull("deleted_at")
                .and(com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.isNotNull("created_at"));

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "records",
                query,
                spec,
                List.of("id")
        );

        assertTrue(sqlQuery.sql().contains("AND deleted_at IS NULL"));
        assertTrue(sqlQuery.sql().contains("AND created_at IS NOT NULL"));
    }

    @Test
    void givenInSpec_whenBuild_thenReturnQueryWithInClause() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "name", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.in("category_id", "categories", Set.of(1, 2, 3));

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "products",
                query,
                spec,
                List.of("name")
        );

        assertTrue(sqlQuery.sql().contains("AND category_id IN (:categories)"));
        assertEquals(Set.of(1, 2, 3), sqlQuery.params().get("categories"));
    }

    @Test
    void givenInnerAndRightJoinSpecs_whenBuild_thenReturnQueryWithJoins() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        final var join1 = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.innerJoin("orders", "o", "o.user_id = users.id");
        final var join2 = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.rightJoin("payments", "p", "p.order_id = o.id");

        final var spec = join1.and(join2);

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("id")
        );

        assertTrue(sqlQuery.sql().contains("INNER JOIN orders o ON o.user_id = users.id"));
        assertTrue(sqlQuery.sql().contains("RIGHT JOIN payments p ON p.order_id = o.id"));
    }

    @Test
    void givenInvalidSortField_whenBuild_thenUseDefaultSortField() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "invalid_field", "ASC");

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "table_x",
                query,
                null,
                List.of("default_field")
        );

        assertTrue(sqlQuery.sql().contains("ORDER BY default_field ASC"));
    }

    @Test
    void givenNullSpec_whenBuild_thenReturnQueryWithoutWhereConditions() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "name", "ASC");

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "customers",
                query,
                null,
                List.of("name")
        );

        assertTrue(sqlQuery.sql().startsWith("SELECT * FROM customers "));
        assertTrue(sqlQuery.sql().contains("WHERE 1=1"));
    }

    @Test
    void givenLikeSpec_whenBuild_thenReturnQueryWithLikeClause() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "name", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.like("title", "title", "Evento");

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "events",
                query,
                spec,
                List.of("name")
        );

        assertTrue(sqlQuery.sql().contains("AND LOWER(title) LIKE :title"));
        assertEquals("%evento%", sqlQuery.params().get("title"));
    }

    @Test
    void givenEqualWithNullValue_thenSpecDoesNothing() {
        var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("field", "param", null);

        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "table_x",
                query,
                spec,
                List.of("id")
        );

        // Não deve ter filtro
        assertFalse(sqlQuery.sql().contains("field = :param"));
        assertFalse(sqlQuery.params().containsKey("param"));
    }

    @Test
    void givenLikeWithBlankValue_thenSpecDoesNothing() {
        var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.like("name", "nameParam", "   ");

        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "events",
                query,
                spec,
                List.of("id")
        );

        assertFalse(sqlQuery.sql().contains("LIKE"));
        assertFalse(sqlQuery.params().containsKey("nameParam"));
    }

    @Test
    void givenLikeWithNullValue_thenSpecDoesNothing() {
        var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.like("name", "nameParam", null);

        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "events",
                query,
                spec,
                List.of("id")
        );

        assertFalse(sqlQuery.sql().contains("LIKE"));
        assertFalse(sqlQuery.params().containsKey("nameParam"));
    }

    @Test
    void givenInWithEmptyValues_thenSpecDoesNothing() {
        var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.in("category_id", "categories", List.of());

        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "products",
                query,
                spec,
                List.of("id")
        );

        assertFalse(sqlQuery.sql().contains("IN (:categories)"));
        assertFalse(sqlQuery.params().containsKey("categories"));
    }

    @Test
    void givenInWithNullValues_thenSpecDoesNothing() {
        var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.in("category_id", "categories", null);

        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "products",
                query,
                spec,
                List.of("id")
        );

        assertFalse(sqlQuery.sql().contains("IN (:categories)"));
        assertFalse(sqlQuery.params().containsKey("categories"));
    }

    @Test
    void where_withNull_returnsNoOpSpec() {
        Specification spec = Specification.where(null);
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        spec.apply(sql, params);

        assertEquals("", sql.toString());
        assertTrue(params.isEmpty());
    }

    @Test
    void where_withNonNull_returnsSameSpec() {
        Specification original = (sql, params) -> sql.append("AND field = ?");
        Specification spec = Specification.where(original);

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        spec.apply(sql, params);

        assertEquals("AND field = ?", sql.toString());
    }

    @Test
    void and_withNullOther_appliesOnlyThis() {
        Specification left = (sql, params) -> sql.append("AND left = ?");
        Specification combined = left.and(null);

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        combined.apply(sql, params);

        assertEquals("AND left = ?", sql.toString());
    }

    @Test
    void and_withNonNullOther_appliesBoth() {
        Specification left = (sql, params) -> sql.append("AND left = ?");
        Specification right = (sql, params) -> sql.append(" AND right = ?");
        Specification combined = left.and(right);

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        combined.apply(sql, params);

        assertEquals("AND left = ? AND right = ?", sql.toString());
    }

    @Test
    void or_withNullOther_appliesOnlyThis() {
        Specification left = (sql, params) -> sql.append("AND left = ?");
        Specification combined = left.or(null);

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        combined.apply(sql, params);

        assertEquals("AND left = ?", sql.toString());
    }

    @Test
    void or_withBothHavingClauses_combinesWithOr() {
        Specification left = (sql, params) -> {
            sql.append("AND left = ?");
            params.put("p1", 1);
        };
        Specification right = (sql, params) -> {
            sql.append("AND right = ?");
            params.put("p2", 2);
        };
        Specification combined = left.or(right);

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        combined.apply(sql, params);

        assertEquals(" AND (left = ? OR right = ?)", sql.toString());
        assertTrue(params.containsKey("p1"));
        assertTrue(params.containsKey("p2"));
    }

    @Test
    void or_withLeftEmpty_andRightNonEmpty() {
        Specification left = (sql, params) -> {
            // no-op
        };
        Specification right = (sql, params) -> sql.append("AND right = ?");
        Specification combined = left.or(right);

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        combined.apply(sql, params);

        assertEquals(" AND (right = ?)", sql.toString());
    }

    @Test
    void or_withLeftEmpty_andRightEmpty() {
        Specification left = (sql, params) -> {
            // no-op
        };
        Specification right = (sql, params) -> {
            // no-op
        };
        Specification combined = left.or(right);

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        combined.apply(sql, params);

        assertTrue(sql.toString().isEmpty());
    }

    @Test
    void or_withRightEmpty_andLeftNonEmpty() {
        Specification left = (sql, params) -> sql.append("AND left = ?");
        Specification right = (sql, params) -> {
            // no-op
        };
        Specification combined = left.or(right);

        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        combined.apply(sql, params);

        assertEquals(" AND (left = ?)", sql.toString());
    }

    @Test
    void applyJoins_defaultDoesNothing() {
        Specification spec = (sql, params) -> {
            // no-op
        };
        StringBuilder sql = new StringBuilder();
        spec.applyJoins(sql);
        assertEquals("", sql.toString());
    }

    @Test
    void applyJoins_withAnd_combinesBoth() {
        Specification left = new Specification() {
            @Override
            public void apply(StringBuilder sql, Map<String, Object> params) {
            }

            @Override
            public void applyJoins(StringBuilder sql) {
                sql.append("LEFT JOIN A ");
            }
        };

        Specification right = new Specification() {
            @Override
            public void apply(StringBuilder sql, Map<String, Object> params) {
            }

            @Override
            public void applyJoins(StringBuilder sql) {
                sql.append("LEFT JOIN B ");
            }
        };

        Specification combined = left.and(right);
        StringBuilder sql = new StringBuilder();

        combined.applyJoins(sql);

        assertEquals("LEFT JOIN A LEFT JOIN B ", sql.toString());
    }

    @Test
    void applyJoins_withAnd_onOtherIsNull() {
        Specification left = new Specification() {
            @Override
            public void apply(StringBuilder sql, Map<String, Object> params) {
            }

            @Override
            public void applyJoins(StringBuilder sql) {
                sql.append("LEFT JOIN A ");
            }
        };

        Specification combined = left.and(null);
        StringBuilder sql = new StringBuilder();

        combined.applyJoins(sql);

        assertEquals("LEFT JOIN A ", sql.toString());
    }

    @Test
    void applyJoins_withOr_combinesBoth() {
        Specification left = new Specification() {
            @Override
            public void apply(StringBuilder sql, Map<String, Object> params) {
            }

            @Override
            public void applyJoins(StringBuilder sql) {
                sql.append("LEFT JOIN X ");
            }
        };

        Specification right = new Specification() {
            @Override
            public void apply(StringBuilder sql, Map<String, Object> params) {
            }

            @Override
            public void applyJoins(StringBuilder sql) {
                sql.append("LEFT JOIN Y ");
            }
        };

        Specification combined = left.or(right);
        StringBuilder sql = new StringBuilder();

        combined.applyJoins(sql);

        assertEquals("LEFT JOIN X LEFT JOIN Y ", sql.toString());
    }

    @Test
    void applyJoins_withOr_andOtherIsNull() {
        Specification left = new Specification() {
            @Override
            public void apply(StringBuilder sql, Map<String, Object> params) {
            }

            @Override
            public void applyJoins(StringBuilder sql) {
                sql.append("LEFT JOIN X ");
            }
        };

        Specification combined = left.or(null);
        StringBuilder sql = new StringBuilder();

        combined.applyJoins(sql);

        assertEquals("LEFT JOIN X ", sql.toString());
    }

    @Test
    void givenAnInvalidNullStartDate_whenCallBetween_thenNothingHappens() {
        // Given
        var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.between("users.created_at", "start", "end", null, "2023-12-31");

        // When
        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("id", "created_at")
        );

        // Then
        assertTrue(sqlQuery.sql().contains("WHERE 1=1"));
        assertTrue(sqlQuery.sql().contains("ORDER BY id ASC"));
    }

    @Test
    void givenAnInvalidNullEndDate_whenCallBetween_thenNothingHappens() {
        // Given
        var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.between("users.created_at", "start", "end", "2023-01-01", null);

        // When
        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("id", "created_at")
        );

        // Then
        assertTrue(sqlQuery.sql().contains("WHERE 1=1"));
        assertTrue(sqlQuery.sql().contains("ORDER BY id ASC"));
    }

    @Test
    void givenAValidDESCOrderBy_whenCallBuild_thenReturnQueryWithDESC() {
        // Given
        var query = SearchQuery.newSearchQuery(1, 10, null, "created_at", "DESC");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("users.active", "active", true);

        // When
        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("id", "created_at")
        );

        // Then
        assertTrue(sqlQuery.sql().contains("ORDER BY created_at DESC"));
    }

    @Test
    void givenAnInvalidSortOrder_whenCallBuild_thenUseDefaultSortOrder() {
        // Given
        var query = SearchQuery.newSearchQuery(1, 10, null, "invalid_field", "INVALID_ORDER");
        var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("users.active", "active", true);

        // When
        var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("id", "created_at")
        );

        // Then
        assertTrue(sqlQuery.sql().contains("ORDER BY id ASC"));
    }

    @Test
    void givenNullSelectColumns_whenBuild_thenSelectAllColumns() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("active", "active", true);

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("id", "name"),
                null
        );

        assertTrue(sqlQuery.sql().startsWith("SELECT * FROM users"));
        assertTrue(sqlQuery.sql().contains("AND active = :active"));
        assertEquals(true, sqlQuery.params().get("active"));
    }

    @Test
    void givenEmptySelectColumns_whenBuild_thenSelectAllColumns() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("active", "active", true);

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("id", "name"),
                List.of()
        );

        assertTrue(sqlQuery.sql().startsWith("SELECT * FROM users"));
        assertTrue(sqlQuery.sql().contains("AND active = :active"));
        assertEquals(true, sqlQuery.params().get("active"));
    }

    @Test
    void givenSelectColumns_whenBuild_thenSelectOnlyThoseColumns() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("active", "active", true);

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("id", "name"),
                List.of("id", "name", "email")
        );

        assertTrue(sqlQuery.sql().startsWith("SELECT id, name, email FROM users"));
        assertTrue(sqlQuery.sql().contains("AND active = :active"));
        assertEquals(true, sqlQuery.params().get("active"));
    }

    @Test
    void givenSelectColumnsWithJoins_whenBuild_thenSelectRespected() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");
        final var join = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.leftJoin("profiles", "p", "p.user_id = users.id");

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                join,
                List.of("id"),
                List.of("users.id", "p.bio")
        );

        assertTrue(sqlQuery.sql().startsWith("SELECT users.id, p.bio FROM users"));
        assertTrue(sqlQuery.sql().contains("LEFT JOIN profiles p ON p.user_id = users.id"));
    }

    @Test
    void givenSelectColumnsAndSpec_whenBuild_thenSelectAndFiltersApplied() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "name", "ASC");
        final var spec = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.equal("active", "active", true)
                .and(com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.like("name", "nameParam", "John"));

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("name"),               // allowedSortFields
                List.of("id", "name", "email") // selectColumns
        );

        assertTrue(sqlQuery.sql().startsWith("SELECT id, name, email FROM users"));

        assertTrue(sqlQuery.sql().contains("AND active = :active"));
        assertTrue(sqlQuery.sql().contains("AND LOWER(name) LIKE :nameParam"));

        assertEquals(true, sqlQuery.params().get("active"));
        assertEquals("%john%", sqlQuery.params().get("nameParam"));
    }

    @Test
    void givenSelectColumnsWithJoinAndSpec_whenBuild_thenQueryCorrect() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "created_at", "DESC");
        final var join = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.leftJoin("profiles", "p", "p.user_id = users.id");
        final var spec = join.and(com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.notEqual("deleted", "deleted", true));

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                spec,
                List.of("created_at"),           // allowedSortFields
                List.of("users.id", "users.name", "p.bio") // selectColumns
        );

        assertTrue(sqlQuery.sql().startsWith("SELECT users.id, users.name, p.bio FROM users"));

        assertTrue(sqlQuery.sql().contains("LEFT JOIN profiles p ON p.user_id = users.id"));

        assertTrue(sqlQuery.sql().contains("AND deleted != :deleted"));
        assertEquals(true, sqlQuery.params().get("deleted"));
    }

    @Test
    void givenNullSpecification_whenBuild_thenQueryWithoutSpecApplied() {
        final var query = SearchQuery.newSearchQuery(1, 10, null, "id", "ASC");

        final var sqlQuery = com.kaua.events.platform.infrastructure.utils.DynamicQueryListBuilder.build(
                "users",
                query,
                null,                     // specification nulo
                List.of("id", "name"),    // allowedSortFields
                List.of("id", "name")     // selectColumns
        );

        assertTrue(sqlQuery.sql().startsWith("SELECT id, name FROM users"));

        assertFalse(sqlQuery.sql().contains("JOIN"));
        assertFalse(sqlQuery.sql().contains("AND"));

        assertTrue(sqlQuery.sql().contains("ORDER BY id ASC"));
        assertEquals(10, sqlQuery.params().get("limit"));
        assertEquals(0, sqlQuery.params().get("offset"));
    }
}
