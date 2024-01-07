package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;

class BugsTests {
    private static final int NUM_PARTNER_CONSECUTIVE_ASSETS = 4;
    private SearchResults searchResults;

    private static Asset makeAsset(AssetVendor vendor) {
        String string = "any";
        URI uri = URI.create(string);
        Money money = new Money(BigDecimal.ZERO);
        AssetPurchaseInfo info = new AssetPurchaseInfo(1, 1, money, money);
        List<AssetTopic> topics = new ArrayList<>();
        return new Asset(string, string, uri, uri, info, info, topics, vendor);
    }

    private static AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        String string = "any";
        return new AssetVendor(string, string, relationshipLevel, 1);
    }

    @BeforeEach
    public void Setup() {
        searchResults = new SearchResults();
    }

    @Test
    void precedingPartnerWithLongTrailingAssetsDoesWin() {
        // ARRANGE
        AssetVendor partnerVendorInShowcase = makeVendor(Partner);
        AssetVendor partnerVendorNotInShowcase = makeVendor(Partner);
        List<Asset> expected = new ArrayList<>();
        expected.add(givenAssetInResultsWithVendor(partnerVendorInShowcase));
        givenAssetInResultsWithVendor(partnerVendorNotInShowcase);
        expected.addAll(makeConsecutiveAssets(partnerVendorInShowcase));
        // ACT
        whenOptimize();
        // ASSERT
        thenHotspotHasExactly(Showcase, expected);
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey,
                                       List<Asset> expected) {
        var hotspotMembers = searchResults.getHotspot(hotspotKey).getMembers().toArray();
        var expectedMembers = expected.toArray();
        Assertions.assertArrayEquals(expectedMembers, hotspotMembers);
    }

    private void whenOptimize() {
        SearchResultHotspotOptimizer optimizer = new SearchResultHotspotOptimizer();
        optimizer.optimize(searchResults);
    }

    private List<Asset> makeConsecutiveAssets(AssetVendor vendor) {
        List<Asset> result = new ArrayList<>();

        for (int i = 0; i < NUM_PARTNER_CONSECUTIVE_ASSETS; i++) {
            result.add(givenAssetInResultsWithVendor(vendor));
        }
        return result;
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        Asset asset = makeAsset(vendor);
        searchResults.addFound(asset);
        return asset;
    }
}
