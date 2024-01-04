package assetingest;

import org.hibernate.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.*;

@RestController
public class AssetIngestController {
    private final IngestEngine engine;
    private final SessionFactory sessionFactory;

    @Autowired
    public AssetIngestController(
            SessionFactory sessionFactory,
            IngestEngine engine) {
        this.sessionFactory = sessionFactory;
        this.engine = engine;
    }

    @PostMapping(path = "/assets", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> startIngesting(@RequestBody IngestDescriptor request) {
        final String id;
        try (var session = sessionFactory.openSession()) {
            id = engine.addAsset(session, request);
        }

        final var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/assets/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }
}
