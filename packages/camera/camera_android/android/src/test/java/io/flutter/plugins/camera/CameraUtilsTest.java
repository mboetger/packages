// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import io.flutter.embedding.engine.systemchannels.PlatformChannel;
import io.flutter.plugins.camera.features.autofocus.FocusMode;
import io.flutter.plugins.camera.features.exposurelock.ExposureMode;
import io.flutter.plugins.camera.features.flash.FlashMode;
import io.flutter.plugins.camera.features.resolution.ResolutionPreset;
import java.util.List;
import org.junit.Test;

public class CameraUtilsTest {

  @Test
  public void getAvailableCameras_retrievesValidCameras() throws CameraAccessException {
    final Activity mockActivity = mock(Activity.class);
    final CameraManager mockCameraManager = mock(CameraManager.class);
    final CameraCharacteristics mockCharacteristics1 = mock(CameraCharacteristics.class);
    final CameraCharacteristics mockCharacteristics2 = mock(CameraCharacteristics.class);
    final CameraCharacteristics mockCharacteristics3 = mock(CameraCharacteristics.class);
    final CameraCharacteristics mockCharacteristics4 = mock(CameraCharacteristics.class);
    final String[] mockCameraIds = {"1394902", "-192930", "0283835", "foobar"};

    when(mockActivity.getSystemService(Context.CAMERA_SERVICE)).thenReturn(mockCameraManager);
    when(mockCameraManager.getCameraIdList()).thenReturn(mockCameraIds);
    when(mockCameraManager.getCameraCharacteristics("1394902")).thenReturn(mockCharacteristics1);
    when(mockCameraManager.getCameraCharacteristics("-192930")).thenReturn(mockCharacteristics2);
    when(mockCameraManager.getCameraCharacteristics("0283835")).thenReturn(mockCharacteristics3);
    when(mockCameraManager.getCameraCharacteristics("foobar")).thenReturn(mockCharacteristics4);

    when(mockCharacteristics1.get(any()))
        .thenReturn(90)
        .thenReturn(CameraMetadata.LENS_FACING_FRONT);
    when(mockCharacteristics2.get(any()))
        .thenReturn(180)
        .thenReturn(CameraMetadata.LENS_FACING_BACK);
    when(mockCharacteristics3.get(any()))
        .thenReturn(270)
        .thenReturn(CameraMetadata.LENS_FACING_EXTERNAL);
    when(mockCharacteristics4.get(any()))
        .thenReturn(0)
        .thenReturn(CameraMetadata.LENS_FACING_EXTERNAL);

    List<Messages.PlatformCameraDescription> availableCameras =
        CameraUtils.getAvailableCameras(mockActivity);

    assertEquals(4, availableCameras.size());
    assertEquals("1394902", availableCameras.get(0).getName());
    assertEquals(90, availableCameras.get(0).getSensorOrientation().intValue());
    assertEquals(
        Messages.PlatformCameraLensDirection.FRONT, availableCameras.get(0).getLensDirection());

    assertEquals("-192930", availableCameras.get(1).getName());
    assertEquals(180, availableCameras.get(1).getSensorOrientation().intValue());
    assertEquals(
        Messages.PlatformCameraLensDirection.BACK, availableCameras.get(1).getLensDirection());

    assertEquals("0283835", availableCameras.get(2).getName());
    assertEquals(270, availableCameras.get(2).getSensorOrientation().intValue());
    assertEquals(
        Messages.PlatformCameraLensDirection.EXTERNAL, availableCameras.get(2).getLensDirection());

    assertEquals("foobar", availableCameras.get(3).getName());
    assertEquals(0, availableCameras.get(3).getSensorOrientation().intValue());
    assertEquals(
        Messages.PlatformCameraLensDirection.EXTERNAL, availableCameras.get(3).getLensDirection());
  }

  @Test
  public void orientationToPigeonTest() {
    assertEquals(
        CameraUtils.orientationToPigeon(PlatformChannel.DeviceOrientation.PORTRAIT_UP),
        Messages.PlatformDeviceOrientation.PORTRAIT_UP);
    assertEquals(
        CameraUtils.orientationToPigeon(PlatformChannel.DeviceOrientation.PORTRAIT_DOWN),
        Messages.PlatformDeviceOrientation.PORTRAIT_DOWN);
    assertEquals(
        CameraUtils.orientationToPigeon(PlatformChannel.DeviceOrientation.LANDSCAPE_LEFT),
        Messages.PlatformDeviceOrientation.LANDSCAPE_LEFT);
    assertEquals(
        CameraUtils.orientationToPigeon(PlatformChannel.DeviceOrientation.LANDSCAPE_RIGHT),
        Messages.PlatformDeviceOrientation.LANDSCAPE_RIGHT);
  }

  @Test
  public void orientationFromPigeonTest() {
    assertEquals(
        CameraUtils.orientationFromPigeon(Messages.PlatformDeviceOrientation.PORTRAIT_UP),
        PlatformChannel.DeviceOrientation.PORTRAIT_UP);
    assertEquals(
        CameraUtils.orientationFromPigeon(Messages.PlatformDeviceOrientation.PORTRAIT_DOWN),
        PlatformChannel.DeviceOrientation.PORTRAIT_DOWN);
    assertEquals(
        CameraUtils.orientationFromPigeon(Messages.PlatformDeviceOrientation.LANDSCAPE_LEFT),
        PlatformChannel.DeviceOrientation.LANDSCAPE_LEFT);
    assertEquals(
        CameraUtils.orientationFromPigeon(Messages.PlatformDeviceOrientation.LANDSCAPE_RIGHT),
        PlatformChannel.DeviceOrientation.LANDSCAPE_RIGHT);
  }

  @Test
  public void focusModeToPigeonTest() {
    assertEquals(CameraUtils.focusModeToPigeon(FocusMode.auto), Messages.PlatformFocusMode.AUTO);
    assertEquals(
        CameraUtils.focusModeToPigeon(FocusMode.locked), Messages.PlatformFocusMode.LOCKED);
  }

  @Test
  public void focusModeFromPigeonTest() {
    assertEquals(CameraUtils.focusModeFromPigeon(Messages.PlatformFocusMode.AUTO), FocusMode.auto);
    assertEquals(
        CameraUtils.focusModeFromPigeon(Messages.PlatformFocusMode.LOCKED), FocusMode.locked);
  }

  @Test
  public void exposureModeToPigeonTest() {
    assertEquals(
        CameraUtils.exposureModeToPigeon(ExposureMode.auto), Messages.PlatformExposureMode.AUTO);
    assertEquals(
        CameraUtils.exposureModeToPigeon(ExposureMode.locked),
        Messages.PlatformExposureMode.LOCKED);
  }

  @Test
  public void exposureModeFromPigeonTest() {
    assertEquals(
        CameraUtils.exposureModeFromPigeon(Messages.PlatformExposureMode.AUTO), ExposureMode.auto);
    assertEquals(
        CameraUtils.exposureModeFromPigeon(Messages.PlatformExposureMode.LOCKED),
        ExposureMode.locked);
  }

  @Test
  public void resolutionPresetFromPigeonTest() {
    assertEquals(
        CameraUtils.resolutionPresetFromPigeon(Messages.PlatformResolutionPreset.LOW),
        ResolutionPreset.low);
    assertEquals(
        CameraUtils.resolutionPresetFromPigeon(Messages.PlatformResolutionPreset.MEDIUM),
        ResolutionPreset.medium);
    assertEquals(
        CameraUtils.resolutionPresetFromPigeon(Messages.PlatformResolutionPreset.HIGH),
        ResolutionPreset.high);
    assertEquals(
        CameraUtils.resolutionPresetFromPigeon(Messages.PlatformResolutionPreset.VERY_HIGH),
        ResolutionPreset.veryHigh);
    assertEquals(
        CameraUtils.resolutionPresetFromPigeon(Messages.PlatformResolutionPreset.ULTRA_HIGH),
        ResolutionPreset.ultraHigh);
    assertEquals(
        CameraUtils.resolutionPresetFromPigeon(Messages.PlatformResolutionPreset.MAX),
        ResolutionPreset.max);
  }

  @Test
  public void imageFormatGroupFromPigeonTest() {
    assertEquals(
        CameraUtils.imageFormatGroupFromPigeon(Messages.PlatformImageFormatGroup.YUV420).intValue(),
        ImageFormat.YUV_420_888);
    assertEquals(
        CameraUtils.imageFormatGroupFromPigeon(Messages.PlatformImageFormatGroup.JPEG).intValue(),
        ImageFormat.JPEG);
    assertEquals(
        CameraUtils.imageFormatGroupFromPigeon(Messages.PlatformImageFormatGroup.NV21).intValue(),
        ImageFormat.NV21);
  }

  @Test
  public void flashModeFromPigeonTest() {
    assertEquals(CameraUtils.flashModeFromPigeon(Messages.PlatformFlashMode.AUTO), FlashMode.auto);
    assertEquals(
        CameraUtils.flashModeFromPigeon(Messages.PlatformFlashMode.ALWAYS), FlashMode.always);
    assertEquals(CameraUtils.flashModeFromPigeon(Messages.PlatformFlashMode.OFF), FlashMode.off);
    assertEquals(
        CameraUtils.flashModeFromPigeon(Messages.PlatformFlashMode.TORCH), FlashMode.torch);
  }
}
