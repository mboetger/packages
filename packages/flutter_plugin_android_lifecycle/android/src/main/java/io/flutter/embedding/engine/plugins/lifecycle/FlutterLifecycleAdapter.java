// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.embedding.engine.plugins.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

/** Provides a static method for extracting lifecycle objects from Flutter plugin bindings. */
public class FlutterLifecycleAdapter {
  /**
   * Returns the lifecycle object for the activity a plugin is bound to.
   *
   * @throws IllegalArgumentException if the Flutter engine version is too old and does not support
   *     the hidden lifecycle API, or if the lifecycle reference is null or invalid.
   */
  @NonNull
  public static Lifecycle getActivityLifecycle(
      @NonNull ActivityPluginBinding activityPluginBinding) {
    Object lifecycleReference = activityPluginBinding.getLifecycle();
    if (lifecycleReference == null) {
      throw new IllegalArgumentException(
          "ActivityPluginBinding.getLifecycle() returned null. This plugin requires an engine"
              + " version that supports HiddenLifecycleReference.");
    }
    if (!(lifecycleReference instanceof HiddenLifecycleReference)) {
      throw new IllegalArgumentException(
          "ActivityPluginBinding.getLifecycle() returned a non-HiddenLifecycleReference object: "
              + lifecycleReference.getClass().getName()
              + ". This plugin requires an engine version that supports HiddenLifecycleReference.");
    }
    HiddenLifecycleReference reference = (HiddenLifecycleReference) lifecycleReference;
    return reference.getLifecycle();
  }
}
