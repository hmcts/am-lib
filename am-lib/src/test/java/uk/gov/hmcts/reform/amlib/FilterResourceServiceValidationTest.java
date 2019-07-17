package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.internal.validation.ValidAttributeSecurityClassification;
import uk.gov.hmcts.reform.amlib.models.Resource;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.ValidationMessageRegexFactory.expectedValidationMessagesRegex;

@SuppressWarnings("PMD.LinguisticNaming")
public class FilterResourceServiceValidationTest {

    private final FilterResourceService service = new FilterResourceService("", "", "");

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void filterResourceMethodShouldRejectInvalidArguments(
        String userId, Set<String> userRoles, Resource resource, @ValidAttributeSecurityClassification
        Map<JsonPointer, SecurityClassification> attributeSecurityClassifications) {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.filterResource(userId, userRoles, resource, attributeSecurityClassifications))
            .withMessageMatching(expectedValidationMessagesRegex(
                "userId - must not be blank",
                "userRoles - must not be empty",
                "userRoles\\[\\].<iterable element> - must not be blank",
                "resource - must not be null",
                "resource.id - must not be blank",
                "resource.definition - must not be null",
                "resource.definition.serviceName - must not be blank",
                "resource.definition.resourceType - must not be blank",
                "resource.definition.resourceName - must not be blank",
                "resource.data - must not be null",
                "attributeSecurityClassifications - must not be empty",
                "attributeSecurityClassifications - must contain root attribute"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void getAccessRightsForResourceMethodShouldRejectInvalidArguments(
        String resourceId, String resourceName, String resourceType) {

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.returnResourceAccessors(resourceId, resourceName, resourceType))
            .withMessageMatching(expectedValidationMessagesRegex(
                "resourceId - must not be blank",
                "resourceName - must not be blank",
                "resourceType - must not be blank"
            ));
    }
}
