// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.webkit.WebView;
import org.junit.Test;

public class WebViewHitTestResultTest {
  @Test
  public void getType() {
    final PigeonApiWebViewHitTestResult api =
        new TestProxyApiRegistrar().getPigeonApiWebViewHitTestResult();

    final WebView.HitTestResult instance = mock(WebView.HitTestResult.class);
    when(instance.getType()).thenReturn(WebView.HitTestResult.IMAGE_TYPE);

    assertEquals(WebViewHitTestResultType.IMAGE, api.getType(instance));
  }

  @Test
  public void getExtra() {
    final PigeonApiWebViewHitTestResult api =
        new TestProxyApiRegistrar().getPigeonApiWebViewHitTestResult();

    final WebView.HitTestResult instance = mock(WebView.HitTestResult.class);
    final String extra = "https://example.com/image.png";
    when(instance.getExtra()).thenReturn(extra);

    assertEquals(extra, api.getExtra(instance));
  }
}
