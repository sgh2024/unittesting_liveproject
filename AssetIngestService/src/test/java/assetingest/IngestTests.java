package assetingest;

import assetmetadataregistry.*;
import assetregistry.*;
import assetsubjectsregistry.*;
import ext.subjects.detection.*;
import imagescaler.*;
import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.awt.*;
import java.util.List;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IngestTests {
    private AssetRepository assets;
    private AssetMetadataRepository metadata;
    private AssetSubjectsRepository subjects;
    private IngestEngine ingestEngine;
    private ImageScaler scaler;
    private String assetId;
    private Session session;
    private FakeAssetStore assetStore;
    private FakeAssetOrigins assetOrigins;
    private HttpServiceEndpoint subjectsEndpoint;
    private PricingService pricingService;

    @BeforeEach
    public void beforeEach() throws Throwable {
        assets = mock(AssetRepository.class);
        metadata = mock(AssetMetadataRepository.class);
        subjects = mock(AssetSubjectsRepository.class);
        scaler = mock(ImageScaler.class);
        assetStore = new FakeAssetStore();
        assetOrigins = new FakeAssetOrigins();
        subjectsEndpoint = mock(HttpServiceEndpoint.class);
        pricingService = mock(PricingService.class);
        ingestEngine = new IngestEngine(assets, metadata, subjects, scaler, assetStore, assetOrigins, new SubjectDetectionAPI(subjectsEndpoint), pricingService);
        assetId = UUID.randomUUID().toString();
        session = mock(Session.class);
        when(assets.provisionAsset(eq(session), any())).thenReturn(assetId);
        when(session.beginTransaction()).thenAnswer(invocation -> mock(Transaction.class));
        when(subjectsEndpoint.execute(anyString(), anyString(), anyString())).thenReturn("{\"Subjects\":[]}");
    }

    @Test
    public void idComesFromProvisioning() {
        final var fullResolutionUrl = "full-resolution.jpg";

        final var descriptor = new IngestDescriptor();
        descriptor.setFullResolutionUrl(fullResolutionUrl);
        final var newAssetId = "a new id";
        final var assetUriCaptor = ArgumentCaptor.forClass(String.class);
        when(assets.provisionAsset(eq(session), assetUriCaptor.capture())).thenReturn(newAssetId);

        final var id = ingestEngine.addAsset(session, descriptor);

        assertEquals(newAssetId, id);
        assertSame(assetOrigins.loadImage(fullResolutionUrl), getCapturedImage(assetUriCaptor).image);
    }

    @Test
    public void createsThumbnails() throws ImageScalerImplementation.Exception {
        final var fullResolutionUrl = "full-resolution.jpg";
        final var primaryImage = assetOrigins.loadImage(fullResolutionUrl);
        final var thumb64 = setupDownscale(primaryImage, 64);
        final var thumb128 = setupDownscale(primaryImage, 128);
        final var thumb256 = setupDownscale(primaryImage, 256);
        final var thumb512 = setupDownscale(primaryImage, 512);

        final var descriptor = new IngestDescriptor();
        descriptor.setFullResolutionUrl(fullResolutionUrl);

        final var id = ingestEngine.addAsset(session, descriptor);

        final var thumb64Capture = ArgumentCaptor.forClass(String.class);
        final var thumb128Capture = ArgumentCaptor.forClass(String.class);
        final var thumb256Capture = ArgumentCaptor.forClass(String.class);
        final var thumb512Capture = ArgumentCaptor.forClass(String.class);
        verify(metadata).setThumbs(eq(session), eq(id), thumb64Capture.capture(), thumb128Capture.capture(), thumb256Capture.capture(), thumb512Capture.capture());
        assertSame(thumb64, getCapturedImage(thumb64Capture).image);
        assertSame(thumb128, getCapturedImage(thumb128Capture).image);
        assertSame(thumb256, getCapturedImage(thumb256Capture).image);
        assertSame(thumb512, getCapturedImage(thumb512Capture).image);
    }

    @Test
    public void initialIngestionSetsEnglishText() {
        final var title = "an english title";
        final var description = "an english description";
        final var fullResolutionUrl = "full-resolution.jpg";

        final var descriptor = new IngestDescriptor();
        final IngestDescriptor.LanguageTextDescriptor text = getTextDescriptor(title, description);
        descriptor.getText().setEnglish(text);
        descriptor.setFullResolutionUrl(fullResolutionUrl);

        ingestEngine.addAsset(session, descriptor);

        verify(metadata).setTitles(session, assetId, title, null, null, null);
        verify(metadata).setDescriptions(session, assetId, description, null, null, null);
    }

    @Test
    public void initialIngestionSetsGermanText() {
        final var title = "an english title";
        final var description = "an english description";
        final var fullResolutionUrl = "full-resolution.jpg";

        final var descriptor = new IngestDescriptor();
        final IngestDescriptor.LanguageTextDescriptor german = getTextDescriptor(title, description);
        descriptor.getText().setGerman(german);
        descriptor.setFullResolutionUrl(fullResolutionUrl);

        ingestEngine.addAsset(session, descriptor);

        verify(metadata).setTitles(session, assetId, null, title, null, null);
        verify(metadata).setDescriptions(session, assetId, null, description, null, null);
    }

    @Test
    public void initialIngestionSetsSpanishText() {
        final var title = "an english title";
        final var description = "an english description";
        final var fullResolutionUrl = "full-resolution.jpg";

        final var descriptor = new IngestDescriptor();
        final IngestDescriptor.LanguageTextDescriptor spanish = getTextDescriptor(title, description);
        descriptor.getText().setSpanish(spanish);
        descriptor.setFullResolutionUrl(fullResolutionUrl);

        ingestEngine.addAsset(session, descriptor);

        verify(metadata).setTitles(session, assetId, null, null, title, null);
        verify(metadata).setDescriptions(session, assetId, null, null, description, null);
    }

    @Test
    public void initialIngestionSetsFrenchText() {
        final var title = "an english title";
        final var description = "an english description";
        final var fullResolutionUrl = "full-resolution.jpg";

        final var descriptor = new IngestDescriptor();
        descriptor.getText().setFrench(getTextDescriptor(title, description));
        descriptor.setFullResolutionUrl(fullResolutionUrl);

        ingestEngine.addAsset(session, descriptor);

        verify(metadata).setTitles(session, assetId, null, null, null, title);
        verify(metadata).setDescriptions(session, assetId, null, null, null, description);
    }

    @Test
    public void addsSubjectsForAsset() throws Throwable {
        final var enTitle = "title 1";
        final var enDescription = "description 1";
        final var frTitle = "title 2";
        final var frDescription = "description 2";
        final var esTitle = "title 3";
        final var esDescription = "description 3";
        final var deTitle = "title 4";
        final var deDescription = "description 4";
        when(subjectsEndpoint.execute("POST", "subjects/scan-service",
                String.join("\n", enTitle, enDescription, frTitle, frDescription, esTitle, esDescription, deTitle, deDescription)))
                .thenReturn("{\"Subjects\":[{\"Id\":\"sub-constr-bridges\",\"Title\":\"Bridges\"}]}");
        final var descriptor = new IngestDescriptor();
        descriptor.getText().setEnglish(getTextDescriptor(enTitle, enDescription));
        descriptor.getText().setFrench(getTextDescriptor(frTitle, frDescription));
        descriptor.getText().setSpanish(getTextDescriptor(esTitle, esDescription));
        descriptor.getText().setGerman(getTextDescriptor(deTitle, deDescription));

        ingestEngine.addAsset(session, descriptor);

        final var subjectsCaptor = ArgumentCaptor.forClass(SubjectRecord.class);
        verify(subjects, times(1)).setSubjects(same(session), eq(assetId), subjectsCaptor.capture());
        final var subject = subjectsCaptor.getValue();
        assertEquals("sub-constr-bridges", subject.getId());
        assertEquals("Bridges", subject.getTitle());
    }

    @Test
    public void toleratesGenericErrorFromSubjectService() throws Throwable {
        final var enTitle = "title 1";
        final var enDescription = "description 1";
        when(subjectsEndpoint.execute("POST", "subjects/scan-service",
                String.join("\n", enTitle, enDescription)))
                .thenThrow(new HttpErrorException(500));
        final var descriptor = new IngestDescriptor();
        descriptor.getText().setEnglish(getTextDescriptor(enTitle, enDescription));

        assertDoesNotThrow(() -> ingestEngine.addAsset(session, descriptor));
    }

    @Test
    public void rateLimitingIsNotTreatedSpecially() throws Throwable {
        final var enTitle = "title 1";
        final var enDescription = "description 1";
        when(subjectsEndpoint.execute("POST", "subjects/scan-service",
                String.join("\n", enTitle, enDescription)))
                .thenThrow(new HttpErrorException(429));
        final var descriptor = new IngestDescriptor();
        descriptor.getText().setEnglish(getTextDescriptor(enTitle, enDescription));

        assertDoesNotThrow(() -> ingestEngine.addAsset(session, descriptor));
    }

    @Test
    public void ifThereIsAPricingCodeItIsPassedToThePricingService() throws Throwable {
        final var descriptor = new IngestDescriptor();
        descriptor.getPricing().setInitialPricingCode("some-pricing-code");

        final var id = ingestEngine.addAsset(session, descriptor);

        verify(pricingService, times(1)).setPricingPolicy(id, "some-pricing-code");
    }

    @Test
    public void setsDefaultPricingCodeIfNoneWasSpecified() throws Throwable {
        final var descriptor = new IngestDescriptor();

        final var id = ingestEngine.addAsset(session, descriptor);

        verify(pricingService, times(1)).setPricingPolicy(id, "default");
    }

    @Test
    public void canHandleALackOfText() {
        final var descriptor = new IngestDescriptor();
        descriptor.setText(null);

        assertDoesNotThrow(() -> ingestEngine.addAsset(session, descriptor));
    }

    @Test
    public void canHandleALackOfPricing() {
        final var descriptor = new IngestDescriptor();
        descriptor.setPricing(null);

        assertDoesNotThrow(() -> ingestEngine.addAsset(session, descriptor));
    }

    private IngestDescriptor.LanguageTextDescriptor getTextDescriptor(String title, String description) {
        final var text = new IngestDescriptor.LanguageTextDescriptor();
        text.setTitle(title);
        text.setDescription(description);
        return text;
    }

    private Image setupDownscale(Image primaryImage, int height) throws ImageScalerImplementation.Exception {
        final var scaleSpec = new ImageScaleSpec();
        scaleSpec.setTargetHeight(height);
        final var result = mock(Image.class);
        when(scaler.scale(primaryImage, scaleSpec)).thenReturn(result);

        return result;
    }

    private FakeAssetStoreEntry getCapturedImage(ArgumentCaptor<String> assetUriCaptor) {
        return assetStore.findByUri(assetUriCaptor.getValue());
    }

    private static class FakeAssetStore implements AssetStore {
        private final List<FakeAssetStoreEntry> entries = new ArrayList<>();

        @Override
        public AssetStoreEntry allocateEntry() {
            final var result = new FakeAssetStoreEntry();
            entries.add(result);

            return result;
        }

        public FakeAssetStoreEntry findByUri(String uri) {
            for (var entry : entries) {
                if (uri.equals(entry.getUri())) {
                    return entry;
                }
            }

            Assertions.fail("Not found: " + uri);
            return null;
        }
    }

    private static class FakeAssetStoreEntry implements AssetStoreEntry {
        private final String uri = UUID.randomUUID().toString();
        private Image image;

        @Override
        public String getUri() {
            return uri;
        }

        @Override
        public void saveImage(Image image) {
            this.image = image;
        }
    }

    private static class FakeAssetOrigins implements AssetOrigins {
        private final Map<String, Image> images = new HashMap<>();

        @Override
        public Image loadImage(String uri) {
            if (!images.containsKey(uri)) {
                images.put(uri, mock(Image.class));
            }

            return images.get(uri);
        }

    }
}
