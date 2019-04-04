package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.AvoidThrowingRawExceptionTypes"})
public class InvalidArgumentsProvider implements ArgumentsProvider {

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        List<Arguments> combinations = new ArrayList<>();

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

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    private Object[] generateInvalidValues(Type parameterType) {
        if (parameterType instanceof Class) {
            return generateInvalidValues((Class<?>) parameterType);
        } else if (parameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) parameterType;

            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType)) {
                Class<?> actualElementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                if (List.class.isAssignableFrom(rawType)) {
                    return generateInvalidCollections(actualElementType, ImmutableList.of(), ImmutableList::of);
                } else if (Set.class.isAssignableFrom(rawType)) {
                    return generateInvalidCollections(actualElementType, ImmutableSet.of(), ImmutableSet::of);
                }
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }

    private Object[] generateInvalidValues(Class<?> parameterType) {
        if (parameterType.equals(String.class)) {
            return new Object[]{null, "", " "};
        } else if (parameterType.isEnum()) {
            return new Object[]{null};
        } else if (isComplexTypeSupported(parameterType)) {
            Object[] invalidValues = new Object[]{null};
            try {
                Method builderMethod = parameterType.getDeclaredMethod("builder");
                Object builderInstance = builderMethod.invoke(this);

                Method[] setterMethods = builderInstance.getClass().getDeclaredMethods();
                for (Method setterMethod : setterMethods) {
                    if (!setterMethod.getReturnType().equals(builderInstance.getClass())) {
                        continue;
                    }
                    Object invalidValue = generateComplexValue(parameterType, setterMethod.getName());
                    invalidValues = ObjectArrays.concat(invalidValues, invalidValue);
                }
                return invalidValues;
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error occurred while processing type: " + parameterType, e);
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }

    private Object[] generateInvalidCollections(Class<?> invalidValueClass,
                                                Collection<?> emptyCollection,
                                                Function<Object, Collection<Object>> collectionCreationFn) {
        Object[] invalidValues = generateInvalidValues(invalidValueClass);

        return ObjectArrays.concat(
            new Object[]{null, emptyCollection},
            Arrays.stream(invalidValues).filter(Objects::nonNull).map(collectionCreationFn).toArray(),
            Object.class
        );
    }

    private Object generateValidValue(Type parameterType) {
        if (parameterType instanceof Class) {
            return generateValidValue((Class<?>) parameterType);
        } else if (parameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) parameterType;

            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType)) {
                Class<?> actualElementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                if (List.class.isAssignableFrom(rawType)) {
                    return ImmutableList.of(generateValidValue(actualElementType));
                } else if (Set.class.isAssignableFrom(rawType)) {
                    return ImmutableSet.of(generateValidValue(actualElementType));
                }
            } else if (Map.class.isAssignableFrom(rawType)) {
                Type actualKeyType = parameterizedType.getActualTypeArguments()[0];
                Type actualValueType = parameterizedType.getActualTypeArguments()[1];
                return ImmutableMap.of(generateValidValue(actualKeyType), generateValidValue(actualValueType));
            } else if (Map.Entry.class.isAssignableFrom(rawType)) {
                Type actualKeyType = parameterizedType.getActualTypeArguments()[0];
                Type actualValueType = parameterizedType.getActualTypeArguments()[1];
                return new Pair<>(generateValidValue(actualKeyType), generateValidValue(actualValueType));
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }

    private Object generateValidValue(Class<?> parameterType) {
        if (parameterType.equals(String.class)) {
            return "valid string";
        } else if (parameterType.equals(JsonNode.class)) {
            return JsonNodeFactory.instance.objectNode();
        } else if (parameterType.equals(JsonPointer.class)) {
            return JsonPointer.valueOf("");
        } else if (parameterType.isEnum()) {
            return parameterType.getEnumConstants()[0];
        } else if (isComplexTypeSupported(parameterType)) {
            return generateComplexValue(parameterType);
        }
        throw new IllegalArgumentException("Unsupported type: " + parameterType);
    }

    private boolean isComplexTypeSupported(Class<?> parameterType) {
        try {
            parameterType.getDeclaredMethod("builder");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Object generateComplexValue(Class<?> parameterType) {
        return generateComplexValue(parameterType, null);
    }

    private Object generateComplexValue(Class<?> parameterType, String skippedMethodName) {
        try {
            Method builderMethod = parameterType.getDeclaredMethod("builder");
            Object builderInstance = builderMethod.invoke(this);

            Method[] setterMethods = builderInstance.getClass().getDeclaredMethods();
            for (Method setterMethod : setterMethods) {
                if (!setterMethod.getReturnType().equals(builderInstance.getClass())) {
                    continue;
                }
                if (setterMethod.getName().equals(skippedMethodName)) {
                    continue;
                }
                Type setterMethodParameterType = setterMethod.getGenericParameterTypes()[0];
                setterMethod.invoke(builderInstance, generateValidValue(setterMethodParameterType));
            }

            Method buildMethod = builderInstance.getClass().getDeclaredMethod("build");
            return buildMethod.invoke(builderInstance);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while processing type: " + parameterType, e);
        }
    }
}
