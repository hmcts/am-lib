package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionsService {

    public Map<JsonPointer, Set<Permission>> mergePermissions(List<Map<JsonPointer, Set<Permission>>> records) {

        //TODO: logic for when parent permission should combine with child permission.

        return records.stream().flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (v1, v2) -> Permissions.fromSumOf(Permissions.sumOf(v1) + Permissions.sumOf(v2))
            ));
    }
}

