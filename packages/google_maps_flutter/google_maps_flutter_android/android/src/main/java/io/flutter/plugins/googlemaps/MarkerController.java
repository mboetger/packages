// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.collections.MarkerManager;
import java.lang.ref.WeakReference;

/** Controller of a single Marker on the map. */
class MarkerController implements MarkerOptionsSink {

  // Holds a weak reference to a Marker instance. The clustering library
  // dynamically manages markers, adding and removing them from the map
  // as needed based on user interaction or data changes.
  private final WeakReference<Marker> weakMarker;
  private final String googleMapsMarkerId;
  private boolean consumeTapEvents;
  private Float alpha;
  private Float anchorU;
  private Float anchorV;
  private Boolean draggable;
  private Boolean flat;
  private BitmapDescriptor icon;
  private Float infoWindowAnchorU;
  private Float infoWindowAnchorV;
  private String title;
  private String snippet;
  private LatLng position;
  private Float rotation;
  private Boolean visible;
  private Float zIndex;

  MarkerController(Marker marker, boolean consumeTapEvents) {
    this.weakMarker = new WeakReference<>(marker);
    this.consumeTapEvents = consumeTapEvents;
    this.googleMapsMarkerId = marker.getId();
  }

  void removeFromCollection(MarkerManager.Collection markerCollection) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    markerCollection.remove(marker);
  }

  @Override
  public void setAlpha(float alpha) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.alpha != null && Float.compare(this.alpha, alpha) == 0) {
      return;
    }
    this.alpha = alpha;
    marker.setAlpha(alpha);
  }

  @Override
  public void setAnchor(float u, float v) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.anchorU != null
        && this.anchorV != null
        && Float.compare(this.anchorU, u) == 0
        && Float.compare(this.anchorV, v) == 0) {
      return;
    }
    this.anchorU = u;
    this.anchorV = v;
    marker.setAnchor(u, v);
  }

  @Override
  public void setConsumeTapEvents(boolean consumeTapEvents) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.consumeTapEvents == consumeTapEvents) {
      return;
    }
    this.consumeTapEvents = consumeTapEvents;
  }

  @Override
  public void setDraggable(boolean draggable) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.draggable != null && this.draggable == draggable) {
      return;
    }
    this.draggable = draggable;
    marker.setDraggable(draggable);
  }

  @Override
  public void setFlat(boolean flat) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.flat != null && this.flat == flat) {
      return;
    }
    this.flat = flat;
    marker.setFlat(flat);
  }

  @Override
  public void setIcon(BitmapDescriptor bitmapDescriptor) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (java.util.Objects.equals(this.icon, bitmapDescriptor)) {
      return;
    }
    this.icon = bitmapDescriptor;
    marker.setIcon(bitmapDescriptor);
  }

  @Override
  public void setInfoWindowAnchor(float u, float v) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.infoWindowAnchorU != null
        && this.infoWindowAnchorV != null
        && Float.compare(this.infoWindowAnchorU, u) == 0
        && Float.compare(this.infoWindowAnchorV, v) == 0) {
      return;
    }
    this.infoWindowAnchorU = u;
    this.infoWindowAnchorV = v;
    marker.setInfoWindowAnchor(u, v);
  }

  @Override
  public void setInfoWindowText(String title, String snippet) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (java.util.Objects.equals(this.title, title)
        && java.util.Objects.equals(this.snippet, snippet)) {
      return;
    }
    this.title = title;
    this.snippet = snippet;
    marker.setTitle(title);
    marker.setSnippet(snippet);
  }

  @Override
  public void setPosition(LatLng position) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (java.util.Objects.equals(this.position, position)) {
      return;
    }
    this.position = position;
    marker.setPosition(position);
  }

  @Override
  public void setRotation(float rotation) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.rotation != null && Float.compare(this.rotation, rotation) == 0) {
      return;
    }
    this.rotation = rotation;
    marker.setRotation(rotation);
  }

  @Override
  public void setVisible(boolean visible) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.visible != null && this.visible == visible) {
      return;
    }
    this.visible = visible;
    marker.setVisible(visible);
  }

  @Override
  public void setZIndex(float zIndex) {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    if (this.zIndex != null && Float.compare(this.zIndex, zIndex) == 0) {
      return;
    }
    this.zIndex = zIndex;
    marker.setZIndex(zIndex);
  }

  @Override
  public void setCollisionBehavior(
      @AdvancedMarkerOptions.CollisionBehavior int collisionBehavior) {}

  String getGoogleMapsMarkerId() {
    return googleMapsMarkerId;
  }

  boolean consumeTapEvents() {
    return consumeTapEvents;
  }

  public void showInfoWindow() {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    marker.showInfoWindow();
  }

  public void hideInfoWindow() {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return;
    }
    marker.hideInfoWindow();
  }

  public boolean isInfoWindowShown() {
    Marker marker = weakMarker.get();
    if (marker == null) {
      return false;
    }
    return marker.isInfoWindowShown();
  }
}
