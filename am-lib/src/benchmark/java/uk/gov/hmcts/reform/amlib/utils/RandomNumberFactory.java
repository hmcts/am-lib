package uk.gov.hmcts.reform.amlib.utils;

import java.util.Random;

public class RandomNumberFactory {
    private static Random random = new Random();

    private RandomNumberFactory() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static int nextIntegerInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }
        return random.nextInt((max - min) + 1) + min;
    }
}
