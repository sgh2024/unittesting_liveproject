package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.Deals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DealsPinningTests {
    private SearchResults searchResults;
    private SearchResultHotspotOptimizer optimizer;


    private static Asset makeAssetWithRevenueAndRoyalties(AssetVendor vendor,
                                                          double revenue, double royalties) {
        String string = "any";
        URI uri = URI.create(string);
        Money revenueMoney = new Money(new BigDecimal(revenue));
        Money royaltiesMoney = new Money(new BigDecimal(royalties));
        AssetPurchaseInfo info = new AssetPurchaseInfo(1, 1, revenueMoney, royaltiesMoney);
        return new Asset(string, string, uri, uri, info, info, null, vendor);
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
    void singlePartnerToDealsHotspot() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Partner}, 1000, 0, false, new int[]{1});
    }

    @Test
    void singleGoldNotToDealsHotspot() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Gold}, 1000, 700, false, new int[]{0});
    }
    @Test
    void singleGoldToDealsHotspot() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Gold}, 1000, 0, false, new int[]{1});
    }

    @Test
    void singleSilverToDealsHotspot() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Silver}, 10000, 5000, false, new int[]{1});
    }

    @Test
    void singleSilverNotToDealsHotspot() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Silver}, 1000, 0, false, new int[]{0});
    }

    @Test
    void singleSilverToDealsHotspotWithDealEligibility() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Silver}, 1500, 800, true, new int[]{1});
    }

    @Test
    void doubleSilverToDealsHotspot() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Silver, Silver}, 100000, 0, true, new int[]{2, 2});
    }

    @Test
    void partnerAndGoldToDealsHotspot() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Partner, Gold}, 100000, 0, true, new int[]{1, 1});
    }

    @Test
    void partnerAndSilverToDealsHotspot() {
        executeDealsTest(new AssetVendorRelationshipLevel[]{Partner, Silver}, 10000, 0, true, new int[]{1, 1});
    }

    private void setAssessments(AssetAssessments assessments) {
        optimizer.setAssessments(assessments);
    }

    private void executeDealsTest(AssetVendorRelationshipLevel[] relationshipLevel,
                                  int revenue,
                                  int royalties,
                                  boolean assetsAreValid,
                                  int[] expectedTimesInHotspot) {
        // ARRANGE
        var assets = new ArrayList<Asset>();
        for (var level : relationshipLevel) {
            var vendor = makeVendor(level);
            assets.add(givenAssetInResultsWithRevenueAndRoyalties(vendor, revenue, royalties));
        }
        setAssessments(a -> assetsAreValid);
        // ACT
        whenOptimize();
        // ASSERT
        for (int i=0; i<assets.size(); i++) {
            timesInHotspot(expectedTimesInHotspot[i], Deals, assets.get(i));
        }
    }

    private void timesInHotspot(int expected, HotspotKey hotspotKey, Asset asset) {
        assertEquals(expected, searchResults.getHotspot(hotspotKey).getMembers().stream()
            .filter(asset::equals).count());
    }

    private void whenOptimize() {
        optimizer.optimize(searchResults);
    }

    private Asset givenAssetInResultsWithRevenueAndRoyalties(AssetVendor vendor,
                                                             double revenue, double royalties) {
        Asset asset = makeAssetWithRevenueAndRoyalties(vendor, revenue, royalties);
        searchResults.addFound(asset);
        return asset;
    }
}
