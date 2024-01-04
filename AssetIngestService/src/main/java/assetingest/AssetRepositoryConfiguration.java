package assetingest;

import assetmetadataregistry.*;
import assetregistry.*;
import assetsubjectsregistry.*;
import org.springframework.context.annotation.*;

@Configuration
public class AssetRepositoryConfiguration {
    @Bean
    public AssetRepository createAssetRepository() {
        return new AssetRepositoryImplementation();
    }

    @Bean
    public AssetMetadataRepository createAssetMetadataRepository() {
        return AssetMetadataRepositoryFactory.createAssetMetadataRepository();
    }

    @Bean
    public AssetSubjectsRepository createAssetSubjectsRepository() {
        return new AssetSubjectsRepositoryImplementation();
    }
}
