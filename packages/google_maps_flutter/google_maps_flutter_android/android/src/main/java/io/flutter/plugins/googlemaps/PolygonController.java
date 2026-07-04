// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import java.util.List;

/** Controller of a single Polygon on the map. */
class PolygonController implements PolygonOptionsSink {
  private final Polygon polygon;
  private final String googleMapsPolygonId;
  private final float density;
  private boolean consumeTapEvents;
  private Integer fillColor;
  private Integer strokeColor;
  private Boolean geodesic;
  private List<LatLng> points;
  private List<List<LatLng>> holes;
  private Boolean visible;
  private Float strokeWidth;
  private Float zIndex;

  PolygonController(Polygon polygon, boolean consumeTapEvents, float density) {
    this.polygon = polygon;
    this.density = density;
    this.consumeTapEvents = consumeTapEvents;
    this.googleMapsPolygonId = polygon.getId();
  }

  void remove() {
    polygon.remove();
  }

  @Override
  public void setConsumeTapEvents(boolean consumeTapEvents) {
    if (this.consumeTapEvents == consumeTapEvents) {
      return;
    }
    this.consumeTapEvents = consumeTapEvents;
    polygon.setClickable(consumeTapEvents);
  }

  @Override
  public void setFillColor(int color) {
    if (this.fillColor != null && this.fillColor == color) {
      return;
    }
    this.fillColor = color;
    polygon.setFillColor(color);
  }

  @Override
  public void setStrokeColor(int color) {
    if (this.strokeColor != null && this.strokeColor == color) {
      return;
    }
    this.strokeColor = color;
    polygon.setStrokeColor(color);
  }

  @Override
  public void setGeodesic(boolean geodesic) {
    if (this.geodesic != null && this.geodesic == geodesic) {
      return;
    }
    this.geodesic = geodesic;
    polygon.setGeodesic(geodesic);
  }

  @Override
  public void setPoints(List<LatLng> points) {
    if (java.util.Objects.equals(this.points, points)) {
      return;
    }
    this.points = points;
    polygon.setPoints(points);
  }

  public void setHoles(List<List<LatLng>> holes) {
    if (java.util.Objects.equals(this.holes, holes)) {
      return;
    }
    this.holes = holes;
    polygon.setHoles(holes);
  }

  @Override
  public void setVisible(boolean visible) {
    if (this.visible != null && this.visible == visible) {
      return;
    }
    this.visible = visible;
    polygon.setVisible(visible);
  }

  @Override
  public void setStrokeWidth(float width) {
    if (this.strokeWidth != null && Float.compare(this.strokeWidth, width) == 0) {
      return;
    }
    this.strokeWidth = width;
    polygon.setStrokeWidth(width * density);
  }

  @Override
  public void setZIndex(float zIndex) {
    if (this.zIndex != null && Float.compare(this.zIndex, zIndex) == 0) {
      return;
    }
    this.zIndex = zIndex;
    polygon.setZIndex(zIndex);
  }

  String getGoogleMapsPolygonId() {
    return googleMapsPolygonId;
  }

  boolean consumeTapEvents() {
    return consumeTapEvents;
  }
}
