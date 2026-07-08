// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter_android/src/android_webkit.g.dart' as android_webview;
import 'package:webview_flutter_android/webview_flutter_android.dart';

import 'android_webview_controller_test.mocks.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    android_webview.PigeonOverrides.pigeon_reset();
  });

  test('AndroidWebViewController supports setNetworkAvailable', () async {
    android_webview.PigeonOverrides.webView_new =
        ({
          dynamic Function(android_webview.WebView, int left, int top, int oldLeft, int oldTop)?
          onScrollChanged,
        }) => MockWebView();
    android_webview.PigeonOverrides.webChromeClient_new =
        ({
          void Function(android_webview.WebChromeClient, android_webview.WebView, int)?
          onProgressChanged,
          Future<List<String>> Function(
            android_webview.WebChromeClient,
            android_webview.WebView,
            android_webview.FileChooserParams,
          )?
          onShowFileChooser,
          void Function(android_webview.WebChromeClient, android_webview.PermissionRequest)?
          onPermissionRequest,
          void Function(
            android_webview.WebChromeClient,
            android_webview.View,
            android_webview.CustomViewCallback,
          )?
          onShowCustomView,
          void Function(android_webview.WebChromeClient)? onHideCustomView,
          void Function(
            android_webview.WebChromeClient,
            String,
            android_webview.GeolocationPermissionsCallback,
          )?
          onGeolocationPermissionsShowPrompt,
          void Function(android_webview.WebChromeClient)? onGeolocationPermissionsHidePrompt,
          void Function(android_webview.WebChromeClient, android_webview.ConsoleMessage)?
          onConsoleMessage,
          Future<void> Function(
            android_webview.WebChromeClient,
            android_webview.WebView,
            String,
            String,
          )?
          onJsAlert,
          Future<bool> Function(
            android_webview.WebChromeClient,
            android_webview.WebView,
            String,
            String,
          )?
          onJsConfirm,
          Future<String?> Function(
            android_webview.WebChromeClient,
            android_webview.WebView,
            String,
            String,
            String,
          )?
          onJsPrompt,
        }) => MockWebChromeClient();

    final androidWebView = MockWebView();
    when(androidWebView.settings).thenReturn(MockWebSettings());

    final creationParams = AndroidWebViewControllerCreationParams(
      androidWebStorage: MockWebStorage(),
    );

    android_webview.PigeonOverrides.webView_new =
        ({
          dynamic Function(android_webview.WebView, int left, int top, int oldLeft, int oldTop)?
          onScrollChanged,
        }) => androidWebView;

    final controller = AndroidWebViewController(creationParams);

    await controller.setNetworkAvailable(true);

    verify(androidWebView.setNetworkAvailable(true)).called(1);
  });
}
