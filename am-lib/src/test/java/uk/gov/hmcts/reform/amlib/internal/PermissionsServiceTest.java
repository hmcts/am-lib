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
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;


class PermissionsServiceTest {

    private final PermissionsService permissionsService = new PermissionsService();

    @Test
    void whenMultiplePermissionsForSameAttributeShouldMergeTogether() {
        JsonPointer attribute = JsonPointer.valueOf("");

        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(attribute, READ_PERMISSION),
            ImmutableMap.of(attribute, CREATE_PERMISSION)
        );

        assertThat(permissionsService.mergePermissions(permissions))
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

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(1)
            .containsEntry(attribute, ImmutableSet.of(READ));
    }

    @Test
    void whenParentAndChildAttributeShouldMergePermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), READ_PERMISSION),
            ImmutableMap.of(JsonPointer.valueOf("/claimant/name"), CREATE_PERMISSION)
        );

        assertThat(permissionsService.mergePermissions(permissions))
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

        assertThat(permissionsService.mergePermissions(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(CREATE, READ));
    }
}
