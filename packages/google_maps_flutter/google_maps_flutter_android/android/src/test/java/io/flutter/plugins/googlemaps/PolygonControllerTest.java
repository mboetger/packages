// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.google.android.gms.internal.maps.zzao;
import com.google.android.gms.maps.model.Polygon;
import org.junit.Test;
import org.mockito.Mockito;

public class PolygonControllerTest {

  @Test
  public void controller_SetsStrokeDensity() {
    final zzao z = mock(zzao.class);
    final Polygon polygon = spy(new Polygon(z));

    final float density = 5;
    final float strokeWidth = 3;
    final PolygonController controller = new PolygonController(polygon, false, density);
    controller.setStrokeWidth(strokeWidth);

    Mockito.verify(polygon).setStrokeWidth(density * strokeWidth);
  }

  @Test
  public void controller_DoesNotCallSetFillColorWhenUnchanged() {
    final zzag z = mock(zzag.class);
    final Polygon polygon = spy(new Polygon(z));
    final PolygonController controller = new PolygonController(polygon, false, 1.0f);

    controller.setFillColor(12345);
    Mockito.verify(polygon, Mockito.times(1)).setFillColor(12345);

    controller.setFillColor(12345);
    Mockito.verify(polygon, Mockito.times(1)).setFillColor(12345);

    controller.setFillColor(54321);
    Mockito.verify(polygon, Mockito.times(1)).setFillColor(54321);
  }

  @Test
  public void controller_DoesNotCallSetPointsWhenUnchanged() {
    final zzag z = mock(zzag.class);
    final Polygon polygon = spy(new Polygon(z));
    final PolygonController controller = new PolygonController(polygon, false, 1.0f);

    java.util.List<com.google.android.gms.maps.model.LatLng> points1 = new java.util.ArrayList<>();
    points1.add(new com.google.android.gms.maps.model.LatLng(1.0, 2.0));
    controller.setPoints(points1);
    Mockito.verify(polygon, Mockito.times(1)).setPoints(points1);

    java.util.List<com.google.android.gms.maps.model.LatLng> points2 = new java.util.ArrayList<>();
    points2.add(new com.google.android.gms.maps.model.LatLng(1.0, 2.0));
    controller.setPoints(points2);
    Mockito.verify(polygon, Mockito.times(1)).setPoints(points1);

    points2.add(new com.google.android.gms.maps.model.LatLng(3.0, 4.0));
    controller.setPoints(points2);
    Mockito.verify(polygon, Mockito.times(1)).setPoints(points2);
  }
}
