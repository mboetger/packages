// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileProvider;

class TileOverlayController implements TileOverlaySink {

  private final TileOverlay tileOverlay;
  private Boolean fadeIn;
  private Float transparency;
  private Float zIndex;
  private Boolean visible;

  TileOverlayController(TileOverlay tileOverlay) {
    this.tileOverlay = tileOverlay;
  }

  void remove() {
    tileOverlay.remove();
  }

  void clearTileCache() {
    tileOverlay.clearTileCache();
  }

  TileOverlay getTileOverlay() {
    return tileOverlay;
  }

  @Override
  public void setFadeIn(boolean fadeIn) {
    if (this.fadeIn != null && this.fadeIn == fadeIn) {
      return;
    }
    this.fadeIn = fadeIn;
    tileOverlay.setFadeIn(fadeIn);
  }

  @Override
  public void setTransparency(float transparency) {
    if (this.transparency != null && Float.compare(this.transparency, transparency) == 0) {
      return;
    }
    this.transparency = transparency;
    tileOverlay.setTransparency(transparency);
  }

  @Override
  public void setZIndex(float zIndex) {
    if (this.zIndex != null && Float.compare(this.zIndex, zIndex) == 0) {
      return;
    }
    this.zIndex = zIndex;
    tileOverlay.setZIndex(zIndex);
  }

  @Override
  public void setVisible(boolean visible) {
    if (this.visible != null && this.visible == visible) {
      return;
    }
    this.visible = visible;
    tileOverlay.setVisible(visible);
  }

  @Override
  public void setTileProvider(TileProvider tileProvider) {
    // You can not change tile provider after creation
  }
}
