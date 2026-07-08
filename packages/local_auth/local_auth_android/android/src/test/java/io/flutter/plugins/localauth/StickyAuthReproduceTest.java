// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.localauth;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.os.Bundle;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {StickyAuthReproduceTest.ShadowBiometricPrompt.class})
public class StickyAuthReproduceTest {
  static final AuthStrings dummyStrings =
      new AuthStrings("a reason", "a hint", "cancel", "sign in");

  static final AuthOptions stickyOptions =
      new AuthOptions(
          /* biometricOnly */ false, /* sensitiveTransaction */ false, /* sticky */ true);

  static FragmentActivity activeActivity;

  @Implements(BiometricPrompt.class)
  public static class ShadowBiometricPrompt {
    static int authenticateCalls = 0;
    static int cancelCalls = 0;

    @Implementation
    protected void authenticate(BiometricPrompt.PromptInfo promptInfo) {
      authenticateCalls++;
      if (activeActivity != null && activeActivity.getSupportFragmentManager().isStateSaved()) {
        throw new IllegalStateException("Can not perform this action after onSaveInstanceState");
      }
    }

    @Implementation
    protected void cancelAuthentication() {
      cancelCalls++;
    }
  }

  public static class TestActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      activeActivity = this;
    }
  }

  private <T extends FragmentActivity> ActivityController<T> buildActivityController(
      Class<T> activityClass) {
    return Robolectric.buildActivity(activityClass);
  }

  @Test
  public void testStickyAuthCrashOnBackgrounding() {
    ShadowBiometricPrompt.authenticateCalls = 0;

    ActivityController<TestActivity> controller = buildActivityController(TestActivity.class);
    TestActivity activity = controller.setup().get();

    AuthenticationHelper.AuthCompletionHandler handler =
        mock(AuthenticationHelper.AuthCompletionHandler.class);

    AuthenticationHelper helper =
        new AuthenticationHelper(
            activity.getLifecycle(), activity, stickyOptions, dummyStrings, handler, true);

    // 1. Start the authentication prompt.
    helper.authenticate();
    ShadowLooper.idleMainLooper();
    assertEquals(1, ShadowBiometricPrompt.authenticateCalls);

    // 2. Simulate backgrounding: Pause and dismiss the dialog.
    controller.pause();
    helper.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "Canceled");

    // 3. Simulate foregrounding: Resume.
    // This schedules prompt.authenticate() on the looper queue.
    controller.resume();

    // 4. Immediately background the activity again BEFORE the looper executes the posted task.
    // Pause, save instance state, and stop the activity to trigger state loss status.
    controller.pause().saveInstanceState(new Bundle()).stop();

    // 5. Idle the main looper to execute the posted task.
    // Under fixed code, this task should be skipped since the activity is paused again.
    ShadowLooper.idleMainLooper();
    assertEquals(1, ShadowBiometricPrompt.authenticateCalls);

    // 6. Simulate foregrounding again.
    controller.start().resume();
    ShadowLooper.idleMainLooper();
    assertEquals(2, ShadowBiometricPrompt.authenticateCalls);
  }

  @Test
  public void testStopAuthenticationBeforeResumptionExecutes() {
    ShadowBiometricPrompt.authenticateCalls = 0;
    ShadowBiometricPrompt.cancelCalls = 0;

    ActivityController<TestActivity> controller = buildActivityController(TestActivity.class);
    TestActivity activity = controller.setup().get();

    AuthenticationHelper.AuthCompletionHandler handler =
        mock(AuthenticationHelper.AuthCompletionHandler.class);

    AuthenticationHelper helper =
        new AuthenticationHelper(
            activity.getLifecycle(), activity, stickyOptions, dummyStrings, handler, true);

    // 1. Start the authentication prompt.
    helper.authenticate();
    ShadowLooper.idleMainLooper();
    assertEquals(1, ShadowBiometricPrompt.authenticateCalls);

    // 2. Simulate backgrounding: Pause and dismiss the dialog.
    controller.pause();
    helper.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "Canceled");

    // 3. Simulate foregrounding: Resume.
    // This schedules prompt.authenticate() on the looper queue.
    controller.resume();

    // 4. Immediately stop authentication before the looper runs.
    helper.stopAuthentication();

    // 5. Idle the main looper.
    ShadowLooper.idleMainLooper();
    assertEquals(1, ShadowBiometricPrompt.authenticateCalls);
    assertEquals(1, ShadowBiometricPrompt.cancelCalls);
  }

  @Test
  public void testStopAuthenticationAfterResumptionExecutes() {
    ShadowBiometricPrompt.authenticateCalls = 0;
    ShadowBiometricPrompt.cancelCalls = 0;

    ActivityController<TestActivity> controller = buildActivityController(TestActivity.class);
    TestActivity activity = controller.setup().get();

    AuthenticationHelper.AuthCompletionHandler handler =
        mock(AuthenticationHelper.AuthCompletionHandler.class);

    AuthenticationHelper helper =
        new AuthenticationHelper(
            activity.getLifecycle(), activity, stickyOptions, dummyStrings, handler, true);

    // 1. Start.
    helper.authenticate();
    ShadowLooper.idleMainLooper();

    // 2. Pause.
    controller.pause();
    helper.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "Canceled");

    // 3. Resume.
    controller.resume();
    ShadowLooper.idleMainLooper();
    assertEquals(2, ShadowBiometricPrompt.authenticateCalls);

    // 4. Stop authentication.
    helper.stopAuthentication();
    assertEquals(1, ShadowBiometricPrompt.cancelCalls);
  }
}
