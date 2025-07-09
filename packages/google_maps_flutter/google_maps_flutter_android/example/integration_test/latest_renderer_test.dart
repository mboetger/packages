// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_maps_flutter_android/google_maps_flutter_android.dart';
import 'package:google_maps_flutter_platform_interface/google_maps_flutter_platform_interface.dart';
import 'package:integration_test/integration_test.dart';

import 'google_maps_tests.dart' show googleMapsTests;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  setUpAll(() async {
    final GoogleMapsFlutterAndroid instance =
        GoogleMapsFlutterPlatform.instance as GoogleMapsFlutterAndroid;
    await instance.initialize();
  });

  testWidgets('throws PlatformException on multiple initializations',
      (WidgetTester _) async {
    final GoogleMapsFlutterAndroid instance =
        GoogleMapsFlutterPlatform.instance as GoogleMapsFlutterAndroid;
    expect(
        () async => instance.initialize(),
        throwsA(isA<PlatformException>().having((PlatformException e) => e.code,
            'code', 'Already initialized')));
  });

  // Run tests.
  googleMapsTests();
}
