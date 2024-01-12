package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;

import static com.assetco.search.results.AssetVendorRelationshipLevel.Partner;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PinTests {
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
    void revenue1000GetsSpotInDeals()
    {
        // ARRANGE
        var vendor = makeVendor(Partner);
        var asset = givenAssetInResultsWithRevenue(vendor, 1000, 0);
        // ACT
        whenOptimize();
        // ASSERT
        timesInHotspot(1, Deals, asset);
    }

    private void timesInHotspot(int expected, HotspotKey hotspotKey, Asset wellSoldAsset) {
        assertEquals(expected, searchResults.getHotspot(hotspotKey).getMembers().stream()
            .filter(wellSoldAsset::equals).count());
    }

    private void whenOptimize() {
        optimizer.optimize(searchResults);
    }

    private Asset givenAssetInResultsWithRevenue(AssetVendor vendor,
                                                 double revenue, double royalties) {
        Asset asset = makeAssetWithRevenueAndRoyalties(vendor, revenue, royalties);
        searchResults.addFound(asset);
        return asset;
    }
}
