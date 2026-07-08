// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.maps.model.Marker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MarkerControllerTest {

  @Test
  public void setInfoWindowText_whenInfoWindowIsShown_updatesTextAndShowsInfoWindow() {
    Marker marker = mock(Marker.class);
    when(marker.isInfoWindowShown()).thenReturn(true);

    MarkerController controller = new MarkerController(marker, false);
    controller.setInfoWindowText("New Title", "New Snippet");

    verify(marker).setTitle("New Title");
    verify(marker).setSnippet("New Snippet");
    verify(marker).showInfoWindow();
  }

  @Test
  public void setInfoWindowText_whenInfoWindowIsNotShown_updatesTextOnly() {
    Marker marker = mock(Marker.class);
    when(marker.isInfoWindowShown()).thenReturn(false);

    MarkerController controller = new MarkerController(marker, false);
    controller.setInfoWindowText("New Title", "New Snippet");

    verify(marker).setTitle("New Title");
    verify(marker).setSnippet("New Snippet");
    verify(marker, never()).showInfoWindow();
  }
}
