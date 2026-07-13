// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;

import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory;

final class LocalVideoAsset extends VideoAsset {
  LocalVideoAsset(
      @NonNull String assetUrl,
      boolean flagDetectAccessUnits,
      boolean flagAllowNonIdrKeyframes) {
    super(assetUrl, flagDetectAccessUnits, flagAllowNonIdrKeyframes);
  }

  @NonNull
  @Override
  public MediaItem getMediaItem() {
    return new MediaItem.Builder().setUri(assetUrl).build();
  }

  @NonNull
  @Override
  public MediaSource.Factory getMediaSourceFactory(@NonNull Context context) {
    DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
    int tsExtractorFlags = 0;
    if (flagDetectAccessUnits) {
      tsExtractorFlags |= DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS;
    }
    if (flagAllowNonIdrKeyframes) {
      tsExtractorFlags |= DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES;
    }
    extractorsFactory.setTsExtractorFlags(tsExtractorFlags);
    return new DefaultMediaSourceFactory(context, extractorsFactory);
  }
}
