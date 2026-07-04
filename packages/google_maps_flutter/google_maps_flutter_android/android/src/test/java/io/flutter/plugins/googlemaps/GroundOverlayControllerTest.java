// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import static org.mockito.Mockito.mock;

import com.google.android.gms.maps.model.GroundOverlay;
import org.junit.Test;
import org.mockito.Mockito;

public class GroundOverlayControllerTest {

  @Test
  public void controller_DoesNotCallSetTransparencyWhenUnchanged() {
    final GroundOverlay groundOverlay = mock(GroundOverlay.class);
    final GroundOverlayController controller = new GroundOverlayController(groundOverlay, false);

    controller.setTransparency(0.5f);
    Mockito.verify(groundOverlay, Mockito.times(1)).setTransparency(0.5f);

    controller.setTransparency(0.5f);
    Mockito.verify(groundOverlay, Mockito.times(1)).setTransparency(0.5f);

    controller.setTransparency(0.8f);
    Mockito.verify(groundOverlay, Mockito.times(1)).setTransparency(0.8f);
  }
}
