// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webview_flutter_android/src/legacy/webview_android.dart';
import 'package:webview_flutter_platform_interface/src/webview_flutter_platform_interface_legacy.dart';

class _TestWebViewPlatformCallbacksHandler
    implements WebViewPlatformCallbacksHandler {
  @override
  FutureOr<bool> onNavigationRequest({
    required String url,
    required bool isForMainFrame,
  }) {
    throw UnimplementedError();
  }

  @override
  void onPageFinished(String url) {}

  @override
  void onPageStarted(String url) {}

  @override
  void onProgress(int progress) {}

  @override
  void onWebResourceError(WebResourceError error) {}
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Issue #70839: long tap to stop scroll fling does not work on Android',
      () {
    testWidgets(
        'legacy API: AndroidWebView should not intercept long press gestures with GestureDetector (which prevents stopping scroll fling)',
        (WidgetTester tester) async {
      await tester.pumpWidget(Builder(builder: (BuildContext context) {
        return AndroidWebView().build(
          context: context,
          creationParams: CreationParams(
            webSettings: WebSettings(
              userAgent: const WebSetting<String?>.absent(),
              hasNavigationDelegate: false,
            ),
          ),
          javascriptChannelRegistry: JavascriptChannelRegistry(null),
          webViewPlatformCallbacksHandler:
              _TestWebViewPlatformCallbacksHandler(),
        );
      }));
      await tester.pumpAndSettle();

      // Find the GestureDetector wrapping the AndroidView.
      final Finder gestureDetectorFinder = find.ancestor(
        of: find.byType(AndroidView),
        matching: find.byType(GestureDetector),
      );

      // In issue #70839, long tap to stop scroll fling does not work on Android because
      // AndroidWebView wraps AndroidView in a stop-gap GestureDetector(onLongPress: () {})
      // that claims long press gestures in the gesture arena, preventing pointer events
      // from reaching the native WebView.
      // If fixed, AndroidWebView should not wrap AndroidView in a GestureDetector that
      // intercepts long press events (onLongPress should be null or no GestureDetector should wrap AndroidView).
      if (gestureDetectorFinder.evaluate().isNotEmpty) {
        final GestureDetector gestureDetector =
            tester.widget<GestureDetector>(gestureDetectorFinder);
        expect(
          gestureDetector.onLongPress,
          isNull,
          reason:
              'onLongPress must be null so that long press gestures can reach the native WebView to stop scroll fling (issue #70839)',
        );
      } else {
        expect(gestureDetectorFinder, findsNothing);
      }
    });
  });
}
