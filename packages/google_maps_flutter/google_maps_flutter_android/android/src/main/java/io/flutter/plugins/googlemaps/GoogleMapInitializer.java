// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemaps;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.MapsInitializer;
import io.flutter.plugin.common.BinaryMessenger;

/** GoogleMaps initializer used to initialize the Google Maps SDK with preferred settings. */
final class GoogleMapInitializer implements Messages.MapsInitializerApi {
  private final Context context;
  private boolean initialized = false;

  GoogleMapInitializer(Context context, BinaryMessenger binaryMessenger) {
    this.context = context;

    Messages.MapsInitializerApi.setUp(binaryMessenger, this);
  }

  @Override
  public void initialize(@NonNull Messages.Result<Void> result) {
    if (initialized) {
      result.error(
          new Messages.FlutterError(
              "Already initialized",
              "Initializer can only be called once.",
              null));
    } else {
      initialized = true;
      // The Maps SDK needs to be initialized before any call to `MapView` can be made.
      // The renderer doesn't need to be specified explicitly.
      // The SDK will automatically determine the best renderer to use.
      //
      // See https://developers.google.com/maps/documentation/android-sdk/renderer
      MapsInitializer.initialize(context);
      result.success(null);
    }
  }
}
