// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter_test/flutter_test.dart';
import 'package:google_maps_flutter_platform_interface/google_maps_flutter_platform_interface.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('LanLng constructor', () {
    test('Maintains longitude precision if within acceptable range', () async {
      const lat = -34.509981;
      const lng = 150.792384;

      const latLng = LatLng(lat, lng);

      expect(latLng.latitude, equals(lat));
      expect(latLng.longitude, equals(lng));
    });

    test('Normalizes longitude that is below lower limit', () async {
      const lat = -34.509981;
      const lng = -270.0;

      const latLng = LatLng(lat, lng);

      expect(latLng.latitude, equals(lat));
      expect(latLng.longitude, equals(90.0));
    });

    test('Normalizes longitude that is above upper limit', () async {
      const lat = -34.509981;
      const lng = 270.0;

      const latLng = LatLng(lat, lng);

      expect(latLng.latitude, equals(lat));
      expect(latLng.longitude, equals(-90.0));
    });

    test('Includes longitude set to lower limit', () async {
      const lat = -34.509981;
      const lng = -180.0;

      const latLng = LatLng(lat, lng);

      expect(latLng.latitude, equals(lat));
      expect(latLng.longitude, equals(-180.0));
    });

    test('Normalizes longitude set to upper limit', () async {
      const lat = -34.509981;
      const lng = 180.0;

      const latLng = LatLng(lat, lng);

      expect(latLng.latitude, equals(lat));
      expect(latLng.longitude, equals(-180.0));
    });
  });

  group('LatLngBounds', () {
    test('fromPoints constructs bounding box from a single point', () {
      const LatLng point = LatLng(10.0, 20.0);
      final LatLngBounds bounds =
          LatLngBounds.fromPoints(const <LatLng>[point]);

      expect(bounds.southwest, equals(point));
      expect(bounds.northeast, equals(point));
      expect(bounds.contains(point), isTrue);
    });

    test(
        'fromPoints constructs bounding box around Mumbai region without antimeridian crossing',
        () {
      const LatLng firstLocation = LatLng(19.230036, 72.98368010000002);
      const LatLng secondLocation = LatLng(19.2057856, 73.10183510000002);

      final LatLngBounds bounds = LatLngBounds.fromPoints(const <LatLng>[
        firstLocation,
        secondLocation,
      ]);

      expect(bounds.southwest,
          equals(const LatLng(19.2057856, 72.98368010000002)));
      expect(
          bounds.northeast, equals(const LatLng(19.230036, 73.10183510000002)));

      const LatLng mumbaiMidpoint = LatLng(19.2179108, 73.0427576);
      const LatLng northPacificLocation = LatLng(19.2179108, -150.0000000);

      expect(bounds.contains(mumbaiMidpoint), isTrue);
      expect(bounds.contains(northPacificLocation), isFalse);
    });

    test(
        'fromPoints constructs bounding box across antimeridian when appropriate',
        () {
      const LatLng point1 = LatLng(50.0, 179.0);
      const LatLng point2 = LatLng(50.0, -179.0);

      final LatLngBounds bounds = LatLngBounds.fromPoints(const <LatLng>[
        point1,
        point2,
      ]);

      expect(bounds.southwest, equals(const LatLng(50.0, 179.0)));
      expect(bounds.northeast, equals(const LatLng(50.0, -179.0)));

      expect(bounds.contains(const LatLng(50.0, 180.0)), isTrue);
      expect(bounds.contains(const LatLng(50.0, -180.0)), isTrue);
      expect(bounds.contains(const LatLng(50.0, 0.0)), isFalse);
    });

    test('fromPoints throws AssertionError when collection is empty', () {
      expect(
        () => LatLngBounds.fromPoints(const <LatLng>[]),
        throwsAssertionError,
      );
    });
  });
}
