package assetingest;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.client.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class PricingServiceTests {
    private RestOperations restOperations;
    private PricingService service;
    private String baseUrl;

    @BeforeEach
    public void setup() {
        baseUrl = "http://any-url.com:9090";
        restOperations = mock(RestOperations.class);
        service = new PricingServiceImplementation(baseUrl, restOperations);
    }

    @Test
    public void adaptsToRestOperation() {
        final var id = UUID.randomUUID().toString();
        final var policy = UUID.randomUUID().toString();

        service.setPricingPolicy(id, policy);

        final var requestCaptor = ArgumentCaptor.forClass(PricingUpdateRequest.class);
        verify(restOperations, times(1)).put(
                eq(baseUrl + "/assets/" + id + "/pricing-schedules"),
                requestCaptor.capture());
        assertThat(requestCaptor.getValue().getPricingCode(), equalTo(policy));
    }
}
