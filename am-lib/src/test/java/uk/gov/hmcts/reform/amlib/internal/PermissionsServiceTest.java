package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DELETE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;

@SuppressWarnings("PMD")
class PermissionsServiceTest {

    private final PermissionsService permissionsService = new PermissionsService();

    @Test
    void whenMultiplePermissionsForSameAttributeShouldMergeTogether() {
        JsonPointer attribute = JsonPointer.valueOf("");

        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(attribute, READ_PERMISSION),
            ImmutableMap.of(attribute, CREATE_PERMISSION)
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(1)
            .containsEntry(attribute, ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenDuplicatePermissionsForSameAttributeShouldMergeTogether() {
        JsonPointer attribute = JsonPointer.valueOf("");

        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(attribute, READ_PERMISSION),
            ImmutableMap.of(attribute, READ_PERMISSION)
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(1)
            .containsEntry(attribute, READ_PERMISSION);
    }

    @Test
    void whenMultiplePermissionsForDifferentAttributesShouldMergeTogether() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), READ_PERMISSION),
            ImmutableMap.of(JsonPointer.valueOf("/defendant"), CREATE_PERMISSION)
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), READ_PERMISSION)
            .containsEntry(JsonPointer.valueOf("/defendant"), CREATE_PERMISSION);
    }

    @Test
    void whenDuplicatePermissionsForDifferentAttributesShouldMergeTogether() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), READ_PERMISSION),
            ImmutableMap.of(JsonPointer.valueOf("/defendant"), READ_PERMISSION)
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), READ_PERMISSION)
            .containsEntry(JsonPointer.valueOf("/defendant"), READ_PERMISSION);
    }

    @Test
    void whenParentAndChildAttributeShouldMergePermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), READ_PERMISSION),
            ImmutableMap.of(JsonPointer.valueOf("/claimant/name"), CREATE_PERMISSION)
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), READ_PERMISSION)
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenParentAndChildAttributeAreNotCloseShouldMergePermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), READ_PERMISSION),
            ImmutableMap.of(JsonPointer.valueOf("/claimant/address/city"), CREATE_PERMISSION)
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), READ_PERMISSION)
            .containsEntry(JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenMultiplePermissionsForSameAttributeShouldPropagateMergedPermissionsToChildren() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), READ_PERMISSION,
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(UPDATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), CREATE_PERMISSION,
                JsonPointer.valueOf("/claimant/name"), DELETE_PERMISSION
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE, READ, UPDATE, DELETE));
    }

    @Test
    void whenRootAttributeIsUsedShouldPropagatePermissionsToAllChildren() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf(""), READ_PERMISSION),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), CREATE_PERMISSION,
                JsonPointer.valueOf("/claimant/name"), DELETE_PERMISSION
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf(""), READ_PERMISSION)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE, READ, DELETE));
    }
}
