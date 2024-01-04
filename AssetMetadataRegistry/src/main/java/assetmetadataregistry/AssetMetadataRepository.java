package assetmetadataregistry;

import org.hibernate.*;

import java.util.*;

public interface AssetMetadataRepository {
    void setTitles(Session session, String assetId, String newEnglishTitle, String newGermanTitle, String newSpanishTitle, String newFrenchTitle);

    void setDescriptions(Session session,String assetId, String newEnglishDescription, String newGermanDescription, String newSpanishDescription, String newFrenchDescription);

    void setThumbs(Session session,String assetId, String thumb64, String thumb128, String thumb256, String thumb512);

    List<AssetMetadataRecord> search(Session session, String searchTerm);
}
