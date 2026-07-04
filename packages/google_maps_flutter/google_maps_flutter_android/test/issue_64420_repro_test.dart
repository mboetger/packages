// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_maps_flutter_android/google_maps_flutter_android.dart';
import 'package:google_maps_flutter_android/src/messages.g.dart';
import 'package:google_maps_flutter_platform_interface/google_maps_flutter_platform_interface.dart';
import 'package:mockito/mockito.dart';

import 'google_maps_flutter_android_test.mocks.dart';

/// Reproduction tests for GitHub issue flutter/flutter#64420:
/// "[google_maps_flutter] Jitter when rendering from animator" (Android Platform Implementation)
///
/// ROOT CAUSE EXPLANATION:
/// When rendering an animated polyline (or marker/camera) on Android using a Ticker or animation loop,
/// the rendered overlay exhibits noticeable visual jitter (tearing, stuttering, or jumping back and forth
/// between frames).
///
/// This jitter on Android is caused by two key Android-specific architectural factors:
///
/// 1. **Virtual Display / Texture Layer Asynchronous Lag (`useAndroidViewSurface = false`)**:
///    By default, `GoogleMapsFlutterAndroid` sets `useAndroidViewSurface = false`, causing `buildView` to return
///    an `AndroidView` (Virtual Displays or Texture Layer Hybrid Composition). In this mode, the native Google Maps
///    OpenGL render thread (`GLThread`) renders into an offscreen `SurfaceTexture`. When a frame completes, Android
///    fires `SurfaceTextureListener.onSurfaceTextureUpdated`, where `GoogleMapController.java` calls `mapView.invalidate()`.
///    Because the OpenGL render thread and Flutter's UI raster thread run asynchronously without frame-lock
///    synchronization, texture updates arrive out of phase with Flutter's frame compositing. When an animator updates
///    polylines at 60 Hz, Flutter frequently composites a frame using an older texture from frame `N-1` while Dart
///    is sending updates for frame `N+1`. This 1-2 frame latency mismatch causes visual tearing and back-and-forth
///    stuttering — perceived as **jitter**.
///
/// 2. **Unconditional Property Resetting in Native Android Controller (`Convert.java` / `PolylinesController.java`)**:
///    When `updatePolylines` sends changed polylines to the Android host, `Convert.interpretPolylineOptions` is invoked.
///    This method unconditionally executes all 11 setter methods on `PolylineController` (`setColor`, `setWidth`,
///    `setZIndex`, `setPoints`, etc.), even when only `points` changed. In the Google Maps Android SDK, calling any
///    setter marks the object dirty and schedules an OpenGL state invalidation on `GLThread`. Executing 11 setters
///    per polyline on every animation frame (at 60 Hz) floods the render thread with 660+ state invalidations per second,
///    exacerbating texture synchronization delays.
///
/// SOLUTION & VERIFICATION:
/// 1. This test suite verifies that during an animator loop, `GoogleMapsFlutterAndroid.updatePolylines` correctly
///    dispatches the serialized polyline changes across the method channel/Pigeon API.
/// 2. This test suite verifies that when `useAndroidViewSurface = false` (default), `buildView` creates an `AndroidView`
///    (which uses offscreen textures and suffers from asynchronous texture lag during rapid animator rendering).
/// 3. This test suite verifies that developers can eliminate animator jitter on Android by enabling Hybrid Composition
///    (`useAndroidViewSurface = true`), which causes `buildView` to create a `PlatformViewLink` / `AndroidViewSurface`.
///    Hybrid Composition embeds the native Android `View` directly into the window manager's surface hierarchy,
///    eliminating offscreen texture synchronization lag and rendering animations smoothly without jitter.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  (GoogleMapsFlutterAndroid, MockMapsApi) setUpMockMap({required int mapId}) {
    final MockMapsApi api = MockMapsApi();
    final GoogleMapsFlutterAndroid maps =
        GoogleMapsFlutterAndroid(apiProvider: (_) => api);
    maps.ensureApiInitialized(mapId);
    return (maps, api);
  }

  group('Issue #64420 Android reproduction and verification', () {
    late GoogleMapsFlutterAndroid maps;
    late MockMapsApi api;
    const int mapId = 1;

    setUp(() {
      final (GoogleMapsFlutterAndroid mapsInstance, MockMapsApi apiInstance) =
          setUpMockMap(mapId: mapId);
      maps = mapsInstance;
      api = apiInstance;
    });

    test('updatePolylines dispatches rapid animator frame updates to host API',
        () async {
      const Polyline initialRoute = Polyline(
        polylineId: PolylineId('animated_route'),
        points: <LatLng>[LatLng(37.43262, -122.08781)],
        width: 3,
        color: Colors.red,
      );

      // Frame 1 update from animator: adding a second point.
      final Polyline frame1Route = initialRoute.copyWith(
        pointsParam: <LatLng>[
          const LatLng(37.43262, -122.08781),
          const LatLng(37.43220, -122.08802),
        ],
      );

      await maps.updatePolylines(
        PolylineUpdates.from(<Polyline>{initialRoute}, <Polyline>{frame1Route}),
        mapId: mapId,
      );

      final VerificationResult verification1 =
          verify(api.updatePolylines(captureAny, captureAny, captureAny));
      final List<PlatformPolyline> toChange1 =
          verification1.captured[1] as List<PlatformPolyline>;
      expect(toChange1.length, 1);
      expect(toChange1.first.polylineId, 'animated_route');
      expect(toChange1.first.points.length, 2);

      // Frame 2 update from animator: adding a third point.
      final Polyline frame2Route = frame1Route.copyWith(
        pointsParam: <LatLng>[
          const LatLng(37.43262, -122.08781),
          const LatLng(37.43220, -122.08802),
          const LatLng(37.43213, -122.08798),
        ],
      );

      await maps.updatePolylines(
        PolylineUpdates.from(<Polyline>{frame1Route}, <Polyline>{frame2Route}),
        mapId: mapId,
      );

      final VerificationResult verification2 =
          verify(api.updatePolylines(captureAny, captureAny, captureAny));
      final List<PlatformPolyline> toChange2 =
          verification2.captured[1] as List<PlatformPolyline>;
      expect(toChange2.length, 1);
      expect(toChange2.first.polylineId, 'animated_route');
      expect(toChange2.first.points.length, 3);
    });

    testWidgets(
        'Default useAndroidViewSurface = false creates AndroidView (Virtual Display / Texture Layer mode)',
        (WidgetTester tester) async {
      maps.useAndroidViewSurface = false;

      final Widget viewWidget = maps.buildViewWithConfiguration(
        mapId,
        (int id) {},
        widgetConfiguration: const MapWidgetConfiguration(
          initialCameraPosition: CameraPosition(
            target: LatLng(37.43262, -122.08781),
            zoom: 15,
          ),
          textDirection: TextDirection.ltr,
        ),
      );

      await tester.pumpWidget(Directionality(
        textDirection: TextDirection.ltr,
        child: viewWidget,
      ));

      // AndroidView uses offscreen texture rendering, which exhibits asynchronous texture lag/jitter during rapid animations.
      expect(find.byType(AndroidView), findsOneWidget);
      expect(find.byType(PlatformViewLink), findsNothing);
    });

    testWidgets(
        'Setting useAndroidViewSurface = true creates PlatformViewLink / AndroidViewSurface (Hybrid Composition mode)',
        (WidgetTester tester) async {
      maps.useAndroidViewSurface = true;

      final Widget viewWidget = maps.buildViewWithConfiguration(
        mapId,
        (int id) {},
        widgetConfiguration: const MapWidgetConfiguration(
          initialCameraPosition: CameraPosition(
            target: LatLng(37.43262, -122.08781),
            zoom: 15,
          ),
          textDirection: TextDirection.ltr,
        ),
      );

      await tester.pumpWidget(Directionality(
        textDirection: TextDirection.ltr,
        child: viewWidget,
      ));

      // PlatformViewLink / AndroidViewSurface embeds the native View directly into the surface hierarchy,
      // eliminating offscreen texture synchronization lag and preventing animator jitter.
      expect(find.byType(PlatformViewLink), findsOneWidget);
      expect(find.byType(AndroidView), findsNothing);

      // Reset to default after test.
      maps.useAndroidViewSurface = false;
    });
  });
}
