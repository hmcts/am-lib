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

class PermissionsServiceTest {

    private final PermissionsService permissionsService = new PermissionsService();

    @Test
    void whenMultiplePermissionsForSameAttributeShouldMergeTogether() {
        JsonPointer attribute = JsonPointer.valueOf("");

        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(attribute, ImmutableSet.of(READ)),
            ImmutableMap.of(attribute, ImmutableSet.of(CREATE))
        );

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(1)
            .containsEntry(attribute, ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenDuplicatePermissionsForSameAttributeShouldMergeTogether() {
        JsonPointer attribute = JsonPointer.valueOf("");

        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(attribute, ImmutableSet.of(READ)),
            ImmutableMap.of(attribute, ImmutableSet.of(READ))
        );

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(1)
            .containsEntry(attribute, ImmutableSet.of(READ));
    }

    @Test
    void whenMultiplePermissionsForDifferentAttributesShouldMergeTogether() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ)),
            ImmutableMap.of(JsonPointer.valueOf("/defendant"), ImmutableSet.of(CREATE))
        );

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/defendant"), ImmutableSet.of(CREATE));
    }

    @Test
    void whenDuplicatePermissionsForDifferentAttributesShouldMergeTogether() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ)),
            ImmutableMap.of(JsonPointer.valueOf("/defendant"), ImmutableSet.of(READ))
        );

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/defendant"), ImmutableSet.of(READ));
    }

    @Test
    void whenParentAndChildAttributeShouldMergePermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ)),
            ImmutableMap.of(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE))
        );

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenParentAndChildAttributeAreNotCloseShouldMergePermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ)),
            ImmutableMap.of(JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(CREATE))
        );

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenMultiplePermissionsForSameAttributeShouldPropagateMergedPermissionsToChildren() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ),
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(UPDATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE, READ, UPDATE, DELETE));
    }

    @Test
    void whenRootAttributeIsUsedShouldPropagatePermissionsToAllChildren() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE, READ, DELETE));
    }
}
