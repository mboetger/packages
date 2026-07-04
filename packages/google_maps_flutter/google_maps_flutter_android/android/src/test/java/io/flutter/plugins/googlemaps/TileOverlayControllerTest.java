// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import static org.mockito.Mockito.mock;

import com.google.android.gms.maps.model.TileOverlay;
import org.junit.Test;
import org.mockito.Mockito;

public class TileOverlayControllerTest {

  @Test
  public void controller_DoesNotCallSetTransparencyWhenUnchanged() {
    final TileOverlay tileOverlay = mock(TileOverlay.class);
    final TileOverlayController controller = new TileOverlayController(tileOverlay);

    controller.setTransparency(0.5f);
    Mockito.verify(tileOverlay, Mockito.times(1)).setTransparency(0.5f);

    controller.setTransparency(0.5f);
    Mockito.verify(tileOverlay, Mockito.times(1)).setTransparency(0.5f);

    controller.setTransparency(0.8f);
    Mockito.verify(tileOverlay, Mockito.times(1)).setTransparency(0.8f);
  }
}
