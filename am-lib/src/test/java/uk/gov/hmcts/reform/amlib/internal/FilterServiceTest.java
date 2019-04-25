package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.FilterServiceTest.Resource.Address;
import uk.gov.hmcts.reform.amlib.internal.FilterServiceTest.Resource.Claimant;
import uk.gov.hmcts.reform.amlib.internal.FilterServiceTest.Resource.Defendant;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
class FilterServiceTest {

    private final ObjectMapper mapper = new ObjectMapper()
        .setSerializationInclusion(Include.NON_NULL);
    private final FilterService fs = new FilterService();

    @Test
    void itShouldBePossibleToSHowEverything() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        JsonNode returnedJson = fs.filterJson(inputJson, createPermissions("", ImmutableSet.of(READ)));

        assertThat(returnedJson).isEqualTo(inputJson);
    }

    @Test
    void itShouldBePossibleToShowEverythingExceptTopLevelValue() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(READ),
            JsonPointer.valueOf("/amount"), ImmutableSet.of(CREATE));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .age(21)
                    .address(Address.builder()
                        .city("London")
                        .postcode("SE1")
                        .build())
                    .build()
                )
                .defendant(Defendant.builder()
                    .name("Marry")
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build())
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowEverythingExceptTopLevelObject() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(READ),
            JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .defendant(Defendant.builder()
                    .name("Marry")
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build())
                .amount(100)
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowEverythingExceptLeafLevelValue() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(READ),
            JsonPointer.valueOf("/claimant/age"), ImmutableSet.of(CREATE));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .address(Address.builder()
                        .city("London")
                        .postcode("SE1")
                        .build())
                    .build()
                )
                .defendant(Defendant.builder()
                    .name("Marry")
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build())
                .amount(100)
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowEverythingExceptLeafLevelObject() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(READ),
            JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .age(21)
                    .build()
                )
                .defendant(Defendant.builder()
                    .name("Marry")
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build())
                .amount(100)
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowTopLevelValueOnly() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/amount"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .amount(100)
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowTopLevelObjectOnly() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .age(21)
                    .address(Address.builder()
                        .city("London")
                        .postcode("SE1")
                        .build())
                    .build())
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowLeafLevelValueOnly() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/claimant/age"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .age(21)
                    .build()
                )
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowLeafLevelObjectOnly() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .address(Address.builder()
                        .city("London")
                        .postcode("SE1")
                        .build())
                    .build()
                )
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowManyTopLevelObjects() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/claimant"), ImmutableSet.of(READ),
            JsonPointer.valueOf("/defendant"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .age(21)
                    .address(Address.builder()
                        .city("London")
                        .postcode("SE1")
                        .build())
                    .build()
                )
                .defendant(Defendant.builder()
                    .name("Marry")
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build()
                )
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowManyLeafLevelObjectsFromSameParent() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(READ),
            JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .address(Address.builder()
                        .city("London")
                        .postcode("SE1")
                        .build())
                    .build()
                )
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowManyLeafLevelObjectsFromDifferentParents() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(READ),
            JsonPointer.valueOf("/defendant/address"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .address(Address.builder()
                        .city("London")
                        .postcode("SE1")
                        .build())
                    .build()
                )
                .defendant(Defendant.builder()
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build()
                )
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowEverythingExceptTopLevelObjectWithLeafLevelException() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(READ),
            JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
            JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .address(Address.builder()
                        .city("London")
                        .build())
                    .build()
                )
                .defendant(Defendant.builder()
                    .name("Marry")
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build()
                )
                .amount(100)
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowEverythingExceptLeafLevelObjectWithChildException() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(READ),
            JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE),
            JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .age(21)
                    .address(Address.builder()
                        .city("London")
                        .build())
                    .build()
                )
                .defendant(Defendant.builder()
                    .name("Marry")
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build()
                )
                .amount(100)
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToHideEverything() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        JsonNode returnedJson = fs.filterJson(inputJson, createPermissions("/name", ImmutableSet.of(CREATE)));

        assertThat(returnedJson).isNull();
    }

    @Test
    void itShouldBePossibleToHideEverythingByDenyingAllPermissions() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.<JsonPointer, Set<Permission>>builder()
            .put(JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/claimant/age"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/claimant/address"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/claimant/address/city"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/claimant/address/postcode"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/defendant"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/defendant/name"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/defendant/address"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/defendant/address/city"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/defendant/address/postcode"), ImmutableSet.of(CREATE))
            .put(JsonPointer.valueOf("/amount"), ImmutableSet.of(CREATE))
            .build();

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isNull();
    }

    @Test
    void itShouldBePossibleToShowLeafLevelValueOnlyWhenParentIsForbidden() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/claimant"), ImmutableSet.of(CREATE),
            JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(READ));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .build()
                )
                .build()
            )
        );
    }

    @Test
    void itShouldBePossibleToShowLeafLevelValueOnlyWhenSiblingIsForbidden() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/claimant/name"), ImmutableSet.of(READ),
            JsonPointer.valueOf("/claimant/age"), ImmutableSet.of(CREATE));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .build()
                )
                .build()
            )
        );
    }

    @Test
    void itShouldReturnEmptyNodeWhenFieldsDoNotExist() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf("/version"), ImmutableSet.of(READ),
            JsonPointer.valueOf("/claimant/id"), ImmutableSet.of(READ),
            JsonPointer.valueOf("/defendant/mobile"), ImmutableSet.of(CREATE),
            JsonPointer.valueOf("/updated"), ImmutableSet.of(CREATE));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(JsonNodeFactory.instance.objectNode());
    }

    @Test
    void itShouldNotLeakAttributeValueWhenAccessIsGrantedToAnotherAttributeThatStartsSameWay() throws IOException {
        JsonNode inputJson = mapper.readTree(ClassLoader.getSystemResource("FilterServiceResources/input.json"));

        Map<JsonPointer, Set<Permission>> attributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(READ),
            JsonPointer.valueOf("/amountInPounds"), ImmutableSet.of(READ),
            JsonPointer.valueOf("/amount"), ImmutableSet.of(CREATE));

        JsonNode returnedJson = fs.filterJson(inputJson, attributePermissions);

        assertThat(returnedJson).isEqualTo(mapper.valueToTree(
            Resource.builder()
                .claimant(Claimant.builder()
                    .name("John")
                    .age(21)
                    .address(Address.builder()
                        .city("London")
                        .postcode("SE1")
                        .build())
                    .build()
                )
                .defendant(Defendant.builder()
                    .name("Marry")
                    .address(Address.builder()
                        .city("Swansea")
                        .postcode("SA1")
                        .build())
                    .build()
                )
                .build()
            )
        );
    }

    @Builder
    @Data
    static class Resource {
        private final Claimant claimant;
        private final Defendant defendant;
        private final Integer amount;

        @Data
        @Builder
        static class Claimant {
            private final String name;
            private final Integer age;
            private final Address address;
        }

        @Data
        @Builder
        static class Defendant {
            private final String name;
            private final Address address;
        }

        @Data
        @Builder
        static class Address {
            private final String city;
            private final String postcode;
        }
    }
}
