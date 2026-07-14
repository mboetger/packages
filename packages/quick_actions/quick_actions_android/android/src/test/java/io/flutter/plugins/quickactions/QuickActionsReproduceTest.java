// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.quickactions;

import static io.flutter.plugins.quickactions.QuickActions.EXTRA_ACTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class QuickActionsReproduceTest {
  private static class TestBinaryMessenger implements BinaryMessenger {
    public final List<String> sentMessages = new ArrayList<>();

    @Override
    public void send(@NonNull String channel, @Nullable ByteBuffer message) {
      send(channel, message, null);
    }

    @Override
    public void send(
        @NonNull String channel,
        @Nullable ByteBuffer message,
        @Nullable final BinaryReply callback) {
      if (channel.contains("launchAction")) {
        sentMessages.add(channel);
      }
    }

    @Override
    public void setMessageHandler(@NonNull String channel, @Nullable BinaryMessageHandler handler) {
      // Do nothing.
    }
  }

  static final int SUPPORTED_BUILD = 25;
  static final String SHORTCUT_TYPE = "action_one";

  @Test
  public void testPreWarmedEngine_reportsActionOnce()
      throws NoSuchFieldException, IllegalAccessException {
    // 1. Arrange: Initialize plugin (simulating pre-warmed engine)
    final TestBinaryMessenger testBinaryMessenger = new TestBinaryMessenger();
    final QuickActionsPlugin plugin =
        new QuickActionsPlugin((version) -> SUPPORTED_BUILD >= version);
    
    final FlutterPluginBinding mockPluginBinding = mock(FlutterPluginBinding.class);
    when(mockPluginBinding.getBinaryMessenger()).thenReturn(testBinaryMessenger);
    final Context mockContext = mock(Context.class);
    when(mockPluginBinding.getApplicationContext()).thenReturn(mockContext);

    // Simulating pre-warmed engine start
    plugin.onAttachedToEngine(mockPluginBinding);

    // Get quickActions instance using reflection
    Field quickActionsField = QuickActionsPlugin.class.getDeclaredField("quickActions");
    quickActionsField.setAccessible(true);
    QuickActions quickActions = (QuickActions) quickActionsField.get(plugin);
    // Simulating Dart registering its receiver during initialization
    quickActions.registerReceiver();

    // 2. Act: Simulate activity attachment (when user launches app via shortcut)
    final Intent mockIntent = mock(Intent.class);
    when(mockIntent.hasExtra(EXTRA_ACTION)).thenReturn(true);
    when(mockIntent.getStringExtra(EXTRA_ACTION)).thenReturn(SHORTCUT_TYPE);
    
    final Activity mockMainActivity = mock(Activity.class);
    when(mockMainActivity.getIntent()).thenReturn(mockIntent);
    when(mockMainActivity.getApplicationContext()).thenReturn(mockContext);

    final ActivityPluginBinding mockActivityPluginBinding = mock(ActivityPluginBinding.class);
    when(mockActivityPluginBinding.getActivity()).thenReturn(mockMainActivity);

    plugin.onAttachedToActivity(mockActivityPluginBinding);

    // 3. Act: Simulate Dart calling initialize() -> getLaunchAction()
    String launchAction = quickActions.getLaunchAction();

    // 4. Assert: Verify the issue is resolved (action reported exactly once via channel)
    // It is reported once via launchAction channel message:
    assertEquals(1, testBinaryMessenger.sentMessages.size());

    // If it was fixed, it should only be reported once. For example, if it was reported via
    // launchAction, getLaunchAction() should return null (because EXTRA_ACTION is cleared).
    // We assert that the launch action is null here since we expect it to be null if it was already sent via launchAction.
    assertNull("Expected launchAction to be null because it was already sent via launchAction channel", launchAction);
  }
}
