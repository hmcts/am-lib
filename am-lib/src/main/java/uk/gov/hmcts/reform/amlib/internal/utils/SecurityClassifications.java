package uk.gov.hmcts.reform.amlib.internal.utils;

import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.EnumSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public final class SecurityClassifications {

    private SecurityClassifications() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    /**
     * Builds a set of security classifications including and below a given hierarchy.
     *
     * @param hierarchy integer value of security classification defined in {@link SecurityClassification}
     * @return a set of security classifications
     */
    public static Set<SecurityClassification> getVisibleSecurityClassifications(int hierarchy) {
        return EnumSet.allOf(SecurityClassification.class)
            .stream()
            .filter(securityClassification ->
                securityClassification.isVisible(hierarchy))
            .collect(toSet());
    }
}
