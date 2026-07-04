// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:async';

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:google_maps_flutter_platform_interface/google_maps_flutter_platform_interface.dart';

import 'fake_google_maps_flutter_platform.dart';

/// Reproduction tests for GitHub issue flutter/flutter#62671:
/// "[google_maps_flutter] CameraUpdate.newLatLngBounds moves map camera to North Pacific Region."
///
/// ROOT CAUSE EXPLANATION:
/// In the reported issue, the user defines two coordinates in Mumbai, India:
///   firstLocation  = LatLng(19.230036, 72.98368010000002);
///   secondLocation = LatLng(19.2057856, 73.10183510000002);
///
/// Notice that `secondLocation` has a smaller latitude (19.2057856 < 19.230036), meaning it is SOUTH
/// of `firstLocation`. However, its longitude (73.10183510000002 > 72.98368010000002) is greater, meaning
/// it is EAST of `firstLocation`. Therefore, `secondLocation` is the SOUTH-EAST corner and `firstLocation`
/// is the NORTH-WEST corner of the bounding box.
///
/// When the user constructs:
///   LatLngBounds(southwest: secondLocation, northeast: firstLocation)
/// they pass a southwest coordinate whose longitude (73.1018) is greater than the northeast longitude (72.9836).
///
/// According to Google Maps API specifications and `LatLngBounds._containsLongitude`, when
/// southwest.longitude > northeast.longitude, the bounding box is defined as crossing the 180th meridian
/// (the antimeridian across the Pacific Ocean). Instead of bounding the small ~0.12° region in Mumbai between
/// 72.9836° E and 73.1018° E, the bounding box spans eastward from 73.1018° E across 180°/-180° all the way around
/// the globe to 72.9836° E (a span of ~359.88 degrees of longitude).
///
/// Consequently, when CameraUpdate.newLatLngBounds(bounds, 50) is passed to the map controller, the camera
/// centers on the midpoint of this ~359.88° span, which is located at approximately longitude -106.96° W
/// (in the North Pacific Ocean between Hawaii and North America), and zooms out to fit the world.
///
/// SOLUTION:
/// Using `LatLngBounds.fromPoints` allows developers to construct bounding boxes from a collection of coordinates
/// without manually determining which point is southwest or northeast, avoiding antimeridian crossing bugs.
void main() {
  late FakeGoogleMapsFlutterPlatform platform;

  setUp(() {
    platform = FakeGoogleMapsFlutterPlatform();
    GoogleMapsFlutterPlatform.instance = platform;
  });

  test(
      'Issue #62671 reproduction: LatLngBounds constructed with southeast/northwest points should contain Mumbai region',
      () {
    // Exact coordinates reported in issue #62671:
    const LatLng firstLocation = LatLng(19.230036, 72.98368010000002);
    const LatLng secondLocation = LatLng(19.2057856, 73.10183510000002);

    // Construct bounds using LatLngBounds.fromPoints to correctly compute southwest and northeast corners
    // from the collection of coordinates.
    final LatLngBounds bounds = LatLngBounds.fromPoints(const <LatLng>[
      secondLocation,
      firstLocation,
    ]);

    // A point located directly between firstLocation and secondLocation in Mumbai, India.
    const LatLng mumbaiMidpoint = LatLng(19.2179108, 73.0427576);

    // A point located in the North Pacific Ocean (around longitude -150° W).
    const LatLng northPacificLocation = LatLng(19.2179108, -150.0000000);

    // EXPECTED BEHAVIOR:
    // The bounds constructed for these two local Mumbai points should enclose the region between them in Mumbai,
    // and should NOT enclose the North Pacific Ocean.
    expect(
      bounds.contains(mumbaiMidpoint),
      isTrue,
      reason:
          'Expected LatLngBounds to contain the local Mumbai region between firstLocation and secondLocation.',
    );
    expect(
      bounds.contains(northPacificLocation),
      isFalse,
      reason:
          'Expected LatLngBounds not to span across the North Pacific Ocean.',
    );
  });

  testWidgets(
      'Issue #62671 reproduction: CameraUpdate.newLatLngBounds moves map camera to North Pacific Region',
      (WidgetTester tester) async {
    final Completer<GoogleMapController> controllerCompleter =
        Completer<GoogleMapController>();
    await tester.pumpWidget(Directionality(
      textDirection: TextDirection.ltr,
      child: GoogleMap(
        initialCameraPosition: const CameraPosition(
          target: LatLng(19.230036, 72.98368010000002),
          zoom: 10,
        ),
        onMapCreated: (GoogleMapController controller) {
          controllerCompleter.complete(controller);
        },
      ),
    ));
    addTearDown(() async {
      await tester.pumpWidget(const SizedBox.shrink());
    });
    final GoogleMapController controller = await controllerCompleter.future;
    final PlatformMapStateRecorder map = platform.lastCreatedMap;

    const LatLng firstLocation = LatLng(19.230036, 72.98368010000002);
    const LatLng secondLocation = LatLng(19.2057856, 73.10183510000002);
    final LatLngBounds bounds = LatLngBounds.fromPoints(const <LatLng>[
      secondLocation,
      firstLocation,
    ]);

    // Simulate calling animateCamera with CameraUpdate.newLatLngBounds(bounds, 50) exactly as in issue #62671.
    await controller.animateCamera(
      CameraUpdate.newLatLngBounds(bounds, 50),
    );

    expect(map.animateCameraConfiguration, isNotNull);
    final CameraUpdate cameraUpdate =
        map.animateCameraConfiguration!.cameraUpdate;
    expect(cameraUpdate, isA<CameraUpdateNewLatLngBounds>());
    final CameraUpdateNewLatLngBounds boundsUpdate =
        cameraUpdate as CameraUpdateNewLatLngBounds;

    const LatLng mumbaiMidpoint = LatLng(19.2179108, 73.0427576);
    const LatLng northPacificLocation = LatLng(19.2179108, -150.0000000);

    // EXPECTED BEHAVIOR:
    // The camera update should target the bounding box around Mumbai, India, not the North Pacific Ocean.
    expect(
      boundsUpdate.bounds.contains(mumbaiMidpoint),
      isTrue,
      reason:
          'CameraUpdate.newLatLngBounds should target the Mumbai region between firstLocation and secondLocation.',
    );
    expect(
      boundsUpdate.bounds.contains(northPacificLocation),
      isFalse,
      reason:
          'CameraUpdate.newLatLngBounds should not target the North Pacific Region.',
    );
  });
}
