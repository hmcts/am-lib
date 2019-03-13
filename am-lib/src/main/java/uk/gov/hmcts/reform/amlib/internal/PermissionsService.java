package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsService {

    public Map<JsonPointer, Set<Permission>> mergePermissions(List<Map<JsonPointer, Set<Permission>>> records) {

        Map<JsonPointer, Set<Permission>> map = new ConcurrentHashMap<>();

        //TODO: logic for when parent permission should combine with child permission.

        records.forEach(attributePermissions -> {

            System.out.println("attributePermissions = " + attributePermissions);

            attributePermissions.forEach((key, value) -> {


                System.out.println("key = " + key);

                map.merge(
                    key, value, (f, s) -> {

                        System.out.println("key = " + key);

                        f.addAll(s);

                        return f;
                    }
                );
            });
        });

        return map;
    }
}

