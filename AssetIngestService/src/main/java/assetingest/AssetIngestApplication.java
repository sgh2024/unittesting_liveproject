package assetingest;

import assetmetadataregistry.*;
import assetregistry.*;
import assetsubjectsregistry.*;
import ext.subjects.detection.*;
import imagescaler.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;

@SpringBootApplication
public class AssetIngestApplication {
    @Bean
    public IngestEngine createEngine(
            AssetRepository assetRepository,
            AssetMetadataRepository assetMetadataRepository,
            AssetSubjectsRepository assetSubjectsRepository,
            ImageScaler scaler,
            AssetStore assetStore,
            AssetOrigins assetOrigins,
            SubjectDetectionAPI subjectDetectionAPI,
            PricingService pricingService) {
        return new IngestEngine(
                assetRepository,
                assetMetadataRepository,
                assetSubjectsRepository,
                scaler,
                assetStore,
                assetOrigins,
                subjectDetectionAPI,
                pricingService);
    }
}

