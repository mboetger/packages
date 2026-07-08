// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.embedding.engine.plugins.lifecycle;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FlutterLifecycleAdapterReproduceTest {
  @Mock ActivityPluginBinding mockActivityPluginBinding;

  AutoCloseable mockCloseable;

  @Before
  public void setUp() {
    mockCloseable = MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    mockCloseable.close();
  }

  @Test
  public void getActivityLifecycle_throwsIfLifecycleIsNull() {
    when(mockActivityPluginBinding.getLifecycle()).thenReturn(null);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          FlutterLifecycleAdapter.getActivityLifecycle(mockActivityPluginBinding);
        });
  }

  @Test
  public void getActivityLifecycle_throwsIfLifecycleIsNotHiddenLifecycleReference() {
    Object invalidReference = new Object();
    when(mockActivityPluginBinding.getLifecycle()).thenReturn(invalidReference);

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          FlutterLifecycleAdapter.getActivityLifecycle(mockActivityPluginBinding);
        });
  }
}
