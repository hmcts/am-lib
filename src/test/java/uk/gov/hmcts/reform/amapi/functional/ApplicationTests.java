package uk.gov.hmcts.reform.amapi.functional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class ApplicationTests {

    @Test
    public void sample_test() {
        assertThat(true).isTrue();
    }

}
