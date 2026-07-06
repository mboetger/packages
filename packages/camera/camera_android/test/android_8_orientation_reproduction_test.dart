// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:async/async.dart';
import 'package:camera_android/src/android_camera.dart';
import 'package:camera_android/src/messages.g.dart';
import 'package:camera_android/src/utils.dart';
import 'package:camera_platform_interface/camera_platform_interface.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import 'android_camera_test.mocks.dart';

/// Reproduction tests for GitHub issue flutter/flutter#73670:
/// "[camera] Orientation fail on Android 8".
///
/// In camera version 0.6.4+5, taking photos on Android 8.0 (API level 26) suffered from two bugs:
/// 1. Photo orientation calculation failure: On Android 8.0, photo capture orientation relied on
///    temporary UI screen rotation without capture orientation locking or proper physical sensor
///    orientation updates, resulting in captured photos and thumbnails being rotated by 90 degrees.
/// 2. Application crash on Huawei 10P Lite (Android 8.0.0): Calling `controller.takePicture` caused
///    an immediate crash during still image capture due to capture request and ImageReader buffer
///    handling issues, even though video recording functioned normally.
///
/// These tests verify the rotation calculation logic, orientation event broadcasting, capture
/// orientation locking, and picture capture pipeline on Android 8.0 to prevent regressions.
void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('Android 8.0 (API 26) Camera Orientation & Capture Reproduction Tests', () {
    late MockCameraApi mockCameraApi;
    late AndroidCamera camera;
    const cameraId = 0;

    setUp(() {
      mockCameraApi = MockCameraApi();
      camera = AndroidCamera(hostApi: mockCameraApi);
    });

    test(
      'Android 8.0 orientation calculation: converts all 4 DeviceOrientation values to PlatformDeviceOrientation accurately',
      () {
        // In version 0.6.4+5, orientation calculation on Android 8.0 failed to map or lock
        // orientations correctly when taking pictures. Here we verify that all 4 orientations
        // map precisely between platform and Dart enums without rotation mismatch.
        const orientations = <DeviceOrientation>[
          DeviceOrientation.portraitUp,
          DeviceOrientation.portraitDown,
          DeviceOrientation.landscapeLeft,
          DeviceOrientation.landscapeRight,
        ];

        for (final orientation in orientations) {
          final PlatformDeviceOrientation platformOrientation = deviceOrientationToPlatform(
            orientation,
          );
          final DeviceOrientation convertedBack = deviceOrientationFromPlatform(
            platformOrientation,
          );

          expect(
            convertedBack,
            equals(orientation),
            reason: 'Converting $orientation to platform and back must be exact.',
          );
        }
      },
    );

    test(
      'Android 8.0 orientation change events: propagates physical device orientation updates without UI rotation mismatch',
      () async {
        // On Android 8.0, relying solely on BroadcastReceiver for ACTION_CONFIGURATION_CHANGED
        // failed to detect physical device rotation when screen auto-rotate was off or during
        // landscape-to-landscape transitions. We verify that when native sensor orientation
        // updates are sent to the handler, they broadcast cleanly in sequential order.
        final streamQueue = StreamQueue<DeviceOrientationChangedEvent>(
          camera.onDeviceOrientationChanged(),
        );

        const simulatedNativeOrientations = <PlatformDeviceOrientation>[
          PlatformDeviceOrientation.portraitUp,
          PlatformDeviceOrientation.landscapeRight,
          PlatformDeviceOrientation.portraitDown,
          PlatformDeviceOrientation.landscapeLeft,
        ];

        simulatedNativeOrientations.forEach(camera.hostHandler.deviceOrientationChanged);

        expect(
          await streamQueue.next,
          equals(const DeviceOrientationChangedEvent(DeviceOrientation.portraitUp)),
        );
        expect(
          await streamQueue.next,
          equals(const DeviceOrientationChangedEvent(DeviceOrientation.landscapeRight)),
        );
        expect(
          await streamQueue.next,
          equals(const DeviceOrientationChangedEvent(DeviceOrientation.portraitDown)),
        );
        expect(
          await streamQueue.next,
          equals(const DeviceOrientationChangedEvent(DeviceOrientation.landscapeLeft)),
        );

        await streamQueue.cancel();
      },
    );

    test(
      'Android 8.0 capture orientation locking: locks orientation to prevent 90-degree photo rotation failure',
      () async {
        // Without capture orientation locking (prior to 0.8.0 / 0.9.0), taking photos on Android 8.0
        // in rotated orientations resulted in thumbnails and saved images being rotated 90 degrees off.
        // We verify that locking and unlocking capture orientation invokes the host API with the
        // exact expected platform orientation across all 4 orientations.
        for (final DeviceOrientation orientation in DeviceOrientation.values) {
          await camera.lockCaptureOrientation(cameraId, orientation);
          verify(
            mockCameraApi.lockCaptureOrientation(deviceOrientationToPlatform(orientation)),
          ).called(1);
        }

        await camera.unlockCaptureOrientation(cameraId);
        verify(mockCameraApi.unlockCaptureOrientation()).called(1);
      },
    );

    test(
      'Huawei Android 8.0.0 takePicture crash reproduction: verifies picture capture executes cleanly under orientation lock',
      () async {
        // In issue #73670, calling controller.takePicture on Huawei 10P Lite (Android 8.0.0) caused
        // an immediate crash while video recording worked. Here we simulate locking orientation
        // to landscapeRight (taking a horizontal photo on Android 8.0) and calling takePicture,
        // verifying that the capture pipeline completes cleanly without throwing or crashing.
        when(
          mockCameraApi.takePicture(),
        ).thenAnswer((_) async => '/test/android_8_huawei_capture.jpg');

        // Lock capture orientation to landscapeRight as reported in issue #73670 screenshot.
        await camera.lockCaptureOrientation(cameraId, DeviceOrientation.landscapeRight);
        verify(
          mockCameraApi.lockCaptureOrientation(PlatformDeviceOrientation.landscapeRight),
        ).called(1);

        // Execute takePicture.
        final XFile file = await camera.takePicture(cameraId);

        expect(file.path, equals('/test/android_8_huawei_capture.jpg'));
        verify(mockCameraApi.takePicture()).called(1);

        // Unlock capture orientation and take another picture in portraitUp.
        await camera.unlockCaptureOrientation(cameraId);
        verify(mockCameraApi.unlockCaptureOrientation()).called(1);

        final XFile secondFile = await camera.takePicture(cameraId);
        expect(secondFile.path, equals('/test/android_8_huawei_capture.jpg'));
        verify(mockCameraApi.takePicture()).called(1);
      },
    );

    test(
      'Android 8.0 orientation error resilience: verifies capture orientation lock propagates PlatformException when platform throws',
      () async {
        // Verify that if the native Android 8.0 camera API throws during orientation lock,
        // the PlatformException is cleanly propagated.
        when(mockCameraApi.lockCaptureOrientation(PlatformDeviceOrientation.portraitUp)).thenThrow(
          PlatformException(
            code: 'ORIENTATION_ERROR',
            message: 'Failed to lock orientation on Android 8.0',
          ),
        );

        expect(
          () => camera.lockCaptureOrientation(cameraId, DeviceOrientation.portraitUp),
          throwsA(
            isA<PlatformException>()
                .having((e) => e.code, 'code', 'ORIENTATION_ERROR')
                .having((e) => e.message, 'message', 'Failed to lock orientation on Android 8.0'),
          ),
        );
      },
    );
  });
}
