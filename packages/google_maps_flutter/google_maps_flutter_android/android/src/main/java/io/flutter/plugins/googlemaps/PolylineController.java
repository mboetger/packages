// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import java.util.List;

/** Controller of a single Polyline on the map. */
class PolylineController implements PolylineOptionsSink {
  private final Polyline polyline;
  private final String googleMapsPolylineId;
  private boolean consumeTapEvents;
  private final float density;
  private Integer color;
  private Cap endCap;
  private Boolean geodesic;
  private Integer jointType;
  private List<PatternItem> pattern;
  private List<LatLng> points;
  private Cap startCap;
  private Boolean visible;
  private Float width;
  private Float zIndex;

  PolylineController(Polyline polyline, boolean consumeTapEvents, float density) {
    this.polyline = polyline;
    this.consumeTapEvents = consumeTapEvents;
    this.density = density;
    this.googleMapsPolylineId = polyline.getId();
  }

  void remove() {
    polyline.remove();
  }

  @Override
  public void setConsumeTapEvents(boolean consumeTapEvents) {
    if (this.consumeTapEvents == consumeTapEvents) {
      return;
    }
    this.consumeTapEvents = consumeTapEvents;
    polyline.setClickable(consumeTapEvents);
  }

  @Override
  public void setColor(int color) {
    if (this.color != null && this.color == color) {
      return;
    }
    this.color = color;
    polyline.setColor(color);
  }

  @Override
  public void setEndCap(Cap endCap) {
    if (java.util.Objects.equals(this.endCap, endCap)) {
      return;
    }
    this.endCap = endCap;
    polyline.setEndCap(endCap);
  }

  @Override
  public void setGeodesic(boolean geodesic) {
    if (this.geodesic != null && this.geodesic == geodesic) {
      return;
    }
    this.geodesic = geodesic;
    polyline.setGeodesic(geodesic);
  }

  @Override
  public void setJointType(int jointType) {
    if (this.jointType != null && this.jointType == jointType) {
      return;
    }
    this.jointType = jointType;
    polyline.setJointType(jointType);
  }

  @Override
  public void setPattern(List<PatternItem> pattern) {
    if (java.util.Objects.equals(this.pattern, pattern)) {
      return;
    }
    this.pattern = pattern;
    polyline.setPattern(pattern);
  }

  @Override
  public void setPoints(List<LatLng> points) {
    if (java.util.Objects.equals(this.points, points)) {
      return;
    }
    this.points = points;
    polyline.setPoints(points);
  }

  @Override
  public void setStartCap(Cap startCap) {
    if (java.util.Objects.equals(this.startCap, startCap)) {
      return;
    }
    this.startCap = startCap;
    polyline.setStartCap(startCap);
  }

  @Override
  public void setVisible(boolean visible) {
    if (this.visible != null && this.visible == visible) {
      return;
    }
    this.visible = visible;
    polyline.setVisible(visible);
  }

  @Override
  public void setWidth(float width) {
    if (this.width != null && Float.compare(this.width, width) == 0) {
      return;
    }
    this.width = width;
    polyline.setWidth(width * density);
  }

  @Override
  public void setZIndex(float zIndex) {
    if (this.zIndex != null && Float.compare(this.zIndex, zIndex) == 0) {
      return;
    }
    this.zIndex = zIndex;
    polyline.setZIndex(zIndex);
  }

  String getGoogleMapsPolylineId() {
    return googleMapsPolylineId;
  }

  boolean consumeTapEvents() {
    return consumeTapEvents;
  }
}
