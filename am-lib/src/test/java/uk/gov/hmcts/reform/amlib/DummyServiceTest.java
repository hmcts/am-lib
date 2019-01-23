package uk.gov.hmcts.reform.amlib;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyServiceTest {

    @Test
    public void dummyTest() {
        String msg = new DummyService(null, null, null).getHello();
        assertThat(msg).isEqualTo("db url or user or pass is null");
    }
}
