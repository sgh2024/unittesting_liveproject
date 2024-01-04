package assetpricing;

public class AssetPricingUpdateDescriptor {
    private String assetId;
    private String pricingCode;
    private String lockPolicy;
    private Long start;
    private Long end;

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setPricingCode(String pricingCode) {
        this.pricingCode = pricingCode;
    }

    public String getPricingCode() {
        return pricingCode;
    }

    public void setLockPolicy(String lockPolicy) {
        this.lockPolicy = lockPolicy;
    }

    public String getLockPolicy() {
        return lockPolicy;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getStart() {
        return start;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getEnd() {
        return end;
    }
}
