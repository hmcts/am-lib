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

@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
class PermissionsServiceTest {

    private final PermissionsService permissionsService = new PermissionsService();

    @Test
    void whenDifferentPermissionsForSameAttributeShouldMergePermissionsForAttribute() {
        JsonPointer attribute = JsonPointer.valueOf("");

        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(attribute, ImmutableSet.of(READ)),
            ImmutableMap.of(attribute, ImmutableSet.of(CREATE))
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(1)
            .containsEntry(attribute, ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenSamePermissionsForSameAttributeShouldMergePermissionsForAttributeWithoutDuplicates() {
        JsonPointer attribute = JsonPointer.valueOf("");

        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(attribute, ImmutableSet.of(READ)),
            ImmutableMap.of(attribute, ImmutableSet.of(READ))
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(1)
            .containsEntry(attribute, ImmutableSet.of(READ));
    }

    @Test
    void whenDifferentPermissionsForDifferentAttributesShouldMergeWithoutChangingAttributePermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ)),
            ImmutableMap.of(JsonPointer.valueOf("/defendant"), ImmutableSet.of(CREATE))
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/defendant"), ImmutableSet.of(CREATE));
    }

    @Test
    void whenSamePermissionsForDifferentAttributesShouldMergeWithoutChangingAttributePermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ)),
            ImmutableMap.of(JsonPointer.valueOf("/defendant"), ImmutableSet.of(READ))
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/defendant"), ImmutableSet.of(READ));
    }

    @Test
    void whenPermissionsForParentAndChildAttributesShouldPropagateParentPermissionsOntoChild() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ)),
            ImmutableMap.of(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE))
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenPermissionsForIndirectAncestorAndChildAttributesShouldPropagateAncestorPermissionsOntoChild() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ)),
            ImmutableMap.of(JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(CREATE))
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(CREATE, READ));
    }

    @Test
    void whenDifferentPermissionsForSameAttributesShouldMergePermissionsForAttributes() {
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

        assertThat(permissionsService.merge(permissions))
            .hasSize(2)
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(UPDATE, DELETE));
    }

    @Test
    void whenOnlyRootAttributeIsUsedInGroupOneShouldPropagatePermissionsOntoAllChildrenDefinedInGroupTwo() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(READ, DELETE));
    }

    @Test
    @SuppressWarnings("LineLength")
    void whenRootAttributeIsUsedInGroupOneAlongsideOtherAttributeShouldOnlyPropagatePermissionsOntoChildrenInGroupTwo() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf(""), ImmutableSet.of(READ),
                JsonPointer.valueOf("/defendant"), ImmutableSet.of(UPDATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(4)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(READ, DELETE))
            .containsEntry(JsonPointer.valueOf("/defendant"), ImmutableSet.of(UPDATE));
    }

    @Test
    @SuppressWarnings("LineLength")
    void whenRootAttributeIsUsedInGroupOneAlongsideAttributeAppearingInBothGroupsShouldOnlyPropagatePermissionsOntoUniqueChildFromGroupTwo() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf(""), ImmutableSet.of(READ),
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(UPDATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, READ))
            .containsEntry(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(UPDATE, DELETE));
    }

    @Test
    @SuppressWarnings("LineLength")
    void whenManyAncestorAttributesDefinedInGroupOneShouldOnlyPropagatePermissionsOntoChildrenInGroupTwoFromClosestAncestorInGroupOne() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf(""), ImmutableSet.of(READ),
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(UPDATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE),
                JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(4)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(UPDATE))
            .containsEntry(JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE, UPDATE))
            .containsEntry(JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(UPDATE, DELETE));
    }

    @Test
    @SuppressWarnings("LineLength")
    void whenDirectParentIsDefinedInBothGroupsShouldOnlyPropagateParentPermissionsDefinedInOtherGroupOntoChild() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf(""), ImmutableSet.of(READ),
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(UPDATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
                JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE, UPDATE))
            .containsEntry(JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(UPDATE, DELETE));
    }

    @Test
    void whenDirectParentIsDefinedInBothGroupsShouldOnlyPropagateParentPermissionsDefinedInOtherGroupsOntoChild() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf(""), ImmutableSet.of(READ),
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(UPDATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(UPDATE, DELETE))
            .containsEntry(JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE, UPDATE, DELETE));
    }

    @Test
    void whenThreeGroupsAreUsedShouldProperlyMergePermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf(""), ImmutableSet.of(READ),
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(UPDATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE)
            ),
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.merge(permissions))
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/claimant"), ImmutableSet.of(UPDATE))
            .containsEntry(JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE, UPDATE, DELETE));
    }

    @Test
    void whenOnlyOneGroupIsUsedShouldReturnUnchangedPermissions() {
        List<Map<JsonPointer, Set<Permission>>> permissions = ImmutableList.of(
            ImmutableMap.of(
                JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
                JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(DELETE)
            )
        );

        assertThat(permissionsService.merge(permissions)).isEqualTo(permissions.get(0));
    }
}
