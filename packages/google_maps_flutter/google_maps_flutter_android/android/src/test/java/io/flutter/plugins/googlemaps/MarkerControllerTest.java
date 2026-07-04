// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import static org.mockito.Mockito.mock;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import org.junit.Test;
import org.mockito.Mockito;

public class MarkerControllerTest {

  @Test
  public void controller_DoesNotCallSetAlphaWhenUnchanged() {
    final Marker marker = mock(Marker.class);
    final MarkerController controller = new MarkerController(marker, false);

    controller.setAlpha(0.5f);
    Mockito.verify(marker, Mockito.times(1)).setAlpha(0.5f);

    controller.setAlpha(0.5f);
    Mockito.verify(marker, Mockito.times(1)).setAlpha(0.5f);

    controller.setAlpha(0.8f);
    Mockito.verify(marker, Mockito.times(1)).setAlpha(0.8f);
  }

  @Test
  public void controller_DoesNotCallSetPositionWhenUnchanged() {
    final Marker marker = mock(Marker.class);
    final MarkerController controller = new MarkerController(marker, false);

    final LatLng pos1 = new LatLng(1.0, 2.0);
    controller.setPosition(pos1);
    Mockito.verify(marker, Mockito.times(1)).setPosition(pos1);

    final LatLng pos2 = new LatLng(1.0, 2.0);
    controller.setPosition(pos2);
    Mockito.verify(marker, Mockito.times(1)).setPosition(pos1);

    final LatLng pos3 = new LatLng(3.0, 4.0);
    controller.setPosition(pos3);
    Mockito.verify(marker, Mockito.times(1)).setPosition(pos3);
  }
}
