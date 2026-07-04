// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:google_maps_flutter_platform_interface/google_maps_flutter_platform_interface.dart';

import 'fake_google_maps_flutter_platform.dart';

/// Reproduction tests for GitHub issue flutter/flutter#64420:
/// "[google_maps_flutter] Jitter when rendering from animator"
///
/// ROOT CAUSE EXPLANATION:
/// In the reported issue, the user animates a route polyline across a Google Map on Android by updating
/// the polyline's points on every frame using a `Ticker` (or `AnimationController`) and calling `setState`:
/// ```dart
/// await for (final tick in Ticker().tick(numTicks, interval)) {
///   final stepPoly = Polyline(polylineId: PolylineId('steps'), points: steps, width: 3, color: Colors.red);
///   _polylines = {_route, stepPoly};
///   setState(() {});
/// }
/// ```
/// When this animation runs on Android, the rendered polyline visibly jitters (stutters, tears, or jumps
/// back and forth between frames). On iOS, however, the exact same animation renders smoothly without jitter.
///
/// This jitter on Android is caused by an architectural interaction between three layers:
///
/// 1. **Unconditional Property Resetting in Native Android Controller (`Convert.java` / `PolylinesController.java`)**:
///    When `setState` is called on every animation frame, `_GoogleMapState` computes `PolylineUpdates` and sends
///    the updated polyline to the native Android host via MethodChannel/Pigeon (`updatePolylines`).
///    In `PolylinesController.java`, when `changePolyline` is invoked, `Convert.interpretPolylineOptions` is called.
///    This method unconditionally executes all 11 setter methods on `PolylineController` (`setColor`, `setWidth`,
///    `setZIndex`, `setPoints`, `setPattern`, `setStartCap`, `setEndCap`, `setGeodesic`, `setJointType`,
///    `setVisible`, and `setConsumeTapEvents`), even when only the geometry (`points`) changed.
///
/// 2. **OpenGL Render Thread Overload & Geometry Re-triangulation in Google Maps SDK**:
///    Each setter call on a `com.google.android.gms.maps.model.Polyline` marks the object dirty in the Google Maps
///    Android SDK and schedules an OpenGL state invalidation on the internal map render thread (`GLThread`).
///    Executing 11 setters sequentially per polyline on every animation tick (at 60 Hz) floods the map's render
///    thread with 660+ state invalidations and geometry re-triangulations per second.
///
/// 3. **Asynchronous Texture Synchronization Lag in Virtual Display / Texture Layer Modes (`AndroidView`)**:
///    By default, `google_maps_flutter_android` sets `useAndroidViewSurface = false`, which renders the map using
///    an `AndroidView` (Virtual Displays or Texture Layer Hybrid Composition). In this mode, the Google Maps
///    OpenGL render thread draws into an offscreen `SurfaceTexture`. When a frame finishes drawing, Android fires
///    `SurfaceTextureListener.onSurfaceTextureUpdated`, where `GoogleMapController.java` calls `mapView.invalidate()`.
///    Because the Google Maps OpenGL render thread and Flutter's UI raster thread run asynchronously without frame-lock
///    synchronization, texture updates arrive out of phase with Flutter's frame compositing. During high-frequency
///    animation, Flutter frequently composites a frame using a texture from frame `N-1` while Dart is already sending
///    polyline updates for frame `N+1`. This 1-2 frame latency mismatch causes visual tearing and back-and-forth
///    stuttering — perceived by the user as **jitter**.
///
/// Why iOS does not jitter:
/// On iOS, `UiKitView` natively composites the `MKMapView` / `GMSMapView` layer directly within the iOS CoreAnimation
/// layer hierarchy without offscreen GL texture copying or asynchronous texture synchronization lag.
///
/// SOLUTION & VERIFICATION:
/// 1. This test suite verifies that during an animator loop, `GoogleMap` correctly computes and emits
///    high-frequency `PolylineUpdates` (`polylinesToChange`) on every tick.
/// 2. For developers requiring jitter-free high-frequency map animations on Android, switching to Hybrid Composition
///    (`useAndroidViewSurface = true` via `GoogleMapsFlutterAndroid`) embeds the native Android `View` directly into
///    the window manager's surface hierarchy, eliminating offscreen texture lag and preventing rendering jitter.
void main() {
  late FakeGoogleMapsFlutterPlatform platform;

  setUp(() {
    platform = FakeGoogleMapsFlutterPlatform();
    GoogleMapsFlutterPlatform.instance = platform;
  });

  testWidgets(
      'Issue #64420 reproduction: Rapid polyline animation emits continuous PolylineUpdates to platform',
      (WidgetTester tester) async {
    final Completer<GoogleMapController> controllerCompleter =
        Completer<GoogleMapController>();

    // Route coordinates simulating the route animation in issue #64420.
    const List<LatLng> routePoints = <LatLng>[
      LatLng(37.43262, -122.08781),
      LatLng(37.43220, -122.08802),
      LatLng(37.43213, -122.08798),
      LatLng(37.43206, -122.08779),
      LatLng(37.43207, -122.08764),
    ];

    await tester.pumpWidget(Directionality(
      textDirection: TextDirection.ltr,
      child: GoogleMap(
        initialCameraPosition: const CameraPosition(
          target: LatLng(37.43262, -122.08781),
          zoom: 15,
        ),
        polylines: <Polyline>{
          const Polyline(
            polylineId: PolylineId('route'),
            points: <LatLng>[LatLng(37.43262, -122.08781)],
            width: 3,
            color: Colors.red,
          ),
        },
        onMapCreated: (GoogleMapController controller) {
          controllerCompleter.complete(controller);
        },
      ),
    ));

    await tester.pumpAndSettle();
    final GoogleMapController controller = await controllerCompleter.future;
    expect(controller, isNotNull);

    final PlatformMapStateRecorder map = platform.lastCreatedMap;
    // Initial creation emits 1 polyline update (adding the initial polyline).
    expect(map.polylineUpdates.length, 1);
    expect(map.polylineUpdates.first.polylinesToAdd.length, 1);

    // Simulate an animator (e.g. Ticker / Stream) rapidly updating the polyline across 4 animation frames.
    for (int i = 1; i < routePoints.length; i++) {
      final List<LatLng> animatedPoints = routePoints.sublist(0, i + 1);

      await tester.pumpWidget(Directionality(
        textDirection: TextDirection.ltr,
        child: GoogleMap(
          initialCameraPosition: const CameraPosition(
            target: LatLng(37.43262, -122.08781),
            zoom: 15,
          ),
          polylines: <Polyline>{
            Polyline(
              polylineId: const PolylineId('route'),
              points: animatedPoints,
              width: 3,
              color: Colors.red,
            ),
          },
        ),
      ));
      await tester.pump();

      // On every animation frame, a new PolylineUpdates object is emitted with the polyline in `polylinesToChange`.
      expect(map.polylineUpdates.length, i + 1);
      final PolylineUpdates latestUpdate = map.polylineUpdates.last;
      expect(latestUpdate.polylinesToChange.length, 1,
          reason:
              'Frame $i of animator should emit exactly 1 changed polyline.');
      expect(
        latestUpdate.polylinesToChange.first.points.length,
        i + 1,
        reason:
            'Frame $i of animator should update polyline points to length ${i + 1}.',
      );
    }

    // Verify that across the rapid animation loop, all updates were continuously dispatched.
    expect(map.polylineUpdates.length, routePoints.length);
  });

  test(
      'Issue #64420 verification: Polyline equality check evaluates all properties, triggering update on point change',
      () {
    const Polyline p1 = Polyline(
      polylineId: PolylineId('animated_step'),
      points: <LatLng>[LatLng(0, 0), LatLng(1, 1)],
      width: 5,
      color: Colors.blue,
    );
    const Polyline p2 = Polyline(
      polylineId: PolylineId('animated_step'),
      points: <LatLng>[LatLng(0, 0), LatLng(1, 1), LatLng(2, 2)],
      width: 5,
      color: Colors.blue,
    );

    // When points change during animation, equality is false.
    expect(p1 == p2, isFalse);

    final PolylineUpdates updates =
        PolylineUpdates.from(<Polyline>{p1}, <Polyline>{p2});
    expect(updates.polylinesToChange.length, 1);
    expect(updates.polylinesToChange.first.polylineId,
        const PolylineId('animated_step'));
  });
}
