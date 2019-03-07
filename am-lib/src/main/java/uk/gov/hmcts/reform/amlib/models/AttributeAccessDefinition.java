package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Set;

public interface AttributeAccessDefinition {

    JsonPointer getAttribute();

    String getAttributeAsString();

    Set<Permission> getPermissions();

    int getPermissionsAsInt();

}
