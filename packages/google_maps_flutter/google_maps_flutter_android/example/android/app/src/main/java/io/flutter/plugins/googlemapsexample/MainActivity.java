// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.googlemapsexample;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
  private static final String CHANNEL = "google_maps_flutter_example/test_helper";

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    super.configureFlutterEngine(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
        .setMethodCallHandler(
            (call, result) -> {
              if (call.method.equals("isApiKeySet")) {
                result.success(isApiKeySet());
              } else {
                result.notImplemented();
              }
            });
  }

  private boolean isApiKeySet() {
    try {
      ApplicationInfo app =
          getPackageManager()
              .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
      Bundle bundle = app.metaData;
      if (bundle != null) {
        String apiKey = bundle.getString("com.google.android.geo.API_KEY");
        return apiKey != null && !apiKey.isEmpty();
      }
    } catch (PackageManager.NameNotFoundException e) {
      // Ignore
    }
    return false;
  }
}
