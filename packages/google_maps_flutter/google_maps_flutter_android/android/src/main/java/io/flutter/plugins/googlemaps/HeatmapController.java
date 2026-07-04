// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import java.util.List;

/** Controller of a single Heatmap on the map. */
public class HeatmapController implements HeatmapOptionsSink {
  private final @NonNull HeatmapTileProvider heatmap;
  private final @NonNull TileOverlay heatmapTileOverlay;
  private List<WeightedLatLng> weightedData;
  private Gradient gradient;
  private Double maxIntensity;
  private Double opacity;
  private Integer radius;
  private boolean hasChanged = false;

  /** Construct a HeatmapController with the given heatmap and heatmapTileOverlay. */
  HeatmapController(@NonNull HeatmapTileProvider heatmap, @NonNull TileOverlay heatmapTileOverlay) {
    this.heatmap = heatmap;
    this.heatmapTileOverlay = heatmapTileOverlay;
  }

  /** Remove the heatmap from the map. */
  void remove() {
    heatmapTileOverlay.remove();
  }

  /** Clear the tile cache of the heatmap in order to update the heatmap. */
  void clearTileCache() {
    if (hasChanged) {
      heatmapTileOverlay.clearTileCache();
      hasChanged = false;
    }
  }

  @Override
  public void setWeightedData(@NonNull List<WeightedLatLng> weightedData) {
    heatmap.updateData(weightedData);
    if (java.util.Objects.equals(this.weightedData, weightedData)) {
      return;
    }
    this.weightedData = weightedData;
    this.hasChanged = true;
    heatmap.setWeightedData(weightedData);  }

  @Override
  public void setGradient(@NonNull Gradient gradient) {
    if (java.util.Objects.equals(this.gradient, gradient)) {
      return;
    }
    this.gradient = gradient;
    this.hasChanged = true;
    heatmap.setGradient(gradient);
  }

  @Override
  public void setMaxIntensity(double maxIntensity) {
    if (this.maxIntensity != null && Double.compare(this.maxIntensity, maxIntensity) == 0) {
      return;
    }
    this.maxIntensity = maxIntensity;
    this.hasChanged = true;
    heatmap.setMaxIntensity(maxIntensity);
  }

  @Override
  public void setOpacity(double opacity) {
    if (this.opacity != null && Double.compare(this.opacity, opacity) == 0) {
      return;
    }
    this.opacity = opacity;
    this.hasChanged = true;
    heatmap.setOpacity(opacity);
  }

  @Override
  public void setRadius(int radius) {
    if (this.radius != null && this.radius == radius) {
      return;
    }
    this.radius = radius;
    this.hasChanged = true;
    heatmap.setRadius(radius);
  }
}
