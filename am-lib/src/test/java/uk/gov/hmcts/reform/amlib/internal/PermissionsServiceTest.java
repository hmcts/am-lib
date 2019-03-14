package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;

public class PermissionsServiceTest {

    private final PermissionsService permissionsService = new PermissionsService();

    @Test
    void whenMultiplePermissionsForSameAttributeShouldMergeTogether() {

        Map<JsonPointer, Set<Permission>> attributePermissionsOne =
            ImmutableMap.of(JsonPointer.valueOf(""), READ_PERMISSION);

        Map<JsonPointer, Set<Permission>> attributePermissionsTwo =
            ImmutableMap.of(JsonPointer.valueOf(""), CREATE_PERMISSION);

        List<Map<JsonPointer, Set<Permission>>> listOfPermissions =
            ImmutableList.<Map<JsonPointer, Set<Permission>>>builder()
                .add(attributePermissionsOne)
                .add(attributePermissionsTwo)
                .build();

        Map<JsonPointer, Set<Permission>> result = permissionsService.mergePermissions(listOfPermissions);

        System.out.println("result = " + result);

        assertThat(result).hasSize(1);
    }

    @Test
    void whenDuplicatePermissionsForSameAttributeShouldMergeTogether() {
        Map<JsonPointer, Set<Permission>> attributePermissionsOne =
            ImmutableMap.of(JsonPointer.valueOf(""), READ_PERMISSION);

        Map<JsonPointer, Set<Permission>> attributePermissionsTwo =
            ImmutableMap.of(JsonPointer.valueOf(""), READ_PERMISSION);

        List<Map<JsonPointer, Set<Permission>>> listOfPermissions =
            ImmutableList.<Map<JsonPointer, Set<Permission>>>builder()
                .add(attributePermissionsOne)
                .add(attributePermissionsTwo)
                .build();

        Map<JsonPointer, Set<Permission>> result = permissionsService.mergePermissions(listOfPermissions);

        assertThat(result).hasSize(1);
    }

    @Test
    void whenParentAndChildAttributeShouldMergePermissions() {
        //TODO: test outcome needs to combine permissions for attributePermission one due to parent child relationship

        Map<JsonPointer, Set<Permission>> attributePermissionsOne =
            ImmutableMap.of(JsonPointer.valueOf("/test/test2"), CREATE_PERMISSION);

        Map<JsonPointer, Set<Permission>> attributePermissionsTwo =
            ImmutableMap.of(JsonPointer.valueOf("/test"), READ_PERMISSION);

        List<Map<JsonPointer, Set<Permission>>> listOfPermissions =
            ImmutableList.<Map<JsonPointer, Set<Permission>>>builder()
                .add(attributePermissionsOne)
                .add(attributePermissionsTwo)
                .build();

        Map<JsonPointer, Set<Permission>> result = permissionsService.mergePermissions(listOfPermissions);

        System.out.println("result = " + result);
    }
}
