// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.util.LongSparseArray;
import androidx.media3.exoplayer.ExoPlayer;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.platform.PlatformViewRegistry;
import io.flutter.plugins.videoplayer.platformview.PlatformVideoViewFactory;
import io.flutter.plugins.videoplayer.platformview.PlatformViewVideoPlayer;
import io.flutter.plugins.videoplayer.texture.TextureVideoPlayer;
import io.flutter.view.TextureRegistry;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class VideoPlayerPluginTest {
  @Mock private TextureRegistry mockTextureRegistry;
  @Mock private TextureRegistry.SurfaceProducer mockSurfaceProducer;
  @Mock private PlatformViewRegistry mockPlatformViewRegistry;
  private VideoPlayerPlugin plugin;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(mockTextureRegistry.createSurfaceProducer()).thenReturn(mockSurfaceProducer);

    FlutterPlugin.FlutterPluginBinding binding = mock(FlutterPlugin.FlutterPluginBinding.class);
    when(binding.getApplicationContext()).thenReturn(mock(Context.class));
    when(binding.getTextureRegistry()).thenReturn(mockTextureRegistry);
    when(binding.getBinaryMessenger())
        .thenReturn(mock(io.flutter.plugin.common.BinaryMessenger.class));
    when(binding.getPlatformViewRegistry()).thenReturn(mockPlatformViewRegistry);

    plugin = new VideoPlayerPlugin();
    plugin.onAttachedToEngine(binding);
  }

  @SuppressWarnings("unchecked")
  private LongSparseArray<VideoPlayer> getVideoPlayers() throws Exception {
    final Field field = VideoPlayerPlugin.class.getDeclaredField("videoPlayers");
    field.setAccessible(true);
    return (LongSparseArray<VideoPlayer>) field.get(plugin);
  }

  // This is only a placeholder test and doesn't actually initialize the plugin.
  @Test
  public void initPluginDoesNotThrow() {
    final VideoPlayerPlugin plugin = new VideoPlayerPlugin();
  }

  @Test
  public void registersPlatformVideoViewFactory() {
    verify(mockPlatformViewRegistry)
        .registerViewFactory(
            eq("plugins.flutter.dev/video_player_android"), any(PlatformVideoViewFactory.class));
  }

  @Test
  public void createsPlatformViewVideoPlayer() throws Exception {
    try (MockedStatic<PlatformViewVideoPlayer> mockedPlatformViewVideoPlayerStatic =
        mockStatic(PlatformViewVideoPlayer.class)) {
      mockedPlatformViewVideoPlayerStatic
          .when(() -> PlatformViewVideoPlayer.create(any(), any(), any(), any()))
          .thenReturn(mock(PlatformViewVideoPlayer.class));

      final CreationOptions options =
          new CreationOptions(
              "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4",
              null,
              new HashMap<>(),
              null);

      final long playerId = plugin.createForPlatformView(options);

      final LongSparseArray<VideoPlayer> videoPlayers = getVideoPlayers();
      assertTrue(videoPlayers.get(playerId) instanceof PlatformViewVideoPlayer);
    }
  }

  @Test
  public void createsTextureVideoPlayer() throws Exception {
    try (MockedStatic<TextureVideoPlayer> mockedTextureVideoPlayerStatic =
        mockStatic(TextureVideoPlayer.class)) {
      mockedTextureVideoPlayerStatic
          .when(() -> TextureVideoPlayer.create(any(), any(), any(), any(), any()))
          .thenReturn(mock(TextureVideoPlayer.class));

      final CreationOptions options =
          new CreationOptions(
              "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4",
              null,
              new HashMap<>(),
              null);

      final TexturePlayerIds ids = plugin.createForTextureView(options);

      final LongSparseArray<VideoPlayer> videoPlayers = getVideoPlayers();
      assertTrue(videoPlayers.get(ids.getPlayerId()) instanceof TextureVideoPlayer);
    }
  }

  @Test
  public void setMixWithOthersUpdatesExistingPlayersToAllowSimultaneousPlayback() throws Exception {
    try (MockedStatic<TextureVideoPlayer> mockedTextureVideoPlayerStatic =
        mockStatic(TextureVideoPlayer.class)) {
      TextureVideoPlayer mockPlayer1 = mock(TextureVideoPlayer.class);
      TextureVideoPlayer mockPlayer2 = mock(TextureVideoPlayer.class);
      ExoPlayer mockExoPlayer1 = mock(ExoPlayer.class);
      ExoPlayer mockExoPlayer2 = mock(ExoPlayer.class);
      when(mockPlayer1.getExoPlayer()).thenReturn(mockExoPlayer1);
      when(mockPlayer2.getExoPlayer()).thenReturn(mockExoPlayer2);

      TextureRegistry.SurfaceProducer mockSurfaceProducer1 =
          mock(TextureRegistry.SurfaceProducer.class);
      TextureRegistry.SurfaceProducer mockSurfaceProducer2 =
          mock(TextureRegistry.SurfaceProducer.class);
      when(mockSurfaceProducer1.id()).thenReturn(1L);
      when(mockSurfaceProducer2.id()).thenReturn(2L);
      when(mockTextureRegistry.createSurfaceProducer())
          .thenReturn(mockSurfaceProducer1, mockSurfaceProducer2);

      mockedTextureVideoPlayerStatic
          .when(() -> TextureVideoPlayer.create(any(), any(), any(), any(), any()))
          .thenReturn(mockPlayer1, mockPlayer2);

      final CreateMessage createMessage =
          new CreateMessage.Builder()
              .setViewType(PlatformVideoViewType.TEXTURE_VIEW)
              .setUri("https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4")
              .setHttpHeaders(new HashMap<>())
              .build();

      // 1. Create two video players simultaneously (e.g. for side-by-side video playback in an app).
      final long playerId1 = plugin.create(createMessage);
      final long playerId2 = plugin.create(createMessage);

      final LongSparseArray<VideoPlayer> videoPlayers = getVideoPlayers();
      assertEquals(mockPlayer1, videoPlayers.get(playerId1));
      assertEquals(mockPlayer2, videoPlayers.get(playerId2));

      // 2. Call setMixWithOthers(true) so both videos can play simultaneously without audio focus
      // interference on Android.
      plugin.setMixWithOthers(true);

      // 3. Verify that existing video players are updated with mixWithOthers = true so that
      // when play() is called on both controllers simultaneously, the second player's audio focus request
      // does not revoke audio focus from the first player and cause it to stop/pause.
      verify(mockPlayer1).setMixWithOthers(true);
      verify(mockPlayer2).setMixWithOthers(true);
    }
  }
}
