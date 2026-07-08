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
   * <p>Throws {@link IllegalStateException} if the lifecycle reference cannot be extracted.
   */
  @NonNull
  public static Lifecycle getActivityLifecycle(
      @NonNull ActivityPluginBinding activityPluginBinding) {
    Object referenceObject = activityPluginBinding.getLifecycle();
    if (!(referenceObject instanceof HiddenLifecycleReference)) {
      throw new IllegalStateException(
          "Cannot extract lifecycle from ActivityPluginBinding. Please ensure that you are using a"
              + " version of the Flutter engine that supports getLifecycle() and provides a"
              + " HiddenLifecycleReference.");
    }
    HiddenLifecycleReference reference = (HiddenLifecycleReference) referenceObject;
    return reference.getLifecycle();
  }
}
