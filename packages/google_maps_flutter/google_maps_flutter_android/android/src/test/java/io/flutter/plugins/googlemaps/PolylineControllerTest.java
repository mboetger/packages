// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.google.android.gms.internal.maps.zzar;
import com.google.android.gms.maps.model.Polyline;
import org.junit.Test;
import org.mockito.Mockito;

public class PolylineControllerTest {

  @Test
  public void controller_SetsStrokeDensity() {
    final zzar z = mock(zzar.class);
    final Polyline polyline = spy(new Polyline(z));

    final float density = 5;
    final float strokeWidth = 3;
    final PolylineController controller = new PolylineController(polyline, false, density);
    controller.setWidth(strokeWidth);

    Mockito.verify(polyline).setWidth(density * strokeWidth);
  }

  @Test
  public void controller_DoesNotCallSetColorWhenUnchanged() {
    final zzaj z = mock(zzaj.class);
    final Polyline polyline = spy(new Polyline(z));
    final PolylineController controller = new PolylineController(polyline, false, 1.0f);

    controller.setColor(12345);
    Mockito.verify(polyline, Mockito.times(1)).setColor(12345);

    controller.setColor(12345);
    Mockito.verify(polyline, Mockito.times(1)).setColor(12345);

    controller.setColor(54321);
    Mockito.verify(polyline, Mockito.times(1)).setColor(54321);
  }

  @Test
  public void controller_DoesNotCallSetPointsWhenUnchanged() {
    final zzaj z = mock(zzaj.class);
    final Polyline polyline = spy(new Polyline(z));
    final PolylineController controller = new PolylineController(polyline, false, 1.0f);

    java.util.List<com.google.android.gms.maps.model.LatLng> points1 = new java.util.ArrayList<>();
    points1.add(new com.google.android.gms.maps.model.LatLng(1.0, 2.0));
    controller.setPoints(points1);
    Mockito.verify(polyline, Mockito.times(1)).setPoints(points1);

    java.util.List<com.google.android.gms.maps.model.LatLng> points2 = new java.util.ArrayList<>();
    points2.add(new com.google.android.gms.maps.model.LatLng(1.0, 2.0));
    controller.setPoints(points2);
    Mockito.verify(polyline, Mockito.times(1)).setPoints(points1);

    points2.add(new com.google.android.gms.maps.model.LatLng(3.0, 4.0));
    controller.setPoints(points2);
    Mockito.verify(polyline, Mockito.times(1)).setPoints(points2);
  }

  @Test
  public void controller_DoesNotCallSetPatternWhenUnchanged() {
    final zzaj z = mock(zzaj.class);
    final Polyline polyline = spy(new Polyline(z));
    final PolylineController controller = new PolylineController(polyline, false, 1.0f);

    controller.setPattern(null);
    Mockito.verify(polyline, Mockito.times(1)).setPattern(null);

    controller.setPattern(null);
    Mockito.verify(polyline, Mockito.times(1)).setPattern(null);
  }

  @Test
  public void controller_DoesNotCallSetWidthWhenUnchanged() {
    final zzaj z = mock(zzaj.class);
    final Polyline polyline = spy(new Polyline(z));
    final float density = 5;
    final PolylineController controller = new PolylineController(polyline, false, density);

    controller.setWidth(3.0f);
    Mockito.verify(polyline, Mockito.times(1)).setWidth(3.0f * density);

    controller.setWidth(3.0f);
    Mockito.verify(polyline, Mockito.times(1)).setWidth(3.0f * density);

    controller.setWidth(4.0f);
    Mockito.verify(polyline, Mockito.times(1)).setWidth(4.0f * density);
  }
}
