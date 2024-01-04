package assetpricing;

import assetpricingregistry.*;
import org.hibernate.*;

import java.util.*;

public class PricingEngine {
    private final AssetPricingRepository repository;
    private final Time time;
    private final long thirtyDays = 1000L * 60 * 60 * 24 * 30;

    public PricingEngine(AssetPricingRepository repository, Time time) {
        this.repository = repository;
        this.time = time;
    }

    public PricingEngine(AssetPricingRepository repository) {
        this(repository, System::currentTimeMillis);
    }

    public boolean process(Session session, AssetPricingUpdateDescriptor pricingRequest) {
        final var newSchedule = new PriceScheduleRecord();
        newSchedule.setPricingCode(pricingRequest.getPricingCode());
        prepareRequest(pricingRequest);
        newSchedule.setStartDate(pricingRequest.getStart());
        newSchedule.setEndDate(pricingRequest.getEnd());
        newSchedule.setIsLocked(shouldLock(pricingRequest));

        final var assetId = pricingRequest.getAssetId();
        final var oldSchedules = repository.getPriceTable(session, assetId);
        final var newSchedules = new ArrayList<PriceScheduleRecord>();
        for (final var oldSchedule : oldSchedules) {
            if (overlap(newSchedule, oldSchedule)) {
                if (oldSchedule.getIsLocked())
                    return false;

                continue;
            }
            newSchedules.add(oldSchedule);
        }

        newSchedules.add(newSchedule);
        repository.setPriceSchedules(session, assetId, newSchedules);

        return true;
    }

    private void prepareRequest(AssetPricingUpdateDescriptor pricingRequest) {
        if (pricingRequest.getStart() == null) {
            pricingRequest.setStart(time.getCurrentTimeInMilliseconds());
            pricingRequest.setEnd(null);
        }
        if (pricingRequest.getEnd() == null) {
            pricingRequest.setEnd(pricingRequest.getStart() + thirtyDays);
        }
    }

    private boolean shouldLock(AssetPricingUpdateDescriptor pricingRequest) {
        final var lockPolicy = pricingRequest.getLockPolicy();

        if (lockPolicy == null) {
            final var now = time.getCurrentTimeInMilliseconds();
            return pricingRequest.getEnd() > now && pricingRequest.getStart() <= now;
        }

        return "LOCK".equals(lockPolicy);
    }

    private static boolean overlap(PriceScheduleRecord newSchedule, PriceScheduleRecord oldSchedule) {
        final var oldStart = oldSchedule.getStartDate();
        final var oldEnd = oldSchedule.getEndDate();
        final var newStart = newSchedule.getStartDate();
        final var newEnd = newSchedule.getEndDate();

        return oldStart < newEnd && oldEnd > newStart;
    }
}
