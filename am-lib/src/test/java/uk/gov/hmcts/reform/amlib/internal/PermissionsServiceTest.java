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
        Map<JsonPointer, Set<Permission>> attributePermissionsOne = ImmutableMap.<JsonPointer, Set<Permission>>builder()
            .put(JsonPointer.valueOf(""), READ_PERMISSION)
            .build();

        Map<JsonPointer, Set<Permission>> attributePermissionsTwo = ImmutableMap.<JsonPointer, Set<Permission>>builder()
            .put(JsonPointer.valueOf(""), CREATE_PERMISSION)
            .build();

        List<Map<JsonPointer, Set<Permission>>> listOfPermissions =
            ImmutableList.<Map<JsonPointer, Set<Permission>>>builder()
                .add(attributePermissionsOne)
                .add(attributePermissionsTwo)
                .build();

        Map<JsonPointer, Set<Permission>> result = permissionsService.mergePermissions(listOfPermissions);

        assertThat(result).hasSize(1);
    }

    @Test
    void whenDuplicatePermissionsForSameAttributeShouldMergeTogether() {
        Map<JsonPointer, Set<Permission>> attributePermissionsOne = ImmutableMap.<JsonPointer, Set<Permission>>builder()
            .put(JsonPointer.valueOf(""), READ_PERMISSION)
            .build();

        Map<JsonPointer, Set<Permission>> attributePermissionsTwo = ImmutableMap.<JsonPointer, Set<Permission>>builder()
            .put(JsonPointer.valueOf(""), READ_PERMISSION)
            .build();

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
        Map<JsonPointer, Set<Permission>> attributePermissionsOne = ImmutableMap.<JsonPointer, Set<Permission>>builder()
            .put(JsonPointer.valueOf("/test/2"), READ_PERMISSION)
            .build();

        Map<JsonPointer, Set<Permission>> attributePermissionsTwo = ImmutableMap.<JsonPointer, Set<Permission>>builder()
            .put(JsonPointer.valueOf("/test"), CREATE_PERMISSION)
            .build();

        List<Map<JsonPointer, Set<Permission>>> listOfPermissions =
            ImmutableList.<Map<JsonPointer, Set<Permission>>>builder()
                .add(attributePermissionsOne)
                .add(attributePermissionsTwo)
                .build();

        Map<JsonPointer, Set<Permission>> result = permissionsService.mergePermissions(listOfPermissions);

        System.out.println("result = " + result);
    }
}
