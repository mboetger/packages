// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import static org.mockito.Mockito.mock;

import com.google.android.gms.maps.model.TileOverlay;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import org.junit.Test;
import org.mockito.Mockito;

public class HeatmapControllerTest {

  @Test
  public void controller_DoesNotCallSetOpacityWhenUnchanged() {
    final HeatmapTileProvider heatmap = mock(HeatmapTileProvider.class);
    final TileOverlay tileOverlay = mock(TileOverlay.class);
    final HeatmapController controller = new HeatmapController(heatmap, tileOverlay);

    controller.setOpacity(0.5);
    Mockito.verify(heatmap, Mockito.times(1)).setOpacity(0.5);
    controller.clearTileCache();
    Mockito.verify(tileOverlay, Mockito.times(1)).clearTileCache();

    controller.setOpacity(0.5);
    Mockito.verify(heatmap, Mockito.times(1)).setOpacity(0.5);
    controller.clearTileCache();
    Mockito.verify(tileOverlay, Mockito.times(1)).clearTileCache();

    controller.setOpacity(0.8);
    Mockito.verify(heatmap, Mockito.times(1)).setOpacity(0.8);
    controller.clearTileCache();
    Mockito.verify(tileOverlay, Mockito.times(2)).clearTileCache();
  }
}
