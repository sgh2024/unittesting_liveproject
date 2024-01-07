package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BugsTests {
    private static final int NUM_PARTNER_CONSECUTIVE_ASSETS = 4;
    private SearchResults searchResults;
    private SearchResultHotspotOptimizer optimizer;

    private static Asset makeAssetWithVendor(AssetVendor vendor) {
        String string = "any";
        URI uri = URI.create(string);
        Money money = new Money(BigDecimal.ZERO);
        AssetPurchaseInfo info = new AssetPurchaseInfo(1, 1, money, money);
        List<AssetTopic> topics = new ArrayList<>();
        return new Asset(string, string, uri, uri, info, info, topics, vendor);
    }

    private static Asset makeAssetWithTopics(AssetVendor vendor,
                                             AssetTopic... topics) {
        String string = "any";
        URI uri = URI.create(string);
        Money money = new Money(BigDecimal.ZERO);
        AssetPurchaseInfo info = new AssetPurchaseInfo(1, 1, money, money);
        return new Asset(string, string, uri, uri, info, info, Arrays.asList(topics), vendor);
    }

    private static AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        String string = "any";
        return new AssetVendor(string, string, relationshipLevel, 1);
    }

    @BeforeEach
    public void Setup() {
        searchResults = new SearchResults();
        optimizer = new SearchResultHotspotOptimizer();
    }

    @Test
    void precedingPartnerWithLongTrailingAssetsDoesWin() {
        // ARRANGE
        AssetVendor partnerVendorInShowcase = makeVendor(Partner);
        AssetVendor partnerVendorNotInShowcase = makeVendor(Partner);
        List<Asset> expected = new ArrayList<>();
        expected.add(givenAssetInResultsWithVendor(partnerVendorInShowcase));
        givenAssetInResultsWithVendor(partnerVendorNotInShowcase);
        expected.addAll(makeConsecutiveAssetsWithVendor(partnerVendorInShowcase));
        // ACT
        whenOptimize();
        // ASSERT
        thenHotspotHasExactly(Showcase, expected);
    }

    @Test
    void notAllLowPriorityHotTopicsAreHighlighted() {
        // ARRANGE
        var highPriorityTopic = new AssetTopic("0", "0");
        var lowPriorityTopic = new AssetTopic("1", "1");
        var vendor = makeVendor(Basic);
        var expected = new ArrayList<>(makeConsecutiveAssetsWithTopics(1, vendor, lowPriorityTopic));
        expected.addAll(makeConsecutiveAssetsWithTopics(3, vendor, highPriorityTopic));
        var missing = makeConsecutiveAssetsWithTopics(1, vendor, lowPriorityTopic);
        setHotTopics(highPriorityTopic, lowPriorityTopic);
        // ACT
        whenOptimize();
        // ASSERT
        thenHotspotContains(Highlight, expected);
        thenHotspotDoesNotContain(Highlight, missing);
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey,
                                       List<Asset> expected) {
        var hotspotMembers = searchResults.getHotspot(hotspotKey).getMembers().toArray();
        var expectedMembers = expected.toArray();
        Assertions.assertArrayEquals(expectedMembers, hotspotMembers);
    }

    private void thenHotspotContains(HotspotKey hotspotKey,
                                     List<Asset> expected) {
        var hotspotMembers = searchResults.getHotspot(hotspotKey).getMembers();
        assertTrue(hotspotMembers.containsAll(expected));
    }

    private void thenHotspotDoesNotContain(HotspotKey hotspotKey,
                                           List<Asset> expected) {
        var hotspotMembers = searchResults.getHotspot(hotspotKey).getMembers();
        assertFalse(expected.stream().anyMatch(hotspotMembers::contains));
    }

    private void whenOptimize() {
        optimizer.optimize(searchResults);
    }

    private void setHotTopics(AssetTopic... topics) {
        optimizer.setHotTopics(() -> Arrays.asList(topics));
    }

    private List<Asset> makeConsecutiveAssetsWithVendor(AssetVendor vendor) {
        List<Asset> result = new ArrayList<>();

        for (int i = 0; i < NUM_PARTNER_CONSECUTIVE_ASSETS; i++) {
            result.add(givenAssetInResultsWithVendor(vendor));
        }
        return result;
    }

    private List<Asset> makeConsecutiveAssetsWithTopics(int num,
                                                        AssetVendor vendor,
                                                        AssetTopic... topics) {
        List<Asset> result = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            result.add(givenAssetInResultsWithTopics(vendor, topics));
        }
        return result;
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        Asset asset = makeAssetWithVendor(vendor);
        searchResults.addFound(asset);
        return asset;
    }

    private Asset givenAssetInResultsWithTopics(AssetVendor vendor,
                                                AssetTopic... topics) {
        Asset asset = makeAssetWithTopics(vendor, topics);
        searchResults.addFound(asset);
        return asset;
    }
}
