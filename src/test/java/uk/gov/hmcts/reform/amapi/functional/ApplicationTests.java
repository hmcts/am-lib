package uk.gov.hmcts.reform.amapi.functional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("PMD")
class ApplicationTests {

    @Test
    void sample_test() {
        assertThat(true).isTrue();
    }

}
