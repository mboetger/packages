// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test(
    'image_picker_android includes consumer ProGuard rules to keep ImagePickerFileProvider (flutter/flutter#74841)',
    () {
      // When running tests, Directory.current might be at the package root or repo root.
      // Locate the image_picker_android directory.
      Directory packageDir = Directory.current;
      if (!File('${packageDir.path}/pubspec.yaml').existsSync() ||
          !packageDir.path.endsWith('image_picker_android')) {
        // Try finding image_picker_android from repo root if running from elsewhere.
        final candidate = Directory(
          '${packageDir.path}/engine/src/flutter/third_party/pkg/flutter_packages/packages/image_picker/image_picker_android',
        );
        if (candidate.existsSync()) {
          packageDir = candidate;
        }
      }

      final buildGradle = File('${packageDir.path}/android/build.gradle.kts');
      expect(buildGradle.existsSync(), isTrue, reason: 'android/build.gradle.kts should exist');

      final String gradleContent = buildGradle.readAsStringSync();
      expect(
        gradleContent,
        contains('consumerProguardFiles("proguard.txt")'),
        reason:
            'build.gradle.kts must declare consumerProguardFiles("proguard.txt") to prevent R8 stripping ImagePickerFileProvider',
      );

      final proguardFile = File('${packageDir.path}/android/proguard.txt');
      expect(proguardFile.existsSync(), isTrue, reason: 'android/proguard.txt must exist');

      final String proguardContent = proguardFile.readAsStringSync();
      expect(
        proguardContent,
        contains('-keep class io.flutter.plugins.imagepicker.ImagePickerFileProvider'),
        reason: 'proguard.txt must keep ImagePickerFileProvider',
      );
    },
  );
}
