// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter_android/src/android_proxy.dart';
import 'package:webview_flutter_android/src/android_webkit.g.dart'
    as android_webview;
import 'package:webview_flutter_android/webview_flutter_android.dart';

import 'android_webview_controller_test.mocks.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  AndroidWebViewController createControllerWithMocks({
    android_webview.WebView? mockWebView,
    android_webview.WebStorage? mockWebStorage,
    android_webview.WebSettings? mockSettings,
  }) {
    final android_webview.WebView nonNullMockWebView =
        mockWebView ?? MockWebView();

    final AndroidWebViewControllerCreationParams creationParams =
        AndroidWebViewControllerCreationParams(
      androidWebStorage: mockWebStorage ?? MockWebStorage(),
      androidWebViewProxy: AndroidWebViewProxy(
        newWebChromeClient: ({
          void Function(android_webview.WebChromeClient,
                  android_webview.WebView, int)?
              onProgressChanged,
          Future<List<String>> Function(
            android_webview.WebChromeClient,
            android_webview.WebView,
            android_webview.FileChooserParams,
          )? onShowFileChooser,
          void Function(android_webview.WebChromeClient,
                  android_webview.PermissionRequest)?
              onPermissionRequest,
          void Function(android_webview.WebChromeClient, android_webview.View,
                  android_webview.CustomViewCallback)?
              onShowCustomView,
          void Function(android_webview.WebChromeClient)? onHideCustomView,
          void Function(
            android_webview.WebChromeClient,
            String,
            android_webview.GeolocationPermissionsCallback,
          )? onGeolocationPermissionsShowPrompt,
          void Function(android_webview.WebChromeClient)?
              onGeolocationPermissionsHidePrompt,
          void Function(android_webview.WebChromeClient,
                  android_webview.ConsoleMessage)?
              onConsoleMessage,
          Future<void> Function(android_webview.WebChromeClient,
                  android_webview.WebView, String, String)?
              onJsAlert,
          Future<bool> Function(
            android_webview.WebChromeClient,
            android_webview.WebView,
            String,
            String,
          )? onJsConfirm,
          Future<String?> Function(
            android_webview.WebChromeClient,
            android_webview.WebView,
            String,
            String,
            String,
          )? onJsPrompt,
        }) =>
            MockWebChromeClient(),
        newWebView: (
                {dynamic Function(android_webview.WebView, int left, int top,
                        int oldLeft, int oldTop)?
                    onScrollChanged}) =>
            nonNullMockWebView,
        newWebViewClient: ({
          void Function(android_webview.WebViewClient, android_webview.WebView,
                  String)?
              onPageStarted,
          void Function(android_webview.WebViewClient, android_webview.WebView,
                  String)?
              onPageFinished,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.WebResourceRequest,
            android_webview.WebResourceResponse,
          )? onReceivedHttpError,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.WebResourceRequest,
            android_webview.WebResourceError,
          )? onReceivedRequestError,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.WebResourceRequest,
            android_webview.WebResourceErrorCompat,
          )? onReceivedRequestErrorCompat,
          void Function(android_webview.WebViewClient, android_webview.WebView,
                  int, String, String)?
              onReceivedError,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.WebResourceRequest,
          )? requestLoading,
          void Function(android_webview.WebViewClient, android_webview.WebView,
                  String)?
              urlLoading,
          void Function(android_webview.WebViewClient, android_webview.WebView,
                  String, bool)?
              doUpdateVisitedHistory,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.HttpAuthHandler,
            String,
            String,
          )? onReceivedHttpAuthRequest,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.AndroidMessage,
            android_webview.AndroidMessage,
          )? onFormResubmission,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            String,
          )? onLoadResource,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            String,
          )? onPageCommitVisible,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.ClientCertRequest,
          )? onReceivedClientCertRequest,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            String,
            String,
            String,
          )? onReceivedLoginRequest,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            android_webview.SslErrorHandler,
            android_webview.SslError,
          )? onReceivedSslError,
          void Function(
            android_webview.WebViewClient,
            android_webview.WebView,
            double,
            double,
          )? onScaleChanged,
        }) =>
            MockWebViewClient(),
        instanceFlutterAssetManager: () => MockFlutterAssetManager(),
        newJavaScriptChannel: ({
          required String channelName,
          required void Function(android_webview.JavaScriptChannel, String)
              postMessage,
        }) =>
            MockJavaScriptChannel(),
      ),
    );
    when(nonNullMockWebView.settings)
        .thenReturn(mockSettings ?? MockWebSettings());
    return AndroidWebViewController(creationParams);
  }

  void stubMockSurfaceViewController(
      MockSurfaceAndroidViewController controller) {
    when(controller.viewId).thenReturn(0);
    when(controller.isCreated).thenReturn(true);
    when(controller.requiresViewComposition).thenReturn(true);
    when(controller.awaitingCreation).thenReturn(false);
    when(controller.setSize(any)).thenAnswer((_) async => const Size(500, 500));
    when(controller.dispatchPointerEvent(any)).thenAnswer((_) async {});
    when(controller.setOffset(any)).thenAnswer((_) async {});
    when(controller.addOnPlatformViewCreatedListener(any))
        .thenAnswer((Invocation invocation) {
      final void Function(int) listener =
          invocation.positionalArguments[0] as void Function(int);
      listener(0);
    });
  }

  group(
      'WebView in PageView Text Selection Reproduction (flutter/flutter#70619)',
      () {
    testWidgets(
        'Without gestureRecognizers, PointerDownEvent is withheld from Android WebView in PageView, preventing text selection',
        (WidgetTester tester) async {
      final android_webview.WebView mockWebView = MockWebView();
      final AndroidWebViewController controller = createControllerWithMocks(
        mockWebView: mockWebView,
      );

      final android_webview.PigeonInstanceManager instanceManager =
          android_webview.PigeonInstanceManager(
        onWeakReferenceRemoved: (_) {},
      );
      instanceManager.addDartCreatedInstance(mockWebView);

      final MockPlatformViewsServiceProxy mockPlatformViewsService =
          MockPlatformViewsServiceProxy();
      final MockSurfaceAndroidViewController mockSurfaceViewController =
          MockSurfaceAndroidViewController();
      stubMockSurfaceViewController(mockSurfaceViewController);

      when(
        mockPlatformViewsService.initSurfaceAndroidView(
          id: anyNamed('id'),
          viewType: anyNamed('viewType'),
          layoutDirection: anyNamed('layoutDirection'),
          creationParams: anyNamed('creationParams'),
          creationParamsCodec: anyNamed('creationParamsCodec'),
          onFocus: anyNamed('onFocus'),
        ),
      ).thenReturn(mockSurfaceViewController);

      // Create AndroidWebViewWidget without passing gestureRecognizers (default empty set).
      // This matches the issue report where WebView is embedded in PageView without recognizers.
      final AndroidWebViewWidget webViewWidget = AndroidWebViewWidget(
        AndroidWebViewWidgetCreationParams(
          key: const Key('test_web_view'),
          controller: controller,
          platformViewsServiceProxy: mockPlatformViewsService,
          instanceManager: instanceManager,
        ),
      );

      await tester.pumpWidget(
        Directionality(
          textDirection: TextDirection.ltr,
          child: PageView(
            children: <Widget>[
              Builder(
                builder: (BuildContext context) => webViewWidget.build(context),
              ),
              const Text('Page 2'),
            ],
          ),
        ),
      );
      await tester.pumpAndSettle();

      // Initiate a touch gesture on the WebView (as if pressing down to long-press and select text).
      final TestGesture gesture = await tester.startGesture(
        tester.getCenter(find.byType(PlatformViewLink)),
      );
      await tester.pump(const Duration(milliseconds: 500));

      // Without gestureRecognizers on the WebView, the PageView's HorizontalDragGestureRecognizer
      // competes in the gesture arena. While unresolved, pointer events are cached in Dart and
      // withheld from the native Android view.
      //
      // In order for text selection to work, the native WebView must receive ACTION_DOWN immediately
      // upon touch down. When this assertion runs against the unfixed code (empty gestureRecognizers),
      // it fails because dispatchPointerEvent was never called (0 calls instead of 1).
      verify(mockSurfaceViewController.dispatchPointerEvent(any)).called(1);

      await gesture.up();
    });

    testWidgets(
        'With EagerGestureRecognizer (fix/workaround), PointerDownEvent is immediately dispatched to Android WebView in PageView',
        (WidgetTester tester) async {
      final android_webview.WebView mockWebView = MockWebView();
      final AndroidWebViewController controller = createControllerWithMocks(
        mockWebView: mockWebView,
      );

      final android_webview.PigeonInstanceManager instanceManager =
          android_webview.PigeonInstanceManager(
        onWeakReferenceRemoved: (_) {},
      );
      instanceManager.addDartCreatedInstance(mockWebView);

      final MockPlatformViewsServiceProxy mockPlatformViewsService =
          MockPlatformViewsServiceProxy();
      final MockSurfaceAndroidViewController mockSurfaceViewController =
          MockSurfaceAndroidViewController();
      stubMockSurfaceViewController(mockSurfaceViewController);

      when(
        mockPlatformViewsService.initSurfaceAndroidView(
          id: anyNamed('id'),
          viewType: anyNamed('viewType'),
          layoutDirection: anyNamed('layoutDirection'),
          creationParams: anyNamed('creationParams'),
          creationParamsCodec: anyNamed('creationParamsCodec'),
          onFocus: anyNamed('onFocus'),
        ),
      ).thenReturn(mockSurfaceViewController);

      // Provide EagerGestureRecognizer so the WebView claims touches immediately in the arena.
      final AndroidWebViewWidget webViewWidget = AndroidWebViewWidget(
        AndroidWebViewWidgetCreationParams(
          key: const Key('test_web_view_eager'),
          controller: controller,
          platformViewsServiceProxy: mockPlatformViewsService,
          instanceManager: instanceManager,
          gestureRecognizers: <Factory<OneSequenceGestureRecognizer>>{
            Factory<OneSequenceGestureRecognizer>(
                () => EagerGestureRecognizer()),
          },
        ),
      );

      await tester.pumpWidget(
        Directionality(
          textDirection: TextDirection.ltr,
          child: PageView(
            children: <Widget>[
              Builder(
                builder: (BuildContext context) => webViewWidget.build(context),
              ),
              const Text('Page 2'),
            ],
          ),
        ),
      );
      await tester.pumpAndSettle();

      final TestGesture gesture = await tester.startGesture(
        tester.getCenter(find.byType(PlatformViewLink)),
      );
      await tester.pump(const Duration(milliseconds: 500));

      // Because EagerGestureRecognizer won the arena immediately upon touch down,
      // dispatchPointerEvent IS called immediately, allowing text selection and dragging to work!
      verify(mockSurfaceViewController.dispatchPointerEvent(any)).called(1);

      await gesture.up();
    });
  });
}
