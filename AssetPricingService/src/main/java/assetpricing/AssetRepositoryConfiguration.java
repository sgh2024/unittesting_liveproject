package assetpricing;

import assetpricingregistry.*;
import org.springframework.context.annotation.*;

@Configuration
public class AssetRepositoryConfiguration {
    @Bean
    public AssetPricingRepository createAssetRepository() {
        return new AssetPricingRepositoryImplementation();
    }
}
