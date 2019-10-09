package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;

import java.util.Set;

public abstract class AttributeAccessDefinition {

    public abstract JsonPointer getAttribute();

    public abstract Set<Permission> getPermissions();

    public String getAttributeAsString() {
        return getAttribute().toString();
    }

    public int getPermissionsAsInt() {
        return Permissions.sumOf(getPermissions());
    }


}
