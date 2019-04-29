package uk.gov.hmcts.reform.amlib.internal.repositories.mappers;

import com.fasterxml.jackson.core.JsonPointer;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonPointerMapper implements ColumnMapper<JsonPointer> {
    @Override
    public JsonPointer map(ResultSet resultSet, int columnNumber, StatementContext ctx) throws SQLException {
        return resultSet.wasNull() ? null : JsonPointer.valueOf(resultSet.getString(columnNumber));
    }

    @Override
    public JsonPointer map(ResultSet resultSet, String columnLabel, StatementContext ctx) throws SQLException {
        return resultSet.wasNull() ? null : JsonPointer.valueOf(resultSet.getString(columnLabel));
    }
}
