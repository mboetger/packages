// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: omit_obvious_local_variable_types

import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:video_player_android/video_player_android.dart';
import 'package:video_player_platform_interface/video_player_platform_interface.dart';

const String _videoAssetKey = 'assets/Butterfly-209.mp4';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  late AndroidVideoPlayer player;

  setUp(() async {
    player = AndroidVideoPlayer();
    await player.init();
  });

  Future<ui.Image> captureWidget(GlobalKey key) async {
    final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
    return boundary.toImage();
  }

  Future<bool> imagesAreEqual(ui.Image img1, ui.Image img2) async {
    final ByteData? bytes1 = await img1.toByteData();
    final ByteData? bytes2 = await img2.toByteData();
    if (bytes1 == null || bytes2 == null) {
      return false;
    }
    if (bytes1.lengthInBytes != bytes2.lengthInBytes) {
      return false;
    }
    for (var i = 0; i < bytes1.lengthInBytes; i++) {
      if (bytes1.getUint8(i) != bytes2.getUint8(i)) {
        return false;
      }
    }
    return true;
  }

  testWidgets('texture updates when seeking while paused', (WidgetTester tester) async {
    final int playerId = (await player.create(
      DataSource(sourceType: DataSourceType.asset, asset: _videoAssetKey),
    ))!;

    final GlobalKey boundaryKey = GlobalKey();

    final repaintNotifier = ValueNotifier<int>(0);

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: Center(
            child: RepaintBoundary(
              key: boundaryKey,
              child: ValueListenableBuilder<int>(
                valueListenable: repaintNotifier,
                builder: (context, value, child) {
                  return SizedBox(
                    width: 300,
                    height: 200,
                    child: Stack(
                      children: [
                        Texture(textureId: playerId),
                        Text('Repaint: $value', style: const TextStyle(color: Colors.transparent)),
                      ],
                    ),
                  );
                },
              ),
            ),
          ),
        ),
      ),
    );

    // Wait for the video to be initialized and the first frame to render.
    final initialized = Completer<void>();
    late StreamSubscription<VideoEvent> subscription;
    subscription = player.videoEventsFor(playerId).listen((VideoEvent event) {
      if (event.eventType == VideoEventType.initialized) {
        initialized.complete();
        subscription.cancel();
      }
    });

    await initialized.future;
    await tester.pumpAndSettle();

    // Ensure we are paused.
    await player.pause(playerId);
    await tester.pumpAndSettle();

    // Capture initial frame (at t=0).
    final ui.Image initialFrame = await captureWidget(boundaryKey);

    // Seek to 3 seconds.
    await player.seekTo(playerId, const Duration(seconds: 3));
    repaintNotifier.value++;

    // Wait for the seek to complete and the frame to render.
    await tester.pump(const Duration(seconds: 1));

    // Capture frame after seek.
    final ui.Image afterSeekFrame = await captureWidget(boundaryKey);

    // Assert that the frames are different.
    final bool areEqual = await imagesAreEqual(initialFrame, afterSeekFrame);
    expect(areEqual, isFalse, reason: 'Texture did not update after seek while paused.');

    await player.dispose(playerId);
  });

  testWidgets('texture updates during continuous scrubbing', (WidgetTester tester) async {
    final int playerId = (await player.create(
      DataSource(sourceType: DataSourceType.asset, asset: _videoAssetKey),
    ))!;

    final GlobalKey boundaryKey = GlobalKey();
    final repaintNotifier = ValueNotifier<int>(0);

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: Center(
            child: RepaintBoundary(
              key: boundaryKey,
              child: ValueListenableBuilder<int>(
                valueListenable: repaintNotifier,
                builder: (context, value, child) {
                  return SizedBox(
                    width: 300,
                    height: 200,
                    child: Stack(
                      children: [
                        Texture(textureId: playerId),
                        Text('Repaint: $value', style: const TextStyle(color: Colors.transparent)),
                      ],
                    ),
                  );
                },
              ),
            ),
          ),
        ),
      ),
    );

    final initialized = Completer<void>();
    late StreamSubscription<VideoEvent> subscription;
    subscription = player.videoEventsFor(playerId).listen((VideoEvent event) {
      if (event.eventType == VideoEventType.initialized) {
        initialized.complete();
        subscription.cancel();
      }
    });

    await initialized.future;
    await tester.pumpAndSettle();
    await player.pause(playerId);
    await tester.pumpAndSettle();

    final ui.Image initialFrame = await captureWidget(boundaryKey);

    // Simulate scrubbing by seeking rapidly.
    for (var i = 1; i <= 5; i++) {
      await player.seekTo(playerId, Duration(milliseconds: i * 500));
      repaintNotifier.value++;
      await tester.pump(const Duration(milliseconds: 100));
    }

    // Capture immediately after the last seek.
    final ui.Image afterScrubFrame = await captureWidget(boundaryKey);

    final bool areEqual = await imagesAreEqual(initialFrame, afterScrubFrame);
    expect(areEqual, isFalse, reason: 'Texture did not update during scrubbing.');

    await player.dispose(playerId);
  });
}
