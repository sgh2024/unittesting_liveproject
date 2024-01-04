package assetpricing;

import org.hibernate.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AssetPricingController {
    private final PricingEngine engine;
    private final SessionFactory sessionFactory;

    @Autowired
    public AssetPricingController(
            SessionFactory sessionFactory,
            PricingEngine engine) {
        this.sessionFactory = sessionFactory;
        this.engine = engine;
    }

    @PutMapping(path = "/assets/{assetId}/pricing-schedules", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<Object> startIngesting(
            @PathVariable String assetId,
            @RequestBody AssetPricingUpdateDescriptor request) {
        request.setAssetId(assetId);
        try (var session = sessionFactory.openSession()) {
            if (!engine.process(session, request))
                return ResponseEntity.badRequest().build();

            return ResponseEntity.ok().build();
        }
    }
}
