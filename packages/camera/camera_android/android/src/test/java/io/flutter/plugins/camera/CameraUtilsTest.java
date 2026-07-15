// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.util.SizeF;
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
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CameraUtilsTest {

  private CameraCharacteristics mockCharacteristicsForAvailableCameras(
      Integer sensorOrientation,
      Integer lensFacing,
      float[] focalLengths,
      SizeF sensorSize) {
    CameraCharacteristics mockCharacteristics = mock(CameraCharacteristics.class);
    when(mockCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).thenReturn(sensorOrientation);
    when(mockCharacteristics.get(CameraCharacteristics.LENS_FACING)).thenReturn(lensFacing);
    when(mockCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)).thenReturn(focalLengths);
    when(mockCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)).thenReturn(sensorSize);
    return mockCharacteristics;
  }

  private CameraCharacteristics mockCharacteristicsForLensType(
      float[] focalLengths,
      SizeF sensorSize) {
    CameraCharacteristics mockCharacteristics = mock(CameraCharacteristics.class);
    when(mockCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)).thenReturn(focalLengths);
    when(mockCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)).thenReturn(sensorSize);
    return mockCharacteristics;
  }

  @Test
  public void getAvailableCameras_retrievesValidCameras()
      throws CameraAccessException, NumberFormatException {
    final Activity mockActivity = mock(Activity.class);
    final CameraManager mockCameraManager = mock(CameraManager.class);
    final CameraCharacteristics mockCameraCharacteristics = mock(CameraCharacteristics.class);
    final String[] mockCameraIds = {"1394902", "-192930", "0283835", "foobar"};
    final int mockSensorOrientation0 = 90;
    final int mockSensorOrientation2 = 270;
    final int mockLensFacing0 = CameraMetadata.LENS_FACING_FRONT;
    final int mockLensFacing2 = CameraMetadata.LENS_FACING_EXTERNAL;

    when(mockActivity.getSystemService(Context.CAMERA_SERVICE)).thenReturn(mockCameraManager);
    when(mockCameraManager.getCameraIdList()).thenReturn(mockCameraIds);
    
    CameraCharacteristics mockCharacteristics0 = mockCharacteristicsForAvailableCameras(
        mockSensorOrientation0,
        mockLensFacing0,
        new float[]{4.5f},
        new SizeF(4.8f, 3.6f)
    );
    
    CameraCharacteristics mockCharacteristics2 = mockCharacteristicsForAvailableCameras(
        mockSensorOrientation2,
        mockLensFacing2,
        null,
        null
    );

    when(mockCameraManager.getCameraCharacteristics("1394902")).thenReturn(mockCharacteristics0);
    when(mockCameraManager.getCameraCharacteristics("0283835")).thenReturn(mockCharacteristics2);

    List<Messages.PlatformCameraDescription> availableCameras =
        CameraUtils.getAvailableCameras(mockActivity);

    assertEquals(availableCameras.size(), 2);
    assertEquals(availableCameras.get(0).getName(), "1394902");
    assertEquals(availableCameras.get(0).getSensorOrientation().intValue(), mockSensorOrientation0);
    assertEquals(
        availableCameras.get(0).getLensDirection(), Messages.PlatformCameraLensDirection.FRONT);
    assertEquals(
        availableCameras.get(0).getLensType(), Messages.PlatformCameraLensType.WIDE); // Assert lensType

    assertEquals(availableCameras.get(1).getName(), "0283835");
    assertEquals(availableCameras.get(1).getSensorOrientation().intValue(), mockSensorOrientation2);
    assertEquals(
        availableCameras.get(1).getLensDirection(), Messages.PlatformCameraLensDirection.EXTERNAL);
    assertEquals(
        availableCameras.get(1).getLensType(), Messages.PlatformCameraLensType.UNKNOWN); // Assert lensType
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

  @Test
  public void getLensType_mapsCorrectlyToPlatformCameraLensType() {
    // Case: null focal lengths
    CameraCharacteristics mockCharacteristics1 = mockCharacteristicsForLensType(null, new SizeF(4.8f, 3.6f));
    assertEquals(CameraUtils.getLensType(mockCharacteristics1), Messages.PlatformCameraLensType.UNKNOWN);

    // Case: empty focal lengths
    CameraCharacteristics mockCharacteristics2 = mockCharacteristicsForLensType(new float[]{}, new SizeF(4.8f, 3.6f));
    assertEquals(CameraUtils.getLensType(mockCharacteristics2), Messages.PlatformCameraLensType.UNKNOWN);

    // Case: null sensor size
    CameraCharacteristics mockCharacteristics3 = mockCharacteristicsForLensType(new float[]{4.5f}, null);
    assertEquals(CameraUtils.getLensType(mockCharacteristics3), Messages.PlatformCameraLensType.UNKNOWN);

    // Case: sensor size diagonal is 0
    CameraCharacteristics mockCharacteristics4 = mockCharacteristicsForLensType(new float[]{4.5f}, new SizeF(0.0f, 0.0f));
    assertEquals(CameraUtils.getLensType(mockCharacteristics4), Messages.PlatformCameraLensType.UNKNOWN);

    // Case: ULTRA_WIDE (< 24mm equivalent)
    // Diagonal = 6.0, Crop factor = 7.21, focal length = 3.0f -> 21.63mm equivalent
    CameraCharacteristics mockCharacteristics5 = mockCharacteristicsForLensType(new float[]{3.0f}, new SizeF(4.8f, 3.6f));
    assertEquals(CameraUtils.getLensType(mockCharacteristics5), Messages.PlatformCameraLensType.ULTRA_WIDE);

    // Case: WIDE (>= 24mm and < 50mm equivalent)
    // Diagonal = 6.0, Crop factor = 7.21, focal length = 4.5f -> 32.45mm equivalent
    CameraCharacteristics mockCharacteristics6 = mockCharacteristicsForLensType(new float[]{4.5f}, new SizeF(4.8f, 3.6f));
    assertEquals(CameraUtils.getLensType(mockCharacteristics6), Messages.PlatformCameraLensType.WIDE);

    // Case: TELEPHOTO (>= 50mm equivalent)
    // Diagonal = 6.0, Crop factor = 7.21, focal length = 8.0f -> 57.69mm equivalent
    CameraCharacteristics mockCharacteristics7 = mockCharacteristicsForLensType(new float[]{8.0f}, new SizeF(4.8f, 3.6f));
    assertEquals(CameraUtils.getLensType(mockCharacteristics7), Messages.PlatformCameraLensType.TELEPHOTO);
  }
}
