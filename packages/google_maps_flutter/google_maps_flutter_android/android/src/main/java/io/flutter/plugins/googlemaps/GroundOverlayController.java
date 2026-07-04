// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

class GroundOverlayController implements GroundOverlaySink {
  private final GroundOverlay groundOverlay;
  private final String googleMapsGroundOverlayId;
  private final boolean isCreatedWithBounds;
  private Float transparency;
  private Float zIndex;
  private Boolean visible;
  private Float bearing;
  private Boolean clickable;
  private BitmapDescriptor image;
  private LatLng location;
  private Float width;
  private Float height;
  private LatLngBounds bounds;

  GroundOverlayController(@NonNull GroundOverlay groundOverlay, boolean isCreatedWithBounds) {
    this.groundOverlay = groundOverlay;
    this.googleMapsGroundOverlayId = groundOverlay.getId();
    this.isCreatedWithBounds = isCreatedWithBounds;
  }

  void remove() {
    groundOverlay.remove();
  }

  GroundOverlay getGroundOverlay() {
    return groundOverlay;
  }

  @Override
  public void setTransparency(float transparency) {
    if (this.transparency != null && Float.compare(this.transparency, transparency) == 0) {
      return;
    }
    this.transparency = transparency;
    groundOverlay.setTransparency(transparency);
  }

  @Override
  public void setZIndex(float zIndex) {
    if (this.zIndex != null && Float.compare(this.zIndex, zIndex) == 0) {
      return;
    }
    this.zIndex = zIndex;
    groundOverlay.setZIndex(zIndex);
  }

  @Override
  public void setVisible(boolean visible) {
    if (this.visible != null && this.visible == visible) {
      return;
    }
    this.visible = visible;
    groundOverlay.setVisible(visible);
  }

  @Override
  public void setAnchor(float u, float v) {}

  @Override
  public void setBearing(float bearing) {
    if (this.bearing != null && Float.compare(this.bearing, bearing) == 0) {
      return;
    }
    this.bearing = bearing;
    groundOverlay.setBearing(bearing);
  }

  @Override
  public void setClickable(boolean clickable) {
    if (this.clickable != null && this.clickable == clickable) {
      return;
    }
    this.clickable = clickable;
    groundOverlay.setClickable(clickable);
  }

  @Override
  public void setImage(@NonNull BitmapDescriptor imageDescriptor) {
    if (java.util.Objects.equals(this.image, imageDescriptor)) {
      return;
    }
    this.image = imageDescriptor;
    groundOverlay.setImage(imageDescriptor);
  }

  @Override
  public void setPosition(@NonNull LatLng location, @NonNull Float width, @Nullable Float height) {
    if (java.util.Objects.equals(this.location, location)
        && java.util.Objects.equals(this.width, width)
        && java.util.Objects.equals(this.height, height)) {
      return;
    }
    this.location = location;
    this.width = width;
    this.height = height;
    groundOverlay.setPosition(location);
    if (height == null) {
      groundOverlay.setDimensions(width);
    } else {
      groundOverlay.setDimensions(width, height);
    }
  }

  @Override
  public void setPositionFromBounds(@NonNull LatLngBounds bounds) {
    if (java.util.Objects.equals(this.bounds, bounds)) {
      return;
    }
    this.bounds = bounds;
    groundOverlay.setPositionFromBounds(bounds);
  }

  String getGoogleMapsGroundOverlayId() {
    return googleMapsGroundOverlayId;
  }

  public boolean isCreatedWithBounds() {
    return isCreatedWithBounds;
  }
}
