package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelationshipPinningTests {
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
    public void setUp() {
        searchResults = new SearchResults();
        optimizer = new SearchResultHotspotOptimizer();
    }

    @Test
    void singleAssets() {
         checkSingleAsset(Partner, 0, 1, 1);
    }

    private void checkSingleAsset(AssetVendorRelationshipLevel relationshipLevel,
                                  int expectedNumShowcase,
                                  int expectedNumFold,
                                  int expectedNumHighValue) {
        //
        setUp();
        var vendor = makeVendor(relationshipLevel);
        var asset = givenAssetInResultsWithVendor(vendor);
        var expected = new HashMap<HotspotKey, Integer>();
        expected.put(Showcase, expectedNumShowcase);
        expected.put(Fold, expectedNumFold);
        expected.put(HighValue, expectedNumHighValue);
        expected.put(TopPicks, 0);
        // ACT
        whenOptimize();
        // ASSERT
        expected.forEach((key, num) -> timesInHotspot(num, key, asset));
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

    private List<Asset> makeConsecutiveAssetsWithVendor(AssetVendor vendor) {
        List<Asset> result = new ArrayList<>();

        for (int i = 0; i < NUM_PARTNER_CONSECUTIVE_ASSETS; i++) {
            result.add(givenAssetInResultsWithVendor(vendor));
        }
        return result;
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        Asset asset = makeAssetWithVendor(vendor);
        searchResults.addFound(asset);
        return asset;
    }

    private Asset givenWellSoldAssetInResults(AssetVendor vendor) {
        Asset asset = makeWellSoldAsset(vendor);
        searchResults.addFound(asset);
        return asset;
    }
}
