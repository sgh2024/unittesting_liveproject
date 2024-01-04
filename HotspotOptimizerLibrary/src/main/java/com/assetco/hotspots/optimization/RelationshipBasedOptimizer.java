package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;

import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;

class RelationshipBasedOptimizer {
    public void optimize(SearchResults searchResults) {
        Iterator<Asset> iterator = searchResults.getFound().iterator();
        var showcaseFull = searchResults.getHotspot(Showcase).getMembers().size() > 0;
        List<Asset> showcaseAssets = new ArrayList<>();
        // We need to "remember" the assets per partner
        var showcaseAssetsByPartner = new HashMap<AssetVendor, List<Asset>>();
        var partnerAssets = new ArrayList<Asset>();
        var goldAssets = new ArrayList<Asset>();
        var silverAssets = new ArrayList<Asset>();

        while (iterator.hasNext()) {
            Asset asset = iterator.next();
            if (asset.getVendor().getRelationshipLevel() == Gold)
                goldAssets.add(asset);
            else if (asset.getVendor().getRelationshipLevel() == Silver)
                silverAssets.add(asset);

            if (asset.getVendor().getRelationshipLevel() != Partner)
                continue;

            partnerAssets.add(asset);

            // if we don't have a showcase, get one for this partner
            if (showcaseAssets.size() == 0)
                showcaseAssets = getShowcaseAssetsForPartner(showcaseAssetsByPartner, asset.getVendor());

            if (showcaseAssets.size() >= 5) {
                if (Objects.equals(showcaseAssets.get(0).getVendor(), asset.getVendor()))
                    searchResults.getHotspot(TopPicks).addMember(asset);
            } else {
                if (showcaseAssets.size() != 0)
                    if (!Objects.equals(showcaseAssets.get(0).getVendor(), asset.getVendor()))
                        if (showcaseAssets.size() < 3)
                            // instead of clearing the current showcase, just switch to one for the new target partner
                            showcaseAssets = getShowcaseAssetsForPartner(showcaseAssetsByPartner, asset.getVendor());

                if (showcaseAssets.size() == 0 || Objects.equals(showcaseAssets.get(0).getVendor(), asset.getVendor()))
                    showcaseAssets.add(asset);
            }
        }

        var highValueHotspot = searchResults.getHotspot(HighValue);
        for (var asset : partnerAssets)
            if (!highValueHotspot.getMembers().contains(asset))
                highValueHotspot.addMember(asset);

        for (var asset : partnerAssets)
            searchResults.getHotspot(Fold).addMember(asset);

        if (!showcaseFull && showcaseAssets.size() >= 3) {
            Hotspot showcaseHotspot = searchResults.getHotspot(Showcase);
            for (Asset asset : showcaseAssets)
                showcaseHotspot.addMember(asset);
        }

        for (var asset : goldAssets)
            if (!highValueHotspot.getMembers().contains(asset))
                highValueHotspot.addMember(asset);

        for (var asset : goldAssets)
            searchResults.getHotspot(Fold).addMember(asset);

        for (var asset : silverAssets)
            searchResults.getHotspot(Fold).addMember(asset);
    }

    private List<Asset> getShowcaseAssetsForPartner(Map<AssetVendor, List<Asset>> map, AssetVendor vendor) {
        if (map.containsKey(vendor))
            return map.get(vendor);
        var result = new ArrayList<Asset>();
        map.put(vendor, result);
        return result;
    }
}
