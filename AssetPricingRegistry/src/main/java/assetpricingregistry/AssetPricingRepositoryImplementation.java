package assetpricingregistry;

import org.hibernate.*;

import java.util.*;

public class AssetPricingRepositoryImplementation implements AssetPricingRepository {
    @Override
    public void setPriceSchedules(Session session, String id, Collection<PriceScheduleRecord> schedules) {
        final var transaction = session.beginTransaction();

        final AssetPricingRecord asset = getAssetForId(session, id);

        asset.setPricingRecords(new HashSet<>(schedules));

        session.saveOrUpdate(asset);

        for (final var schedule : schedules) {
            if (schedule.getId() == null) {
                schedule.setId(UUID.randomUUID().toString());
            }
            session.saveOrUpdate(schedule);
        }

        transaction.commit();
    }

    private AssetPricingRecord getAssetForId(Session session, String id) {
        final var preExistingAsset = session.get(AssetPricingRecord.class, id);
        if (preExistingAsset != null)
            return preExistingAsset;

        final var asset = new AssetPricingRecord();
        asset.setId(id);
        return asset;
    }

    @Override
    public Collection<PriceScheduleRecord> getPriceTable(Session session, String assetId) {
        final var asset = session.find(AssetPricingRecord.class, assetId);

        return new HashSet<>(asset.getPricingRecords());
    }
}
