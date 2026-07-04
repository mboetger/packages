// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.extractor.mp4.FragmentedMp4Extractor;
import androidx.media3.extractor.mp4.Mp4Extractor;
import androidx.test.core.app.ApplicationProvider;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Reproduction unit tests for GitHub issue flutter/flutter#64438:
 * "[video_player android] video skipped first seconds for some .mp4 file".
 *
 * <p>When playing certain MP4 files (e.g. videos exported from PowerPoint or encoded with
 * complex edit lists), ExoPlayer's MP4 extractor processes the edit list (elst atom). Because
 * ExoPlayer only partially supports edit lists (and cannot handle edits that offset timestamps
 * or do not align with sync samples without dropping samples), playback skips the first seconds
 * or jumps unexpectedly when seeking (e.g., seeking to 15s jumps to 35s).
 *
 * <p>As documented in Media3 / ExoPlayer troubleshooting, the official workaround to resolve
 * this playback and seek distortion is to instruct the extractors to ignore edit lists by setting
 * {@link Mp4Extractor#FLAG_WORKAROUND_IGNORE_EDIT_LISTS} and
 * {@link FragmentedMp4Extractor#FLAG_WORKAROUND_IGNORE_EDIT_LISTS} on the
 * {@link DefaultExtractorsFactory} used by the media source factory.
 *
 * <p>These reproduction tests verify that {@link LocalVideoAsset} and {@link HttpVideoAsset}
 * configure their media source factories to ignore edit lists, preventing timestamp skipping
 * and jumpy seeking in MP4 assets.
 */
@RunWith(RobolectricTestRunner.class)
public final class VideoAssetEditListTest {

  @Test
  public void localVideoAssetEnablesIgnoreEditListsWorkaround() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    VideoAsset asset = VideoAsset.fromAssetUrl("asset:///test_video.mp4");
    DefaultMediaSourceFactory mediaSourceFactory =
        (DefaultMediaSourceFactory) asset.getMediaSourceFactory(context);

    // Trigger media source creation so that the delegate factory loader initializes its extractors.
    MediaSource mediaSource = mediaSourceFactory.createMediaSource(asset.getMediaItem());

    DefaultExtractorsFactory extractorsFactory = getExtractorsFactory(mediaSourceFactory);
    int mp4Flags = getMp4Flags(extractorsFactory);
    int fragmentedMp4Flags = getFragmentedMp4Flags(extractorsFactory);

    assertTrue(
        "LocalVideoAsset must enable Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS to prevent "
            + "skipping first seconds or jumping on seek in MP4 files (flutter/flutter#64438). "
            + "Actual mp4Flags: " + mp4Flags,
        (mp4Flags & Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS) != 0);
    assertTrue(
        "LocalVideoAsset must enable FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS to "
            + "prevent skipping first seconds or jumping on seek in FMP4 files (flutter/flutter#64438). "
            + "Actual fragmentedMp4Flags: " + fragmentedMp4Flags,
        (fragmentedMp4Flags & FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS) != 0);
  }

  @Test
  public void remoteVideoAssetEnablesIgnoreEditListsWorkaround() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    VideoAsset asset =
        VideoAsset.fromRemoteUrl(
            "https://flutter.dev/test_video.mp4",
            VideoAsset.StreamingFormat.UNKNOWN,
            new HashMap<>());
    DefaultMediaSourceFactory mediaSourceFactory =
        (DefaultMediaSourceFactory) asset.getMediaSourceFactory(context);

    // Trigger media source creation so that the delegate factory loader initializes its extractors.
    MediaSource mediaSource = mediaSourceFactory.createMediaSource(asset.getMediaItem());

    DefaultExtractorsFactory extractorsFactory = getExtractorsFactory(mediaSourceFactory);
    int mp4Flags = getMp4Flags(extractorsFactory);
    int fragmentedMp4Flags = getFragmentedMp4Flags(extractorsFactory);

    assertTrue(
        "HttpVideoAsset must enable Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS to prevent "
            + "skipping first seconds or jumping on seek in MP4 files (flutter/flutter#64438). "
            + "Actual mp4Flags: " + mp4Flags,
        (mp4Flags & Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS) != 0);
    assertTrue(
        "HttpVideoAsset must enable FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS to "
            + "prevent skipping first seconds or jumping on seek in FMP4 files (flutter/flutter#64438). "
            + "Actual fragmentedMp4Flags: " + fragmentedMp4Flags,
        (fragmentedMp4Flags & FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS) != 0);
  }

  @Test
  public void mp4ExtractorWithIgnoreEditListsFlagIgnoresEditLists() throws Exception {
    DefaultExtractorsFactory extractorsFactory =
        new DefaultExtractorsFactory()
            .setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)
            .setFragmentedMp4ExtractorFlags(FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS);

    int mp4Flags = getMp4Flags(extractorsFactory);
    int fragmentedMp4Flags = getFragmentedMp4Flags(extractorsFactory);

    assertTrue(
        "Configured DefaultExtractorsFactory should have Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS set.",
        (mp4Flags & Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS) != 0);
    assertTrue(
        "Configured DefaultExtractorsFactory should have FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS set.",
        (fragmentedMp4Flags & FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS) != 0);
  }

  private static DefaultExtractorsFactory getExtractorsFactory(
      DefaultMediaSourceFactory mediaSourceFactory) throws Exception {
    Field delegateLoaderField =
        DefaultMediaSourceFactory.class.getDeclaredField("delegateFactoryLoader");
    delegateLoaderField.setAccessible(true);
    Object delegateLoader = delegateLoaderField.get(mediaSourceFactory);

    Field extractorsFactoryField = delegateLoader.getClass().getDeclaredField("extractorsFactory");
    extractorsFactoryField.setAccessible(true);
    return (DefaultExtractorsFactory) extractorsFactoryField.get(delegateLoader);
  }

  private static int getMp4Flags(DefaultExtractorsFactory extractorsFactory) throws Exception {
    Field mp4FlagsField = DefaultExtractorsFactory.class.getDeclaredField("mp4Flags");
    mp4FlagsField.setAccessible(true);
    return (int) mp4FlagsField.get(extractorsFactory);
  }

  private static int getFragmentedMp4Flags(DefaultExtractorsFactory extractorsFactory)
      throws Exception {
    Field fragmentedMp4FlagsField =
        DefaultExtractorsFactory.class.getDeclaredField("fragmentedMp4Flags");
    fragmentedMp4FlagsField.setAccessible(true);
    return (int) fragmentedMp4FlagsField.get(extractorsFactory);
  }
}
