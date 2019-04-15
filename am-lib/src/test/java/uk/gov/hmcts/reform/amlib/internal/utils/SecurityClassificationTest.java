package uk.gov.hmcts.reform.amlib.internal.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.NONE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.RESTRICTED;

@SuppressWarnings("PMD.UnusedPrivateMethod")
class SecurityClassificationTest {

    @ParameterizedTest
    @MethodSource("createArguments")
    void test(Arguments args) {
        boolean isVisible = args.resourceClassification.isVisible(args.roleClassification.getHierarchy());
        assertThat(isVisible).isEqualTo(args.expectedResult);
    }

    private static Stream<Arguments> createArguments() {
        return Stream.of(
            new Arguments(true, PUBLIC, NONE),
            new Arguments(true, PUBLIC, PUBLIC),
            new Arguments(false, PUBLIC, PRIVATE),
            new Arguments(false, PUBLIC, RESTRICTED),
            new Arguments(true, PRIVATE, NONE),
            new Arguments(true, PRIVATE, PUBLIC),
            new Arguments(true, PRIVATE, PRIVATE),
            new Arguments(false, PRIVATE, RESTRICTED),
            new Arguments(true, RESTRICTED, NONE),
            new Arguments(true, RESTRICTED, PUBLIC),
            new Arguments(true, RESTRICTED, PRIVATE),
            new Arguments(true, RESTRICTED, RESTRICTED),
            new Arguments(false, NONE, PUBLIC),
            new Arguments(false, NONE, PRIVATE),
            new Arguments(false, NONE, RESTRICTED)
        );
    }

    private static class Arguments {
        private final boolean expectedResult;
        private final SecurityClassification roleClassification;
        private final SecurityClassification resourceClassification;

        private Arguments(boolean expectedResult,
                          SecurityClassification roleClassification,
                          SecurityClassification resourceClassification) {
            this.expectedResult = expectedResult;
            this.roleClassification = roleClassification;
            this.resourceClassification = resourceClassification;
        }
    }
}
