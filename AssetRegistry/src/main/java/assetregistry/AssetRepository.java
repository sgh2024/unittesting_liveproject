package assetregistry;

import org.hibernate.*;

public interface AssetRepository {
    String provisionAsset(Session session, String fullResolutionUrl);
}
