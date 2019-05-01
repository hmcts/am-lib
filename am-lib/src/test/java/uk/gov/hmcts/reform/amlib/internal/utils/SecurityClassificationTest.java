package uk.gov.hmcts.reform.amlib.internal.utils;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.NONE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.RESTRICTED;

@SuppressWarnings({"PMD.UnusedPrivateMethod", "LineLength", "PMD.ArrayIsStoredDirectly"})
class SecurityClassificationTest {

    @ParameterizedTest
    @MethodSource("visibleArguments")
    void whenRoleSecurityClassificationIsHighEnoughResourceShouldBeVisible(boolean expectedResult,
                                                                           SecurityClassification roleClassification,
                                                                           SecurityClassification resourceClassification) {
        boolean isVisible = resourceClassification.isVisible(roleClassification.getHierarchy());
        assertThat(isVisible).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("getVisibleSecurityClassificationsArguments")
    void fromValueOfShouldReturnSetOfSecurityClassifications(SecurityClassification securityClassification,
                                                             Set<SecurityClassification> expectedSecurityClassifications) {
        Set<SecurityClassification> securityClassifications =
            SecurityClassifications.getVisibleSecurityClassifications(securityClassification.getHierarchy());

        assertThat(securityClassifications).isEqualTo(expectedSecurityClassifications);
    }

    private static Stream<Arguments> visibleArguments() {
        return Stream.of(
            Arguments.of(true, PUBLIC, NONE),
            Arguments.of(true, PUBLIC, PUBLIC),
            Arguments.of(false, PUBLIC, PRIVATE),
            Arguments.of(false, PUBLIC, RESTRICTED),
            Arguments.of(true, PRIVATE, NONE),
            Arguments.of(true, PRIVATE, PUBLIC),
            Arguments.of(true, PRIVATE, PRIVATE),
            Arguments.of(false, PRIVATE, RESTRICTED),
            Arguments.of(true, RESTRICTED, NONE),
            Arguments.of(true, RESTRICTED, PUBLIC),
            Arguments.of(true, RESTRICTED, PRIVATE),
            Arguments.of(true, RESTRICTED, RESTRICTED),
            Arguments.of(false, NONE, PUBLIC),
            Arguments.of(false, NONE, PRIVATE),
            Arguments.of(false, NONE, RESTRICTED)
        );
    }

    private static Stream<Arguments> getVisibleSecurityClassificationsArguments() {
        return Stream.of(
            Arguments.of(NONE, ImmutableSet.of(NONE)),
            Arguments.of(PUBLIC, ImmutableSet.of(NONE, PUBLIC)),
            Arguments.of(PRIVATE, ImmutableSet.of(NONE, PUBLIC, PRIVATE)),
            Arguments.of(RESTRICTED, ImmutableSet.of(NONE, PUBLIC, PRIVATE, RESTRICTED))
        );
    }
}
