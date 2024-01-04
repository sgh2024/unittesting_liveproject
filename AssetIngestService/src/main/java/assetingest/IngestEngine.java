package assetingest;

import assetmetadataregistry.AssetMetadataRepository;
import assetregistry.AssetRepository;
import assetsubjectsregistry.*;
import ext.subjects.detection.*;
import imagescaler.ImageScaleSpec;
import imagescaler.ImageScaler;
import org.hibernate.*;

import java.awt.*;
import java.util.*;

public class IngestEngine {
    private final AssetRepository assets;
    private final AssetSubjectsRepository subjects;
    private final ImageScaler scaler;
    private final AssetStore assetStore;
    private final AssetOrigins assetOrigins;
    private final AssetMetadataRepository metadata;
    private final SubjectDetectionAPI subjectsAPI;
    private final PricingService pricingService;

    public IngestEngine(
            AssetRepository assets,
            AssetMetadataRepository metadata,
            AssetSubjectsRepository subjects,
            ImageScaler scaler,
            AssetStore assetStore,
            AssetOrigins assetOrigins,
            SubjectDetectionAPI subjectsAPI,
            PricingService pricingService) {
        this.metadata = metadata;
        this.assets = assets;
        this.subjects = subjects;
        this.scaler = scaler;
        this.assetStore = assetStore;
        this.assetOrigins = assetOrigins;
        this.subjectsAPI = subjectsAPI;
        this.pricingService = pricingService;
    }

    public String addAsset(Session session, IngestDescriptor descriptor) {
        final var primaryImage = assetOrigins.loadImage(descriptor.getFullResolutionUrl());
        final String id = ingestPrimaryImage(session, primaryImage);

        ingestMetadata(session, descriptor, id);

        ingestThumbs(session, primaryImage, id);

        ingestSubjects(session, descriptor, id);

        ingestPricing(descriptor.getPricing(), id);

        return id;
    }

    private void ingestPricing(IngestDescriptor.PricingDescriptor descriptor, String id) {
        final var initialPricingCode = descriptor.getInitialPricingCode();
        pricingService.setPricingPolicy(id, initialPricingCode != null ? initialPricingCode : "default");
    }

    private void ingestSubjects(Session session, IngestDescriptor descriptor, String id) {
        try {
            final var subjectsForThisAsset = subjectsAPI.getSubjectsFromText(getAccumulatedText(descriptor));

            final var newSubjects = new ArrayList<SubjectRecord>();

            for (var subjectMessage : subjectsForThisAsset.results.subjects) {
                final var newSubject = new SubjectRecord();
                newSubject.setId(subjectMessage.id);
                newSubject.setTitle(subjectMessage.title);
                newSubjects.add(newSubject);
            }

            subjects.setSubjects(session, id, newSubjects.toArray(new SubjectRecord[0]));
        } catch (HttpErrorException e) {
            e.printStackTrace();
        }
    }

    private String getAccumulatedText(IngestDescriptor descriptor) {
        final var fragments = new ArrayList<String>();
        for (var text :
                new IngestDescriptor.LanguageTextDescriptor[]{
                        descriptor.getText().getEnglish(),
                        descriptor.getText().getFrench(),
                        descriptor.getText().getSpanish(),
                        descriptor.getText().getGerman()
                }) {
            if (null == text) continue;

            if (null != text.getTitle())
                fragments.add(text.getTitle());
            if (null != text.getDescription())
                fragments.add(text.getDescription());
        }
        return String.join("\n", fragments);
    }

    private void ingestThumbs(Session session, Image primaryImage, String id) {
        var transaction = session.beginTransaction();
        final var thumb64Uri = ingestThumb(primaryImage, 64);
        final var thumb128Uri = ingestThumb(primaryImage, 128);
        final var thumb256Uri = ingestThumb(primaryImage, 256);
        final var thumb512Uri = ingestThumb(primaryImage, 512);

        metadata.setThumbs(session, id, thumb64Uri, thumb128Uri, thumb256Uri, thumb512Uri);

        transaction.commit();
    }

    private String ingestThumb(Image primaryImage, int height) {
        final var spec = new ImageScaleSpec();
        spec.setTargetHeight(height);

        final var entry = assetStore.allocateEntry();
        final Image scaled;
        try {
            scaled = scaler.scale(primaryImage, spec);
        } catch (Exception e) {
            return null;
        }
        entry.saveImage(scaled);

        return entry.getUri();
    }

    private String ingestPrimaryImage(Session session, Image primaryImage) {
        final var transaction = session.beginTransaction();
        final String primaryEntryUri = ingestToPrimaryAssetUri(primaryImage);
        final var id = assets.provisionAsset(session, primaryEntryUri);
        transaction.commit();
        return id;
    }

    private String ingestToPrimaryAssetUri(Image primaryImage) {
        try {
            final var primaryEntry = assetStore.allocateEntry();
            primaryEntry.saveImage(primaryImage);
            return primaryEntry.getUri();
        } catch (Exception ex) {
            System.err.println("Failed to ingest: " + ex);

            return null;
        }
    }

    private void ingestMetadata(Session session, IngestDescriptor descriptor, String id) {
        final var transaction = session.beginTransaction();
        String englishTitle = null;
        String englishDescription = null;
        IngestDescriptor.LanguageTextDescriptor english = descriptor.getText().getEnglish();
        if (english != null) {
            englishTitle = english.getTitle();
            englishDescription = english.getDescription();
        }

        String germanTitle = null;
        String germanDescription = null;
        IngestDescriptor.LanguageTextDescriptor german = descriptor.getText().getGerman();
        if (german != null) {
            germanTitle = german.getTitle();
            germanDescription = german.getDescription();
        }

        String spanishTitle = null;
        String spanishDescription = null;
        IngestDescriptor.LanguageTextDescriptor spanish = descriptor.getText().getSpanish();
        if (spanish != null) {
            spanishTitle = spanish.getTitle();
            spanishDescription = spanish.getDescription();
        }

        String frenchTitle = null;
        String frenchDescription = null;
        IngestDescriptor.LanguageTextDescriptor french = descriptor.getText().getFrench();
        if (french != null) {
            frenchTitle = french.getTitle();
            frenchDescription = french.getDescription();
        }

        metadata.setTitles(session, id, englishTitle, germanTitle, spanishTitle, frenchTitle);
        metadata.setDescriptions(session, id, englishDescription, germanDescription, spanishDescription, frenchDescription);

        transaction.commit();
    }
}
