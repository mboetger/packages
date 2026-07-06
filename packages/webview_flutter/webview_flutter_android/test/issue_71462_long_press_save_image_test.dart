// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
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
    android_webview.WebView? mockWebView,
    android_webview.WebViewClient? mockWebViewClient,
    android_webview.WebStorage? mockWebStorage,
    android_webview.WebSettings? mockSettings,
    Future<bool> Function(String)? isWebViewFeatureSupported,
    Future<void> Function(android_webview.WebSettings, bool)? setPaymentRequestEnabled,
  }) {
    final android_webview.WebView nonNullMockWebView = mockWebView ?? MockWebView();

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
    android_webview.PigeonOverrides.webView_new =
        ({
          dynamic Function(android_webview.WebView, int left, int top, int oldLeft, int oldTop)?
          onScrollChanged,
          void Function(android_webview.WebView, String)? onLongPressImage,
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

  group('Issue flutter/flutter#71462 - Long press save image on Android', () {
    test(
      'AndroidWebViewController does not support getHitTestResult or image long press callbacks',
      () async {
        final AndroidWebViewController controller = createControllerWithMocks();

        // On Android, enabling long-press save image requires inspecting the hit test result
        // of the WebView when a long-click or context menu event occurs (e.g. via WebView.getHitTestResult()
        // returning IMAGE_TYPE or SRC_IMAGE_ANCHOR_TYPE with the image URL).
        // Currently, AndroidWebViewController and the underlying Pigeon API do not expose
        // getHitTestResult or a long press image save callback.
        try {
          await (controller as dynamic).getHitTestResult();
        } on NoSuchMethodError {
          fail(
            'AndroidWebViewController fails to support long press save image: '
            'getHitTestResult / long press image callback is not implemented (flutter/flutter#71462).',
          );
        }
      },
    );

    testWidgets(
      'AndroidWebViewWidget with Hybrid Composition enabled does not support long press save image',
      (WidgetTester tester) async {
        final mockWebView = MockWebView();
        final AndroidWebViewController controller = createControllerWithMocks(
          mockWebView: mockWebView,
        );

        android_webview.PigeonInstanceManager.instance.addDartCreatedInstance(mockWebView);

        final mockPlatformViewsService = MockPlatformViewsServiceProxy();

        when(
          mockPlatformViewsService.initExpensiveAndroidView(
            id: anyNamed('id'),
            viewType: anyNamed('viewType'),
            layoutDirection: anyNamed('layoutDirection'),
            creationParams: anyNamed('creationParams'),
            creationParamsCodec: anyNamed('creationParamsCodec'),
            onFocus: anyNamed('onFocus'),
          ),
        ).thenReturn(MockExpensiveAndroidViewController());

        final webViewWidget = AndroidWebViewWidget(
          AndroidWebViewWidgetCreationParams(
            key: const Key('test_web_view'),
            controller: controller,
            platformViewsServiceProxy: mockPlatformViewsService,
            displayWithHybridComposition: true,
          ),
        );

        await tester.pumpWidget(
          Builder(builder: (BuildContext context) => webViewWidget.build(context)),
        );
        await tester.pumpAndSettle();

        // Verify that Hybrid Composition (expensive Android view) was initialized as requested.
        verify(
          mockPlatformViewsService.initExpensiveAndroidView(
            id: anyNamed('id'),
            viewType: anyNamed('viewType'),
            layoutDirection: anyNamed('layoutDirection'),
            creationParams: anyNamed('creationParams'),
            creationParamsCodec: anyNamed('creationParamsCodec'),
            onFocus: anyNamed('onFocus'),
          ),
        );

        // Even when Hybrid Composition is enabled, the widget and controller lack any API
        // or feature flag to enable long-press save image or context menus on Android.
        try {
          (webViewWidget as dynamic).enableLongPressSaveImage();
        } on NoSuchMethodError {
          fail(
            'AndroidWebViewWidget with Hybrid Composition enabled fails to support long press save image: '
            'no feature flag or callback exists to enable image saving (flutter/flutter#71462).',
          );
        }
      },
    );
  });
}
