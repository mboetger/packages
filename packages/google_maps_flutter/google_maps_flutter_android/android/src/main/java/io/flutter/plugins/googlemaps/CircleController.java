// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

/** Controller of a single Circle on the map. */
class CircleController implements CircleOptionsSink {
  private final Circle circle;
  private final String googleMapsCircleId;
  private final float density;
  private boolean consumeTapEvents;
  private Integer strokeColor;
  private Integer fillColor;
  private LatLng center;
  private Double radius;
  private Boolean visible;
  private Float strokeWidth;
  private Float zIndex;

  CircleController(Circle circle, boolean consumeTapEvents, float density) {
    this.circle = circle;
    this.consumeTapEvents = consumeTapEvents;
    this.density = density;
    this.googleMapsCircleId = circle.getId();
  }

  void remove() {
    circle.remove();
  }

  @Override
  public void setConsumeTapEvents(boolean consumeTapEvents) {
    if (this.consumeTapEvents == consumeTapEvents) {
      return;
    }
    this.consumeTapEvents = consumeTapEvents;
    circle.setClickable(consumeTapEvents);
  }

  @Override
  public void setStrokeColor(int strokeColor) {
    if (this.strokeColor != null && this.strokeColor == strokeColor) {
      return;
    }
    this.strokeColor = strokeColor;
    circle.setStrokeColor(strokeColor);
  }

  @Override
  public void setFillColor(int fillColor) {
    if (this.fillColor != null && this.fillColor == fillColor) {
      return;
    }
    this.fillColor = fillColor;
    circle.setFillColor(fillColor);
  }

  @Override
  public void setCenter(LatLng center) {
    if (java.util.Objects.equals(this.center, center)) {
      return;
    }
    this.center = center;
    circle.setCenter(center);
  }

  @Override
  public void setRadius(double radius) {
    if (this.radius != null && Double.compare(this.radius, radius) == 0) {
      return;
    }
    this.radius = radius;
    circle.setRadius(radius);
  }

  @Override
  public void setVisible(boolean visible) {
    if (this.visible != null && this.visible == visible) {
      return;
    }
    this.visible = visible;
    circle.setVisible(visible);
  }

  @Override
  public void setStrokeWidth(float strokeWidth) {
    if (this.strokeWidth != null && Float.compare(this.strokeWidth, strokeWidth) == 0) {
      return;
    }
    this.strokeWidth = strokeWidth;
    circle.setStrokeWidth(strokeWidth * density);
  }

  @Override
  public void setZIndex(float zIndex) {
    if (this.zIndex != null && Float.compare(this.zIndex, zIndex) == 0) {
      return;
    }
    this.zIndex = zIndex;
    circle.setZIndex(zIndex);
  }

  String getGoogleMapsCircleId() {
    return googleMapsCircleId;
  }

  boolean consumeTapEvents() {
    return consumeTapEvents;
  }
}
