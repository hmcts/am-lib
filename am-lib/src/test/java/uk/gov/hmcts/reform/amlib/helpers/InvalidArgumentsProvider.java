package uk.gov.hmcts.reform.amlib.helpers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class InvalidArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        List<Arguments> combinations = new ArrayList<>();

//        Class<?>[] parameterTypes = context.getRequiredTestMethod().getParameterTypes();
        Type[] parameterTypes = context.getRequiredTestMethod().getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Object[] invalidValues = generateInvalidValues(parameterTypes[i]);
            for (Object invalidValue : invalidValues) {
                Object[] arguments = new Object[parameterTypes.length];
                arguments[i] = invalidValue;
                for (int j = 0; j < parameterTypes.length; j++) {
                    if (i == j) {
                        continue;
                    }

                    arguments[j] = generateValidValue(parameterTypes[j]);
                }
                combinations.add(Arguments.of(arguments));
            }
        }

        return combinations.stream();
    }

    private Object[] generateInvalidValues(Type parameterType) {
        if (parameterType instanceof Class) {
            return generateInvalidValues((Class<?>) parameterType);
        } else if (parameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) parameterType;
            if (((Class<?>) parameterizedType.getRawType()).isAssignableFrom(Set.class)) {
                Class<?> actualElementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                Object[] invalidValues = generateInvalidValues(actualElementType);

                return ObjectArrays.concat(
                    new Object[]{null, ImmutableSet.of()},
                    Arrays.stream(invalidValues).filter(Objects::nonNull).map(ImmutableSet::of).toArray(),
                    Object.class
                );
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }

    private Object[] generateInvalidValues(Class<?> parameterType) {
        if (parameterType.equals(String.class)) {
            return new Object[]{null, "", " "};
        } else if (parameterType.isEnum()) {
            return new Object[]{null};
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }

    private Object generateValidValue(Type parameterType) {
        if (parameterType instanceof Class) {
            return generateValidValue((Class<?>) parameterType);
        } else if (parameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) parameterType;
            if (((Class<?>) parameterizedType.getRawType()).isAssignableFrom(Set.class)) {
                Class<?> actualElementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                return ImmutableSet.of(generateValidValue(actualElementType));
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }

    private Object generateValidValue(Class<?> parameterType) {
        if (parameterType.equals(String.class)) {
            return "valid string";
        } else if (parameterType.isEnum()) {
            return parameterType.getEnumConstants()[0];
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }
}
