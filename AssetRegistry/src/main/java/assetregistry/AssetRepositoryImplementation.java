package assetregistry;

import org.hibernate.*;

import java.util.*;

public class AssetRepositoryImplementation implements AssetRepository {
    @Override
    public String provisionAsset(Session session, String fullResolutionUrl) {
        final var id = UUID.randomUUID().toString();
        final var asset = new AssetRecord();
        asset.setId(id);
        asset.setFullUrl(fullResolutionUrl);
        asset.setTitleEnglish("Coming soon...");
        asset.setTitleGerman("Demnächst...");
        asset.setTitleFrench("À venir...");
        asset.setTitleSpanish("Próximamente...");
        session.saveOrUpdate(asset);

        return id;
    }
}
