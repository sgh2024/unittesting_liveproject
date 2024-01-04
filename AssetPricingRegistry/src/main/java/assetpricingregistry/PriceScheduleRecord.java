package assetpricingregistry;

import javax.persistence.*;

@Entity
@Table(name = "asset_pricing_schedules")
public class PriceScheduleRecord {
    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "price_code")
    private String pricingCode;

    @Id
    @Column(name = "asset_pricing_schedule_id")
    private String id;

    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "end_date")
    private Long endDate;

    @Column(name = "schedule_committed")
    private boolean isLocked;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getPricingCode() {
        return pricingCode;
    }

    public void setPricingCode(String value) {
        this.pricingCode = value;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean getIsLocked() {
        return isLocked;
    }
}
