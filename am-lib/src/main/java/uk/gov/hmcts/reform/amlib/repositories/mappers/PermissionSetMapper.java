package uk.gov.hmcts.reform.amlib.repositories.mappers;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class PermissionSetMapper implements ColumnMapper<Set<Permission>> {
    @Override
    public Set<Permission> map(ResultSet resultSet, int columnNumber, StatementContext ctx) throws SQLException {
        return resultSet.wasNull() ? null : Permissions.fromSumOf(resultSet.getInt(columnNumber));
    }

    @Override
    public Set<Permission> map(ResultSet resultSet, String columnLabel, StatementContext ctx) throws SQLException {
        return resultSet.wasNull() ? null : Permissions.fromSumOf(resultSet.getInt(columnLabel));
    }
}
