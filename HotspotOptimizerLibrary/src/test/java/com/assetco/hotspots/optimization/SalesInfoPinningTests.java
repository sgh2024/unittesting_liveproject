package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;

import static com.assetco.search.results.HotspotKey.HighValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesInfoPinningTests {
    private SearchResults searchResults;
    private SearchResultHotspotOptimizer optimizer;

    private static Asset makeAsset(AssetVendor vendor,
                                   int timesShown24h, int timesPurchased24h,
                                   double revenue30d, double royalties30d,
                                   int timesShown30d, int timesPurchased30d) {
        String string = "any";
        var uri = URI.create(string);
        var info24h = makeAssetPurchaseInfo(0.0, 0.0, timesShown24h, timesPurchased24h);
        var info30d = makeAssetPurchaseInfo(revenue30d, royalties30d, timesShown30d, timesPurchased30d);
        return new Asset(string, string, uri, uri, info30d, info24h, null, vendor);
    }

    private static AssetPurchaseInfo makeAssetPurchaseInfo(double revenue,
                                                           double royalties,
                                                           int timesShown,
                                                           int timesPurchased) {
        Money totalRevenue = new Money(new BigDecimal(revenue));
        Money totalRoyalties = new Money(new BigDecimal(royalties));
        return new AssetPurchaseInfo(timesShown, timesPurchased, totalRevenue, totalRoyalties);
    }

    private static AssetVendor makeVendor() {
        String string = "any";
        return new AssetVendor(string, string, AssetVendorRelationshipLevel.Basic, 1);
    }

    @BeforeEach
    public void setUp() {
        searchResults = new SearchResults();
        optimizer = new SearchResultHotspotOptimizer();
    }

    @Test
    void checkHighValueSpots() {
        executeDealsTest(1, 0, 0, 0.0, 0.0, 0, 0, 0);
        executeDealsTest(1, 1000, 4, 0.0, 0.0, 0, 0, 0);
        executeDealsTest(1, 999, 5, 0.0, 0.0, 0, 0, 0);
        executeDealsTest(1, 1000, 5, 0.0, 0.0, 0, 0, 1);
        executeDealsTest(1, 10000, 100, 0.0, 0.0, 0, 0, 1);
        executeDealsTest(1, 0, 0, 0.0, 0.0, 49999, 400, 0);
        executeDealsTest(1, 0, 0, 0.0, 0.0, 50000, 399, 0);
        executeDealsTest(1, 0, 0, 0.0, 0.0, 50000, 400, 1);
        executeDealsTest(1, 0, 0, 0.0, 0.0, 500000, 8000, 1);
        executeDealsTest(1, 0, 0, 4999.00, 999.0, 0, 0, 0);
        executeDealsTest(1, 0, 0, 5000.00, 1001.0, 0, 0, 0);
        executeDealsTest(1, 0, 0, 5000.00, 1000.0, 0, 0, 1);
        executeDealsTest(1, 1000, 5, 5000.00, 1000.0, 50000, 400, 1);
        executeDealsTest(2, 1000, 5, 5000.00, 1000.0, 50000, 400, 1);
    }

    private void executeDealsTest(int numAssets,
                                  int timesShown24h, int timesPurchased24h,
                                  double revenue30d, double royalties30d,
                                  int timesShown30d, int timesPurchased30d,
                                  int expectedTimesInHighValuePerAsset) {
        // ARRANGE
        setUp();
        var assets = new ArrayList<Asset>();
        for (int i = 0; i < numAssets; i++) {
            var vendor = makeVendor();
            assets.add(givenAssetInResults(vendor,
                timesShown24h, timesPurchased24h,
                revenue30d, royalties30d, timesShown30d, timesPurchased30d));
        }
        // ACT
        whenOptimize();
        // ASSERT
        for (Asset asset : assets) {
            timesInHighValue(expectedTimesInHighValuePerAsset, asset);
        }
    }

    private void timesInHighValue(int expected, Asset asset) {
        assertEquals(expected, searchResults.getHotspot(HighValue).getMembers().stream()
            .filter(asset::equals).count());
    }

    private void whenOptimize() {
        optimizer.optimize(searchResults);
    }

    private Asset givenAssetInResults(AssetVendor vendor,
                                      int timesShown24h, int timesPurchased24h,
                                      double revenue30d, double royalties30d,
                                      int timesShown30d, int timesPurchased30d) {
        Asset asset = makeAsset(vendor,
            timesShown24h, timesPurchased24h,
            revenue30d, royalties30d, timesShown30d, timesPurchased30d);
        searchResults.addFound(asset);
        return asset;
    }
}
