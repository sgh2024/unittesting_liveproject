package assetsubjectsregistry;

import org.hibernate.*;

public interface AssetSubjectsRepository {
    void setSubjects(Session session, String assetId, SubjectRecord... subjects);
}
