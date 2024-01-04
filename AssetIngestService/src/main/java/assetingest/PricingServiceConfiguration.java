package assetingest;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.web.client.*;

import java.util.*;

@Configuration
public class PricingServiceConfiguration {
    @Bean
    public PricingService createPricingService(
            @Value("${assetingest.dependencies.pricing.url:}") String baseUrl
    ) {
        if (baseUrl == null || Objects.equals("", baseUrl.trim()))
            return (ignore1, ignore2) -> {};

        final var restOperations = new RestTemplate();
        return new PricingServiceImplementation(baseUrl, restOperations);
    }
}
