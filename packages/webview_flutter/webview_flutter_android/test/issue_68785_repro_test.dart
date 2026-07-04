// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter_android/src/android_proxy.dart';
import 'package:webview_flutter_android/src/legacy/webview_android_widget.dart';
import 'package:webview_flutter_android/webview_flutter_android.dart';
import 'package:webview_flutter_platform_interface/src/webview_flutter_platform_interface_legacy.dart';
import 'package:webview_flutter_platform_interface/webview_flutter_platform_interface.dart';

import 'android_navigation_delegate_test.dart';
import 'android_webview_controller_test.mocks.dart';
import 'legacy/webview_android_widget_test.mocks.dart' as legacy_mocks;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Issue #68785: NavigationDecision.prevent not working on Android', () {
    test(
        'modern API: loadRequest ignores NavigationDecision.prevent from navigationDelegate',
        () async {
      final MockWebView mockWebView = MockWebView();
      when(mockWebView.settings).thenReturn(MockWebSettings());

      final AndroidWebViewController controller =
          AndroidWebViewController(AndroidWebViewControllerCreationParams(
        androidWebStorage: MockWebStorage(),
        androidWebViewProxy: AndroidWebViewProxy(
          newWebChromeClient: CapturingWebChromeClient.new,
          newWebView: ({dynamic onScrollChanged}) => mockWebView,
          newWebViewClient: CapturingWebViewClient.new,
          newDownloadListener: CapturingDownloadListener.new,
          instanceWebStorage: () => MockWebStorage(),
        ),
      ));

      final AndroidNavigationDelegate delegate = AndroidNavigationDelegate(
        AndroidNavigationDelegateCreationParams
            .fromPlatformNavigationDelegateCreationParams(
          const PlatformNavigationDelegateCreationParams(),
          androidWebViewProxy: const AndroidWebViewProxy(
            newWebChromeClient: CapturingWebChromeClient.new,
            newWebViewClient: CapturingWebViewClient.new,
            newDownloadListener: CapturingDownloadListener.new,
          ),
        ),
      );
      await delegate.setOnNavigationRequest((NavigationRequest request) {
        return NavigationDecision.prevent;
      });
      await controller.setPlatformNavigationDelegate(delegate);

      await controller.loadRequest(LoadRequestParams(
        uri: Uri.parse('https://flutter.dev'),
      ));

      // Because programmatic loads via loadRequest directly call _webView.loadUrl
      // without invoking the navigation delegate (and Android WebView does not
      // trigger shouldOverrideUrlLoading for loadUrl), NavigationDecision.prevent
      // is ignored and loadUrl is executed.
      verifyNever(mockWebView.loadUrl(any, any));
    });

    testWidgets(
        'legacy API: initialUrl ignores NavigationDecision.prevent from navigationDelegate',
        (WidgetTester tester) async {
      final legacy_mocks.MockFlutterAssetManager mockFlutterAssetManager =
          legacy_mocks.MockFlutterAssetManager();
      final legacy_mocks.MockWebView mockWebView = legacy_mocks.MockWebView();
      final legacy_mocks.MockWebSettings mockWebSettings =
          legacy_mocks.MockWebSettings();
      final legacy_mocks.MockWebStorage mockWebStorage =
          legacy_mocks.MockWebStorage();
      final legacy_mocks.MockWebViewClient mockWebViewClient =
          legacy_mocks.MockWebViewClient();
      when(mockWebView.settings).thenReturn(mockWebSettings);

      final legacy_mocks.MockWebViewProxy mockWebViewProxy =
          legacy_mocks.MockWebViewProxy();
      when(mockWebViewProxy.createWebView()).thenReturn(mockWebView);
      when(mockWebViewProxy.createWebViewClient(
        onPageStarted: anyNamed('onPageStarted'),
        onPageFinished: anyNamed('onPageFinished'),
        onReceivedError: anyNamed('onReceivedError'),
        onReceivedRequestError: anyNamed('onReceivedRequestError'),
        requestLoading: anyNamed('requestLoading'),
        urlLoading: anyNamed('urlLoading'),
        onReceivedSslError: anyNamed('onReceivedSslError'),
        onFormResubmission: anyNamed('onFormResubmission'),
        onReceivedClientCertRequest: anyNamed('onReceivedClientCertRequest'),
      )).thenReturn(mockWebViewClient);

      final legacy_mocks.MockWebViewPlatformCallbacksHandler
          mockCallbacksHandler =
          legacy_mocks.MockWebViewPlatformCallbacksHandler();
      when(mockCallbacksHandler.onNavigationRequest(
        isForMainFrame: anyNamed('isForMainFrame'),
        url: anyNamed('url'),
      )).thenReturn(false); // NavigationDecision.prevent

      final legacy_mocks.MockJavascriptChannelRegistry
          mockJavascriptChannelRegistry =
          legacy_mocks.MockJavascriptChannelRegistry();

      await tester.pumpWidget(WebViewAndroidWidget(
        creationParams: CreationParams(
          initialUrl: 'https://flutter.dev',
          webSettings: WebSettings(
            userAgent: const WebSetting<String?>.absent(),
            hasNavigationDelegate: true,
          ),
        ),
        callbacksHandler: mockCallbacksHandler,
        javascriptChannelRegistry: mockJavascriptChannelRegistry,
        webViewProxy: mockWebViewProxy,
        flutterAssetManager: mockFlutterAssetManager,
        webStorage: mockWebStorage,
        onBuildWidget: (WebViewAndroidPlatformController controller) {
          return Container();
        },
      ));

      // In the legacy implementation, passing initialUrl to WebView directly calls
      // webView.loadUrl without checking onNavigationRequest on Android, causing
      // NavigationDecision.prevent to be ignored.
      verifyNever(mockWebView.loadUrl(any, any));
    });
  });
}
