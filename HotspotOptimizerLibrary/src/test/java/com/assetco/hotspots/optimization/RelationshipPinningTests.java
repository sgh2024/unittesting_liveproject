package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;

class RelationshipPinningTests {
    private SearchResults searchResults;
    private SearchResultHotspotOptimizer optimizer;
    private int uniqueIdCounter;

    private static Asset makeAssetWithTopics(AssetVendor vendor,
                                             AssetTopic... topics) {
        String string = "any";
        URI uri = URI.create(string);
        Money money = new Money(BigDecimal.ZERO);
        AssetPurchaseInfo info = new AssetPurchaseInfo(1, 1, money, money);
        return new Asset(string, string, uri, uri, info, info, Arrays.asList(topics), vendor);
    }

    private int getNextUniqueId() {
        return uniqueIdCounter++;
    }

    private Asset makeAssetWithVendor(AssetVendor vendor) {
        String string = String.valueOf(getNextUniqueId());
        URI uri = URI.create(string);
        Money money = new Money(BigDecimal.ZERO);
        AssetPurchaseInfo info = new AssetPurchaseInfo(1, 1, money, money);
        List<AssetTopic> topics = new ArrayList<>();
        return new Asset(string, string, uri, uri, info, info, topics, vendor);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        String string = String.valueOf(getNextUniqueId());
        return new AssetVendor(string, string, relationshipLevel, 1);
    }

    @BeforeEach
    public void setUp() {
        searchResults = new SearchResults();
        optimizer = new SearchResultHotspotOptimizer();
        uniqueIdCounter = 0;
    }

    @Test
    void singleVendorPerCase() {
        var result = new ArrayList<ArrayList<TreeMap<Asset, TreeMap<HotspotKey, Long>>>>();
        result.add(actOnNumTimesAssetOfLevel(Partner, 2, false));
        result.add(actOnNumTimesAssetOfLevel(Partner, 3, false));
        result.add(actOnNumTimesAssetOfLevel(Partner, 4, true));
        result.add(actOnNumTimesAssetOfLevel(Partner, 5, false));
        result.add(actOnNumTimesAssetOfLevel(Partner, 6, false));
        result.add(actOnNumTimesAssetOfLevel(Partner, 6, true));
        result.add(actOnNumTimesAssetOfLevel(Basic, 5, false));
        result.add(actOnNumTimesAssetOfLevel(Silver, 5, false));
        result.add(actOnNumTimesAssetOfLevel(Gold, 5, false));
        Approvals.verifyAll("", result, this::listToString);
    }

    private String entryToString(TreeMap<Asset, TreeMap<HotspotKey, Long>> entry, String leadingSpaces) {
        StringBuilder result = new StringBuilder();
        for (var e : entry.entrySet()) {
            result.append(String.format("%s (Asset: %s, Vendor: %s, RelationshipLevel: %s ) -> (%s)\n",
                leadingSpaces,
                e.getKey().getId(),
                e.getKey().getVendor().getId(),
                e.getKey().getVendor().getRelationshipLevel(),
                e.getValue()));
        }
        return result.toString();
    }

    private String listToString(ArrayList<TreeMap<Asset, TreeMap<HotspotKey, Long>>> list) {
        StringBuilder result = new StringBuilder();
        result.append("Case {\n");
        for (var e : list) {
            result.append(entryToString(e, " "));
        }
        result.append("}\n");
        return result.toString();
    }

    private ArrayList<TreeMap<Asset, TreeMap<HotspotKey, Long>>> actOnNumTimesAssetOfLevel(AssetVendorRelationshipLevel relationshipLevel, int num, boolean prefillShowcaseHotspot) {
        // ARRANGE
        setUp();
        var vendor = makeVendor(relationshipLevel);
        var assets = new ArrayList<Asset>();
        for (int i = 0; i < num; i++) {
            assets.add(givenAssetInResultsWithVendor(vendor));
        }
        if (prefillShowcaseHotspot) {
            var highPriorityTopic = new AssetTopic("0", "0");
            var partnerVendor = makeVendor(Partner);
            givenAssetInResultsWithTopics(partnerVendor, highPriorityTopic);
            optimizer.setHotTopics(() -> List.of(highPriorityTopic));
        }
        // ACT
        whenOptimize();
        // TO ASSERT
        var result = new ArrayList<TreeMap<Asset, TreeMap<HotspotKey, Long>>>();
        for (int i = 0; i < num; i++) {
            var resultEntry = new TreeMap<Asset, TreeMap<HotspotKey, Long>>((lhs, rhs) -> lhs.getId().toString().compareTo(rhs.getId().toString()));
            resultEntry.put(assets.get(i), getCountInHotspots(assets.get(i)));
            result.add(resultEntry);
        }
        return result;
    }

    private TreeMap<HotspotKey, Long> getCountInHotspots(Asset asset) {
        var result = new TreeMap<HotspotKey, Long>();
        for (var key : HotspotKey.values()) {
            result.put(key, searchResults.getHotspot(key).getMembers().stream()
                .filter(asset::equals)
                .count());
        }
        return result;
    }

    private void whenOptimize() {
        optimizer.optimize(searchResults);
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
