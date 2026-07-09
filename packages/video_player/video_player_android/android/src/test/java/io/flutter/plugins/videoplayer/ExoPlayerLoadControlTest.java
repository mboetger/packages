// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.view.Surface;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.test.core.app.ApplicationProvider;
import io.flutter.plugins.videoplayer.platformview.PlatformViewVideoPlayer;
import io.flutter.plugins.videoplayer.texture.TextureVideoPlayer;
import io.flutter.view.TextureRegistry;
import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public final class ExoPlayerLoadControlTest {

  private static Object getFieldValue(Object obj, String fieldName) throws Exception {
    Class<?> clazz = obj.getClass();
    while (clazz != null) {
      try {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }

  @Test
  public void testDefaultExoPlayerHasLargeBufferAndNoByteLimit() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
    ExoPlayer player = new ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .build();

    // Fetch the loadControl from internalPlayer
    Object internalPlayer = getFieldValue(player, "internalPlayer");
    LoadControl loadControl = (LoadControl) getFieldValue(internalPlayer, "loadControl");
    assertNotNull("ExoPlayer should have a LoadControl", loadControl);
    assertTrue("ExoPlayer should use DefaultLoadControl", loadControl instanceof DefaultLoadControl);

    // Verify the buffer configurations
    long minBufferUs = (long) getFieldValue(loadControl, "minBufferUs");
    long maxBufferUs = (long) getFieldValue(loadControl, "maxBufferUs");
    int targetBufferBytesOverwrite = (int) getFieldValue(loadControl, "targetBufferBytesOverwrite");
    boolean prioritizeTimeOverSizeThresholds = (boolean) getFieldValue(loadControl, "prioritizeTimeOverSizeThresholds");

    // By default, media3 1.9.2:
    // minBufferUs = 50_000_000 (50s)
    // maxBufferUs = 50_000_000 (50s)
    // targetBufferBytesOverwrite = -1 (C.LENGTH_UNSET, dynamically calculated which can exceed 125MB)
    // prioritizeTimeOverSizeThresholds = false (does not cap by size)
    
    // We assert these to show that the player has a huge default buffer settings,
    // which can lead to OOM exceptions on large files.
    assertEquals(50000000L, minBufferUs);
    assertEquals(50000000L, maxBufferUs);
    assertEquals(-1, targetBufferBytesOverwrite);
    assertEquals(false, prioritizeTimeOverSizeThresholds);

    player.release();
  }

  @Test
  public void testTextureVideoPlayerAppliesCustomLoadControl() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    VideoPlayerCallbacks mockEvents = mock(VideoPlayerCallbacks.class);
    TextureRegistry.SurfaceProducer mockProducer = mock(TextureRegistry.SurfaceProducer.class);
    when(mockProducer.getSurface()).thenReturn(mock(Surface.class));
    when(mockProducer.handlesCropAndRotation()).thenReturn(true);

    FakeVideoAsset fakeVideoAsset = new FakeVideoAsset("https://flutter.dev/movie.mp4");

    TextureVideoPlayer player = TextureVideoPlayer.create(
        context,
        mockEvents,
        mockProducer,
        fakeVideoAsset,
        new VideoPlayerOptions()
    );

    ExoPlayer exoPlayer = player.getExoPlayer();

    // Fetch the loadControl from internalPlayer
    Object internalPlayer = getFieldValue(exoPlayer, "internalPlayer");
    LoadControl loadControl = (LoadControl) getFieldValue(internalPlayer, "loadControl");
    assertNotNull("ExoPlayer should have a LoadControl", loadControl);
    assertTrue("ExoPlayer should use DefaultLoadControl", loadControl instanceof DefaultLoadControl);

    // Verify the custom buffer configurations
    long minBufferUs = (long) getFieldValue(loadControl, "minBufferUs");
    long maxBufferUs = (long) getFieldValue(loadControl, "maxBufferUs");
    int targetBufferBytesOverwrite = (int) getFieldValue(loadControl, "targetBufferBytesOverwrite");
    boolean prioritizeTimeOverSizeThresholds = (boolean) getFieldValue(loadControl, "prioritizeTimeOverSizeThresholds");

    assertEquals(15000000L, minBufferUs); // 15s
    assertEquals(50000000L, maxBufferUs); // 50s
    assertEquals(-1, targetBufferBytesOverwrite);
    assertEquals(true, prioritizeTimeOverSizeThresholds);

    player.dispose();
  }

  @Test
  public void testPlatformViewVideoPlayerAppliesCustomLoadControl() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    VideoPlayerCallbacks mockEvents = mock(VideoPlayerCallbacks.class);

    FakeVideoAsset fakeVideoAsset = new FakeVideoAsset("https://flutter.dev/movie.mp4");

    PlatformViewVideoPlayer player = PlatformViewVideoPlayer.create(
        context,
        mockEvents,
        fakeVideoAsset,
        new VideoPlayerOptions()
    );

    ExoPlayer exoPlayer = player.getExoPlayer();

    // Fetch the loadControl from internalPlayer
    Object internalPlayer = getFieldValue(exoPlayer, "internalPlayer");
    LoadControl loadControl = (LoadControl) getFieldValue(internalPlayer, "loadControl");
    assertNotNull("ExoPlayer should have a LoadControl", loadControl);
    assertTrue("ExoPlayer should use DefaultLoadControl", loadControl instanceof DefaultLoadControl);

    // Verify the custom buffer configurations
    long minBufferUs = (long) getFieldValue(loadControl, "minBufferUs");
    long maxBufferUs = (long) getFieldValue(loadControl, "maxBufferUs");
    int targetBufferBytesOverwrite = (int) getFieldValue(loadControl, "targetBufferBytesOverwrite");
    boolean prioritizeTimeOverSizeThresholds = (boolean) getFieldValue(loadControl, "prioritizeTimeOverSizeThresholds");

    assertEquals(15000000L, minBufferUs); // 15s
    assertEquals(50000000L, maxBufferUs); // 50s
    assertEquals(-1, targetBufferBytesOverwrite);
    assertEquals(true, prioritizeTimeOverSizeThresholds);

    player.dispose();
  }
}
