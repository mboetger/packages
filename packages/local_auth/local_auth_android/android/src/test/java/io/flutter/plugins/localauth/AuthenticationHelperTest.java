// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.localauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.FragmentActivity;
import io.flutter.plugins.localauth.AuthenticationHelper.AuthCompletionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

// TODO(stuartmorgan): Add injectable BiometricPrompt factory, and AlertDialog factor, and add
// testing of the rest of the flows.

@RunWith(RobolectricTestRunner.class)
public class AuthenticationHelperTest {
  static final AuthStrings dummyStrings =
      new AuthStrings("a reason", "a hint", "cancel", "sign in");

  static final AuthOptions defaultOptions =
      new AuthOptions(
          /* biometricOnly */ false, /* sensitiveTransaction */ false, /* sticky */ false);

  @Test
  public void onAuthenticationError_returnsUserCanceled() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_USER_CANCELED, "");

    verify(handler).complete(new AuthResult(AuthResultCode.USER_CANCELED, ""));
  }

  @Test
  public void onAuthenticationError_returnsNegativeButton() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_NEGATIVE_BUTTON, "");

    verify(handler).complete(new AuthResult(AuthResultCode.NEGATIVE_BUTTON, ""));
  }

  @Test
  public void onAuthenticationError_withoutDialogs_returnsNoCredential() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL, "");

    verify(handler).complete(new AuthResult(AuthResultCode.NO_CREDENTIALS, ""));
  }

  @Test
  public void onAuthenticationError_withoutDialogs_returnsNotEnrolledForNoBiometrics() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_NO_BIOMETRICS, "");

    verify(handler).complete(new AuthResult(AuthResultCode.NOT_ENROLLED, ""));
  }

  @Test
  public void onAuthenticationError_returnsHardwareUnavailable() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "");

    verify(handler).complete(new AuthResult(AuthResultCode.HARDWARE_UNAVAILABLE, ""));
  }

  @Test
  public void onAuthenticationError_returnsHardwareNotPresent() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_HW_NOT_PRESENT, "");

    verify(handler).complete(new AuthResult(AuthResultCode.NO_HARDWARE, ""));
  }

  @Test
  public void onAuthenticationError_returnsTemporaryLockoutForLockout() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_LOCKOUT, "");

    verify(handler).complete(new AuthResult(AuthResultCode.LOCKED_OUT_TEMPORARILY, ""));
  }

  @Test
  public void onAuthenticationError_returnsPermanentLockoutForLockoutPermanent() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_LOCKOUT_PERMANENT, "");

    verify(handler).complete(new AuthResult(AuthResultCode.LOCKED_OUT_PERMANENTLY, ""));
  }

  @Test
  public void onAuthenticationError_withoutSticky_returnsSystemCanceled() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "");

    verify(handler).complete(new AuthResult(AuthResultCode.SYSTEM_CANCELED, ""));
  }

  @Test
  public void onAuthenticationError_returnsTimeout() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_TIMEOUT, "");

    verify(handler).complete(new AuthResult(AuthResultCode.TIMEOUT, ""));
  }

  @Test
  public void onAuthenticationError_returnsNoSpace() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_NO_SPACE, "");

    verify(handler).complete(new AuthResult(AuthResultCode.NO_SPACE, ""));
  }

  @Test
  public void onAuthenticationError_returnsSecurityUpdateRequired() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED, "");

    verify(handler).complete(new AuthResult(AuthResultCode.SECURITY_UPDATE_REQUIRED, ""));
  }

  @Test
  public void onAuthenticationError_returnsUnknownForOtherCases() {
    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            buildMockActivityWithContext(mock(FragmentActivity.class)),
            defaultOptions,
            dummyStrings,
            handler,
            true);

    helper.onAuthenticationError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "");

    verify(handler).complete(new AuthResult(AuthResultCode.UNKNOWN_ERROR, ""));
  }

  @Test
  @org.robolectric.annotation.Config(sdk = 27)
  @SuppressWarnings("deprecation")
  public void authenticate_withNonAppCompatTheme_reproducesCrash() {
    final FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class).create().start().resume().get();
    activity.setTheme(android.R.style.Theme_Black_NoTitleBar); // Non-AppCompat theme

    final AuthCompletionHandler handler = mock(AuthCompletionHandler.class);
    final AuthenticationHelper helper =
        new AuthenticationHelper(
            null,
            activity,
            defaultOptions,
            dummyStrings,
            handler,
            false);

    // Mock FingerprintManagerCompat to simulate hardware presence and enrollment
    final FingerprintManagerCompat mockCompat = mock(FingerprintManagerCompat.class);
    when(mockCompat.isHardwareDetected()).thenReturn(true);
    when(mockCompat.hasEnrolledFingerprints()).thenReturn(true);

    try (MockedStatic<FingerprintManagerCompat> staticMock = Mockito.mockStatic(FingerprintManagerCompat.class)) {
      staticMock.when(() -> FingerprintManagerCompat.from(any(Context.class))).thenReturn(mockCompat);

      System.out.println("SDK: " + android.os.Build.VERSION.SDK_INT);
      System.out.println("Activity FP hardware via Compat: " + FingerprintManagerCompat.from(activity).isHardwareDetected());
      System.out.println("BiometricManager canAuthenticate: " + androidx.biometric.BiometricManager.from(activity).canAuthenticate());

      // This is expected to throw IllegalStateException: You need to use a Theme.AppCompat theme...
      try {
        helper.authenticate();
      } catch (IllegalStateException e) {
        System.out.println("CAUGHT EXCEPTION: " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
    }

    System.out.println("Fragments before idle: " + activity.getSupportFragmentManager().getFragments());
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
    System.out.println("Fragments after idle: " + activity.getSupportFragmentManager().getFragments());

    boolean dialogShown = false;
    for (androidx.fragment.app.Fragment f : activity.getSupportFragmentManager().getFragments()) {
      if (f.getClass().getName().equals("androidx.biometric.FingerprintDialogFragment")) {
        dialogShown = true;
      }
    }
    org.junit.Assert.assertTrue(dialogShown);

    helper.stopAuthentication();
  }

  private FragmentActivity buildMockActivityWithContext(FragmentActivity mockActivity) {
    final Application mockApplication = mock(Application.class);
    final Context mockContext = mock(Context.class);
    when(mockActivity.getBaseContext()).thenReturn(mockContext);
    when(mockActivity.getApplicationContext()).thenReturn(mockContext);
    when(mockActivity.getApplication()).thenReturn(mockApplication);
    return mockActivity;
  }
}
