package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static Asset makeWellSoldAsset(AssetVendor vendor) {
        var string = "any";
        var uri = URI.create(string);
        var money = new Money(BigDecimal.ONE);
        var info24hours = new AssetPurchaseInfo(1000, 5, money, money);
        var info30days = new AssetPurchaseInfo(50000, 400, money, money);
        return new Asset(string, string, uri, uri, info30days, info24hours, null, vendor);
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
    void allLowPriorityHotTopicsAreHighlighted() {
        // ARRANGE
        var highPriorityTopic = new AssetTopic("0", "0");
        var lowPriorityTopic = new AssetTopic("1", "1");
        var vendor = makeVendor(Basic);
        var expected = new ArrayList<>(makeConsecutiveAssetsWithTopics(2, vendor, lowPriorityTopic));
        makeConsecutiveAssetsWithTopics(3, vendor, highPriorityTopic);
        expected.addAll(makeConsecutiveAssetsWithTopics(4, vendor, lowPriorityTopic));
        setHotTopics(highPriorityTopic, lowPriorityTopic);
        // ACT
        whenOptimize();
        // ASSERT
        thenHotspotContains(Highlight, expected);
    }

    @Test
    void wellSoldAssetsGetsOnlyOneHighValueSpot() {
        // ARRANGE
        var vendor = makeVendor(Basic);
        var wellSoldAsset = givenWellSoldAssetInResults(vendor);
        // ACT
        whenOptimize();
        // ASSERT
        timesInHotspot(1, HighValue, wellSoldAsset);
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

    private void timesInHotspot(int expected, HotspotKey hotspotKey, Asset wellSoldAsset) {
        assertEquals(expected, searchResults.getHotspot(hotspotKey).getMembers().stream()
            .filter(wellSoldAsset::equals).count());
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

    private Asset givenWellSoldAssetInResults(AssetVendor vendor) {
        Asset asset = makeWellSoldAsset(vendor);
        searchResults.addFound(asset);
        return asset;
    }
}
