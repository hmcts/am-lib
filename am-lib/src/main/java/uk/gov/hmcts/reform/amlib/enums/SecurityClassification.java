package uk.gov.hmcts.reform.amlib.enums;

import java.util.Arrays;

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

    public static SecurityClassification fromHierarchy(int heirarchy) {
        for (SecurityClassification securityClassification : values()) {
            if (securityClassification.getHierarchy() == heirarchy) {
                return securityClassification;
            }
        }
        return null;
    }
}
