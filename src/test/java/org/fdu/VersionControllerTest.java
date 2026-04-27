package org.fdu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.assertj.RestTestClientResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
public class VersionControllerTest {
    @Test
    @DisplayName("Testing index version loads properly.")
    void testIndexPageLoads(@Autowired RestTestClient restClient) {
        RestTestClient.ResponseSpec spec = restClient.get().uri("/api/version").exchange();
        RestTestClientResponse response = RestTestClientResponse.from(spec);
        assertThat(response).hasStatusOk();
    }
}