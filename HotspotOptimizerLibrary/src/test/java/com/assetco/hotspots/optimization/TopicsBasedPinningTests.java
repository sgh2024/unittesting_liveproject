package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.assetco.search.results.AssetVendorRelationshipLevel.Basic;

class TopicsBasedPinningTests {
    private static final AssetTopic highPriorityTopic = new AssetTopic("high", "high");
    private static final AssetTopic lowPriorityTopic = new AssetTopic("low", "low");
    private static final List<AssetTopic> all = List.of(highPriorityTopic, lowPriorityTopic);
    private static final List<AssetTopic> high = List.of(highPriorityTopic);
    private static final List<AssetTopic> low = List.of(lowPriorityTopic);
    private static final List<AssetTopic> empty = List.of();
    private static final AssetVendor vendor = makeVendor();

    private SearchResults searchResults;
    private SearchResultHotspotOptimizer optimizer;
    private int uniqueIdCounter;

    private static AssetVendor makeVendor() {
        return new AssetVendor("", "", Basic, 1);
    }

    private int getNextUniqueId() {
        return uniqueIdCounter++;
    }

    @BeforeEach
    public void setUp() {
        searchResults = new SearchResults();
        optimizer = new SearchResultHotspotOptimizer();
        uniqueIdCounter = 0;
    }

    @Test
    void variateTopicsAndHotTopics() {
        var result = new ArrayList<>();
        result.add(actOnAssetsWithTopics(empty, empty));
        result.add(actOnAssetsWithTopics(empty, low));
        result.add(actOnAssetsWithTopics(low, low));
        result.add(actOnAssetsWithTopics(all, low, low));
        result.add(actOnAssetsWithTopics(low, high, low, low));
        result.add(actOnAssetsWithTopics(high, high, low, high, all));
        result.add(actOnAssetsWithTopics(all, low, all, high, low, low, high, all));
        result.add(actOnAssetsWithTopics(all, all, high, low, low, low, high, all, high));
        result.add(actOnAssetsWithTopics(high, high, high, all, high, all));
        result.add(actOnAssetsWithTopics(all, high, high, all, high, all, high, high));
        Approvals.verifyAll("Case", result);
    }

    private String topicsToString(List<AssetTopic> topics) {
        return topics.stream()
            .map(AssetTopic::getId).collect(Collectors.joining(", ", "[", "]"));
    }

    private String entryToString(TreeMap<Asset, TreeMap<HotspotKey, Long>> entry) {
        StringBuilder result = new StringBuilder();
        for (var e : entry.entrySet()) {
            result.append(String.format("  (Asset: %s, Topics: %s ) -> (%s)\n",
                e.getKey().getId(),
                topicsToString(e.getKey().getTopics()),
                e.getValue()));
        }
        return result.toString();
    }

    private String resultToString(List<AssetTopic> hotTopics,
                                  ArrayList<TreeMap<Asset, TreeMap<HotspotKey, Long>>> list) {
        StringBuilder result = new StringBuilder();
        result.append("{\n HotTopics: ");
        result.append(topicsToString(hotTopics));
        result.append(",\n");
        for (var e : list) {
            result.append(entryToString(e));
        }
        result.append("}\n");
        return result.toString();
    }

    @SafeVarargs
    private String actOnAssetsWithTopics(List<AssetTopic> hotTopics,
                                         List<AssetTopic>... topicsToCreateAssetsFrom) {
        // ARRANGE
        setUp();
        var assets = makeAssetsFromTopics(topicsToCreateAssetsFrom);
        optimizer.setHotTopics(() -> hotTopics);
        // ACT
        whenOptimize();
        // TO ASSERT
        return resultToString(hotTopics, buildResult(assets));
    }

    private ArrayList<TreeMap<Asset, TreeMap<HotspotKey, Long>>> buildResult(ArrayList<Asset> assets) {
        var result = new ArrayList<TreeMap<Asset, TreeMap<HotspotKey, Long>>>();
        for (var asset : Collections.unmodifiableList(assets)) {
            var resultEntry = new TreeMap<Asset, TreeMap<HotspotKey, Long>>((lhs, rhs) -> lhs.getId().toString().compareTo(rhs.getId().toString()));
            resultEntry.put(asset, getCountInHotspots(asset));
            result.add(resultEntry);
        }
        return result;
    }

    @SafeVarargs
    private ArrayList<Asset> makeAssetsFromTopics(List<AssetTopic>... topicsToCreateAssetsFrom) {
        var assets = new ArrayList<Asset>();
        for (var topics : topicsToCreateAssetsFrom) {
            assets.add(givenAssetInResultsWithTopics(topics));
        }
        return assets;
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

    private Asset makeAssetWithTopics(List<AssetTopic> topics) {
        String string = String.valueOf(getNextUniqueId());
        Money money = new Money(BigDecimal.ZERO);
        AssetPurchaseInfo info = new AssetPurchaseInfo(1, 1, money, money);
        return new Asset(string, string, null, null, info, info, topics, vendor);
    }

    private Asset givenAssetInResultsWithTopics(List<AssetTopic> topics) {
        Asset asset = makeAssetWithTopics(topics);
        searchResults.addFound(asset);
        return asset;
    }

}
