package uk.gov.hmcts.reform.amlib.internal.repositories.mappers;

import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import uk.gov.hmcts.reform.amlib.models.DefaultRolePermissions;
import uk.gov.hmcts.reform.amlib.models.RolePermissionsForCaseTypeEnvelope;

import java.util.Map;

public class RolePermissionsForCaseTypeEnvelopeReducer implements
    LinkedHashMapRowReducer<String, RolePermissionsForCaseTypeEnvelope> {

    @Override
    public void accumulate(Map<String, RolePermissionsForCaseTypeEnvelope> map, RowView rowView) {

        final RolePermissionsForCaseTypeEnvelope rolePermissionsForCaseTypeEnvelope =
            map.computeIfAbsent(rowView.getColumn("caseTypeId", String.class),
                id -> rowView.getRow(RolePermissionsForCaseTypeEnvelope.class));

        if (rowView.getColumn("role", String.class) != null) {
            rolePermissionsForCaseTypeEnvelope.addDefaultRolePermissions(rowView.getRow(DefaultRolePermissions.class));
        }
    }
}
