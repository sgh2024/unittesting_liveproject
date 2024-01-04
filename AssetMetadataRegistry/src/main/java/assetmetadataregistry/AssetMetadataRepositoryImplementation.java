package assetmetadataregistry;

import org.hibernate.*;

import java.util.*;

class AssetMetadataRepositoryImplementation implements AssetMetadataRepository {
    @Override
    public void setTitles(Session session, String assetId, String newEnglishTitle, String newGermanTitle, String newSpanishTitle, String newFrenchTitle) {
        final var asset = getAssetById(session, assetId);

        if (newEnglishTitle != null)
            asset.setTitleEnglish(newEnglishTitle);
        if (newGermanTitle != null)
            asset.setTitleGerman(newGermanTitle);
        if (newFrenchTitle != null)
            asset.setTitleFrench(newFrenchTitle);
        if (newSpanishTitle != null)
            asset.setTitleSpanish(newSpanishTitle);

        session.saveOrUpdate(asset);
    }

    @Override
    public void setDescriptions(Session session,String assetId, String newEnglishDescription, String newGermanDescription, String newSpanishDescription, String newFrenchDescription) {
        final var asset = getAssetById(session, assetId);

        if (newEnglishDescription != null)
            asset.setDescriptionEnglish(newEnglishDescription);
        if (newGermanDescription != null)
            asset.setDescriptionGerman(newGermanDescription);
        if (newFrenchDescription != null)
            asset.setDescriptionFrench(newFrenchDescription);
        if (newSpanishDescription != null)
            asset.setDescriptionSpanish(newSpanishDescription);

        session.saveOrUpdate(asset);
    }

    @Override
    public void setThumbs(Session session,String assetId, String thumb64, String thumb128, String thumb256, String thumb512) {
        final var asset = getAssetById(session, assetId);
        asset.setThumb64(thumb64);
        asset.setThumb128(thumb128);
        asset.setThumb256(thumb256);
        asset.setThumb512(thumb512);

        session.saveOrUpdate(asset);
    }

    @Override
    public List<AssetMetadataRecord> search(Session session, String searchTerm) {
        final var query = session.createQuery("select results " +
                "from assets_metadata results " +
                "where titleEnglish like concat('%', :searchTerm, '%') " +
                "or titleFrench like concat('%', :searchTerm, '%') " +
                "or titleGerman like concat('%', :searchTerm, '%') " +
                "or titleSpanish like concat('%', :searchTerm, '%') " +
                "or descriptionEnglish like concat('%', :searchTerm, '%') " +
                "or descriptionFrench like concat('%', :searchTerm, '%') " +
                "or descriptionGerman like concat('%', :searchTerm, '%') " +
                "or descriptionSpanish like concat('%', :searchTerm, '%')", AssetMetadataRecord.class);
        query.setParameter("searchTerm", searchTerm);

        return query.list();
    }

    private AssetMetadataRecord getAssetById(Session session, String assetId) {
        var result = session.get(AssetMetadataRecord.class, assetId);
        if (result != null)
            return result;

        result = new AssetMetadataRecord();
        result.setId(assetId);

        return result;
    }
}
