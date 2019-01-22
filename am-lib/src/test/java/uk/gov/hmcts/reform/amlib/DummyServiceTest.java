package uk.gov.hmcts.reform.amlib;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyServiceTest {

    @Test
    public void dummyTest() {
        assertThat(new DummyService().getHello()).isEqualTo("Hello Dummy Service 2");
    }
}
