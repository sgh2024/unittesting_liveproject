package assetpricingregistry;


import javax.persistence.*;
import java.util.*;

@Entity(name = "asset_pricing_asset")
@Table(name = "assets")
public class AssetPricingRecord {
    @Id()
    @Column(name = "asset_id")
    private String id;

    @OneToMany()
    @JoinColumn(name="asset_id")
    private Set<PriceScheduleRecord> pricingRecords;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<PriceScheduleRecord> getPricingRecords() {
        return pricingRecords;
    }

    public void setPricingRecords(Set<PriceScheduleRecord> pricingRecords) {
        this.pricingRecords = pricingRecords;
    }
}
