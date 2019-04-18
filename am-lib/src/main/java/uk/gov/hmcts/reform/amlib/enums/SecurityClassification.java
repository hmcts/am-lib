package uk.gov.hmcts.reform.amlib.enums;

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
}
