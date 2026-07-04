// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.google.android.gms.internal.maps.zzl;
import com.google.android.gms.maps.model.Circle;
import org.junit.Test;
import org.mockito.Mockito;

public class CircleControllerTest {

  @Test
  public void controller_SetsStrokeDensity() {
    final zzl z = mock(zzl.class);
    final Circle circle = spy(new Circle(z));

    final float density = 5;
    final float strokeWidth = 3;
    final CircleController controller = new CircleController(circle, false, density);
    controller.setStrokeWidth(strokeWidth);

    Mockito.verify(circle).setStrokeWidth(density * strokeWidth);
  }

  @Test
  public void controller_DoesNotCallSetFillColorWhenUnchanged() {
    final zzl z = mock(zzl.class);
    final Circle circle = spy(new Circle(z));
    final CircleController controller = new CircleController(circle, false, 1.0f);

    controller.setFillColor(12345);
    Mockito.verify(circle, Mockito.times(1)).setFillColor(12345);

    controller.setFillColor(12345);
    Mockito.verify(circle, Mockito.times(1)).setFillColor(12345);

    controller.setFillColor(54321);
    Mockito.verify(circle, Mockito.times(1)).setFillColor(54321);
  }

  @Test
  public void controller_DoesNotCallSetRadiusWhenUnchanged() {
    final zzl z = mock(zzl.class);
    final Circle circle = spy(new Circle(z));
    final CircleController controller = new CircleController(circle, false, 1.0f);

    controller.setRadius(100.0);
    Mockito.verify(circle, Mockito.times(1)).setRadius(100.0);

    controller.setRadius(100.0);
    Mockito.verify(circle, Mockito.times(1)).setRadius(100.0);

    controller.setRadius(200.0);
    Mockito.verify(circle, Mockito.times(1)).setRadius(200.0);
  }
}
