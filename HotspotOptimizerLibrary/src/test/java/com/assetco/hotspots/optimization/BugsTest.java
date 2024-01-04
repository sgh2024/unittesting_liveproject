package com.assetco.hotspots.optimization;

import com.assetco.search.results.Asset;
import com.assetco.search.results.AssetVendor;
import com.assetco.search.results.AssetVendorRelationshipLevel;
import com.assetco.search.results.HotspotKey;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

class BugsTest
{

  private static final int NUM_PARTNER_CONSECUTIVE_ASSETS = 4;

  @Test
  void precedingPartnerWithLongTrailingAssetsDoesNotWin()
  {
    // ARRANGE
    AssetVendor partnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
    Asset missing = givenAssetInResultsWithVendor(partnerVendor);
    AssetVendor otherPartnerVendor = makeVendor(AssetVendorRelationshipLevel.Partner);
    Asset otherDisrupting = givenAssetInResultsWithVendor(partnerVendor);
    List<Asset> expected = makeConsecutiveAssets(partnerVendor);
    // ACT
    whenOptimize();
    // ASSERT
    thenHotspotDoesNotHave(HotspotKey.Showcase, missing);
    thenHotspotHasExactly(HotspotKey.Showcase, expected);
  }

  private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expected)
  {

  }

  private void thenHotspotDoesNotHave(HotspotKey hotspotKey, Asset... missing)
  {

  }

  private void whenOptimize()
  {

  }

  private List<Asset> makeConsecutiveAssets(AssetVendor partnerVendor)
  {
    List<Asset> result = new ArrayList<>();

    for (int i = 0; i < NUM_PARTNER_CONSECUTIVE_ASSETS; i++) {
      result.add(givenAssetInResultsWithVendor(partnerVendor));
    }
    return result;
  }

  private Asset givenAssetInResultsWithVendor(AssetVendor partnerVendor)
  {
    return null;
  }

  private AssetVendor makeVendor(AssetVendorRelationshipLevel assetVendorRelationshipLevel)
  {
    return null;
  }
}
