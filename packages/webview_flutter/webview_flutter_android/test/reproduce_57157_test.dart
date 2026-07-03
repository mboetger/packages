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

  AndroidWebViewController createControllerWithMocks({
    android_webview.FlutterAssetManager? mockFlutterAssetManager,
    android_webview.JavaScriptChannel? mockJavaScriptChannel,
    android_webview.WebChromeClient Function({
      void Function(android_webview.WebChromeClient, android_webview.WebView, int)?
      onProgressChanged,
      required Future<List<String>> Function(
        android_webview.WebChromeClient,
        android_webview.WebView,
        android_webview.FileChooserParams,
      )
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
      required Future<bool> Function(
        android_webview.WebChromeClient,
        android_webview.WebView,
        String,
        String,
      )
      onJsConfirm,
      Future<String?> Function(
        android_webview.WebChromeClient,
        android_webview.WebView,
        String,
        String,
        String,
      )?
      onJsPrompt,
    })?
    createWebChromeClient,
    android_webview.WebView? mockWebView,
    android_webview.WebViewClient? mockWebViewClient,
    android_webview.WebStorage? mockWebStorage,
    android_webview.WebSettings? mockSettings,
    Future<bool> Function(String)? isWebViewFeatureSupported,
    Future<void> Function(android_webview.WebSettings, bool)? setPaymentRequestEnabled,
  }) {
    final android_webview.WebView nonNullMockWebView = mockWebView ?? MockWebView();

    android_webview.PigeonOverrides.webChromeClient_new =
        createWebChromeClient ??
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
    android_webview.PigeonOverrides.webView_new =
        ({
          dynamic Function(android_webview.WebView, int left, int top, int oldLeft, int oldTop)?
          onScrollChanged,
        }) => nonNullMockWebView;
    android_webview.PigeonOverrides.webViewClient_new =
        ({
          void Function(android_webview.WebViewClient, android_webview.WebView, String)?
          onPageStarted,
          void Function(android_webview.WebViewClient, android_webview.WebView, String)?
          onPageFinished,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.WebResourceRequest,
            android_webview.WebResourceResponse,
          )?
          onReceivedHttpError,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.WebResourceRequest,
            android_webview.WebResourceError,
          )?
          onReceivedRequestError,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.WebResourceRequest,
            android_webview.WebResourceErrorCompat,
          )?
          onReceivedRequestErrorCompat,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            int,
            String,
            String,
          )?
          onReceivedError,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.WebResourceRequest,
          )?
          requestLoading,
          void Function(android_webview.WebViewClient, android_webview.WebView, String)? urlLoading,
          void Function(android_webview.WebViewClient, android_webview.WebView, String, bool)?
          doUpdateVisitedHistory,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.HttpAuthHandler,
            String,
            String,
          )?
          onReceivedHttpAuthRequest,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.AndroidMessage,
            android_webview.AndroidMessage,
          )?
          onFormResubmission,
          void Function(android_webview.WebViewClient, android_webview.WebView, String)?
          onLoadResource,
          void Function(android_webview.WebViewClient, android_webview.WebView, String)?
          onPageCommitVisible,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.ClientCertRequest,
          )?
          onReceivedClientCertRequest,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            String,
            String,
            String,
          )?
          onReceivedLoginRequest,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.SslErrorHandler,
            android_webview.SslError,
          )?
          onReceivedSslError,
          void Function(android_webview.WebViewClient, android_webview.WebView, double, double)?
          onScaleChanged,
        }) => mockWebViewClient ?? MockWebViewClient();
    android_webview.PigeonOverrides.flutterAssetManager_instance =
        mockFlutterAssetManager ?? MockFlutterAssetManager();
    android_webview.PigeonOverrides.javaScriptChannel_new =
        ({
          required String channelName,
          required void Function(android_webview.JavaScriptChannel, String) postMessage,
        }) => mockJavaScriptChannel ?? MockJavaScriptChannel();
    android_webview.PigeonOverrides.webViewFeature_isFeatureSupported =
        isWebViewFeatureSupported ?? (_) async => false;
    android_webview.PigeonOverrides.webSettingsCompat_setPaymentRequestEnabled =
        setPaymentRequestEnabled ?? (_, _) async {};

    final creationParams = AndroidWebViewControllerCreationParams(
      androidWebStorage: mockWebStorage ?? MockWebStorage(),
    );

    when(nonNullMockWebView.settings).thenReturn(mockSettings ?? MockWebSettings());

    return AndroidWebViewController(creationParams);
  }

  setUp(() {
    android_webview.PigeonOverrides.pigeon_reset();
  });

  group('Reproduction Test for WebSettings.setDomStorageEnabled', () {
    test('verifies setDomStorageEnabled(true) is called on initialization', () {
      final mockWebView = MockWebView();
      final mockWebSettings = MockWebSettings();

      createControllerWithMocks(mockWebView: mockWebView, mockSettings: mockWebSettings);

      // Verify that setDomStorageEnabled(true) was indeed called.
      verify(mockWebSettings.setDomStorageEnabled(true)).called(1);
    });
  });
}
