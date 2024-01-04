package assetingest;

import org.springframework.web.client.*;

public class PricingServiceImplementation implements PricingService{
    private final String baseUrl;
    private final RestOperations restOperations;

    public PricingServiceImplementation(String baseUrl, RestOperations restOperations) {

        this.baseUrl = baseUrl.replace("/*$", "") + "/";
        this.restOperations = restOperations;
    }

    @Override
    public void setPricingPolicy(String assetId, String pricingPolicyCode) {
        final var request = new PricingUpdateRequest();
        request.setPricingCode(pricingPolicyCode);
        try {
            restOperations.put(baseUrl + "assets/" + assetId + "/pricing-schedules", request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
