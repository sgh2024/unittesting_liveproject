package assetpricingregistry;

import org.hibernate.*;

import java.util.*;

public interface AssetPricingRepository {
    void setPriceSchedules(Session session, String id, Collection<PriceScheduleRecord> schedules);

    Collection<PriceScheduleRecord> getPriceTable(Session session, String assetId);
}
