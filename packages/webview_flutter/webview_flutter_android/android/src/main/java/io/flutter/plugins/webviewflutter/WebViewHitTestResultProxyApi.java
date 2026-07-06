// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ProxyApi implementation for {@link WebView.HitTestResult}. This class may handle instantiating
 * native object instances that are attached to a Dart instance or handle method calls on the
 * associated native class or an instance of that class.
 */
class WebViewHitTestResultProxyApi extends PigeonApiWebViewHitTestResult {
  WebViewHitTestResultProxyApi(@NonNull ProxyApiRegistrar pigeonRegistrar) {
    super(pigeonRegistrar);
  }

  @NonNull
  @Override
  public ProxyApiRegistrar getPigeonRegistrar() {
    return (ProxyApiRegistrar) super.getPigeonRegistrar();
  }

  @NonNull
  @Override
  public WebViewHitTestResultType getType(@NonNull WebView.HitTestResult pigeon_instance) {
    switch (pigeon_instance.getType()) {
      case WebView.HitTestResult.ANCHOR_TYPE:
        return WebViewHitTestResultType.ANCHOR;
      case WebView.HitTestResult.PHONE_TYPE:
        return WebViewHitTestResultType.PHONE;
      case WebView.HitTestResult.GEO_TYPE:
        return WebViewHitTestResultType.GEO;
      case WebView.HitTestResult.EMAIL_TYPE:
        return WebViewHitTestResultType.EMAIL;
      case WebView.HitTestResult.IMAGE_TYPE:
        return WebViewHitTestResultType.IMAGE;
      case WebView.HitTestResult.IMAGE_ANCHOR_TYPE:
        return WebViewHitTestResultType.IMAGE_ANCHOR;
      case WebView.HitTestResult.SRC_ANCHOR_TYPE:
        return WebViewHitTestResultType.SRC_ANCHOR;
      case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
        return WebViewHitTestResultType.SRC_IMAGE_ANCHOR;
      case WebView.HitTestResult.EDIT_TEXT_TYPE:
        return WebViewHitTestResultType.EDIT_TEXT;
      case WebView.HitTestResult.UNKNOWN_TYPE:
      default:
        return WebViewHitTestResultType.UNKNOWN;
    }
  }

  @Nullable
  @Override
  public String getExtra(@NonNull WebView.HitTestResult pigeon_instance) {
    return pigeon_instance.getExtra();
  }
}
