package uk.gov.hmcts.reform.amlib.enums;

import java.util.Arrays;
import java.util.NoSuchElementException;

public enum SecurityClassification {
    NONE(0),
    PUBLIC(1),
    PRIVATE(2),
    RESTRICTED(3);

    private int hierarchy;

    SecurityClassification(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    public int getHierarchy() {
        return hierarchy;
    }

    public boolean isVisible(int maxHierarchy) {
        return maxHierarchy >= this.getHierarchy();
    }

    /**
     * Gives a SecurityClassification enum corresponding to a given hierarchy.
     *
     * @param hierarchy
     * @return SecurityClassification
     */
    public static SecurityClassification valueOf(int hierarchy) {
        return Arrays.stream(values())
            .filter(securityClassification -> securityClassification.hierarchy == hierarchy)
            .findFirst()
            .orElseThrow(NoSuchElementException::new);
    }
}
