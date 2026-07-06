// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:video_player_android/src/messages.g.dart';
import 'package:video_player_android/video_player_android.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('AndroidVideoPlayer background playback', () {
    test(
      'setAllowBackgroundPlayback is implemented and completes without UnimplementedError',
      () async {
        TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockDecodedMessageHandler<
          Object?
        >(
          const BasicMessageChannel<Object?>(
            'dev.flutter.pigeon.video_player_android.AndroidVideoPlayerApi.setAllowBackgroundPlayback',
            AndroidVideoPlayerApi.pigeonChannelCodec,
          ),
          (Object? message) async => <Object?>[null],
        );

        final player = AndroidVideoPlayer();

        // In order to support video status and controls in the notification center
        // and background audio playback on Android (flutter/flutter#71482),
        // AndroidVideoPlayer must override setAllowBackgroundPlayback from
        // VideoPlayerPlatform and communicate the setting to the native Android plugin.
        // Without the fix, this throws UnimplementedError.
        await expectLater(
          player.setAllowBackgroundPlayback(true),
          completes,
          reason:
              'setAllowBackgroundPlayback should be implemented in AndroidVideoPlayer '
              'to enable background audio playback and notification center controls.',
        );
      },
    );
  });
}
