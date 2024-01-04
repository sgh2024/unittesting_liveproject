package assetpricing;

import assetpricingregistry.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;

@SpringBootApplication
public class AssetPricingApplication {
    @Bean
    public PricingEngine createEngine(AssetPricingRepository repository) {
        return new PricingEngine(repository);
    }
}
