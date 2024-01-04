package assetmetadataregistry;

import org.hibernate.*;

public class AssetMetadataRepositoryFactory {
    public static AssetMetadataRepositoryImplementation createAssetMetadataRepository() {
        return new AssetMetadataRepositoryImplementation();
    }
}
