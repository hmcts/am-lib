package uk.gov.hmcts.reform.amlib.models;

import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExplicitPermissions {
    private final Set<Permission> userPermissions;

    public ExplicitPermissions(Set<Permission> userPermissions) {
        this.userPermissions = userPermissions;
    }

    public ExplicitPermissions(Permission...userPermissions) {
        this.userPermissions = Collections.unmodifiableSet(Stream.of(userPermissions).collect(Collectors.toSet()));
    }

    public Set<Permission> getUserPermissions() {
        return userPermissions;
    }
}
