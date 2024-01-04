package assetsubjectsregistry;

import org.hibernate.*;

import java.util.*;

public final class AssetSubjectsRepositoryImplementation implements AssetSubjectsRepository {
    @Override
    public void setSubjects(Session session, String assetId, SubjectRecord... subjects) {
        final var transaction = session.beginTransaction();

        final var asset = getAssetForId(session, assetId);
        final HashSet<SubjectRecord> newSubjects = getAssociatedSubjects(session, subjects);

        asset.setSubjects(newSubjects);

        session.saveOrUpdate(asset);
        transaction.commit();
    }

    private AssetSubjectsRecord getAssetForId(Session session, String assetId) {
        final var existingAsset = session.find(AssetSubjectsRecord.class, assetId);
        if (existingAsset != null)
            return existingAsset;

        var newAsset = new AssetSubjectsRecord();
        newAsset.setId(assetId);
        return newAsset;
    }

    private HashSet<SubjectRecord> getAssociatedSubjects(Session session, SubjectRecord[] subjects) {
        final var result = new HashSet<SubjectRecord>();
        for (var subject : subjects) {
            var existingSubject = session.get(SubjectRecord.class, subject.getId());
            if (existingSubject != null) {
                existingSubject.setTitle(subject.getTitle());
                session.update(existingSubject);
                result.add(existingSubject);
            } else {
                session.save(subject);
                result.add(subject);
            }
        }
        return result;
    }
}
