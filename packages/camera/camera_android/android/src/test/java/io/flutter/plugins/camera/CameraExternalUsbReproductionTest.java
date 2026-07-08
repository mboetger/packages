// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import java.util.List;
import org.junit.Test;

public class CameraExternalUsbReproductionTest {

  @Test
  public void getAvailableCameras_includesExternalUsbCameraWithNonIntegerId()
      throws CameraAccessException {
    final Activity mockActivity = mock(Activity.class);
    final CameraManager mockCameraManager = mock(CameraManager.class);
    final CameraCharacteristics mockCameraCharacteristics = mock(CameraCharacteristics.class);

    // "usb:1" is a non-integer camera ID representing an external USB camera.
    final String[] mockCameraIds = {"usb:1"};
    final int mockSensorOrientation = 0;
    final int mockLensFacing = CameraMetadata.LENS_FACING_EXTERNAL;

    when(mockActivity.getSystemService(Context.CAMERA_SERVICE)).thenReturn(mockCameraManager);
    when(mockCameraManager.getCameraIdList()).thenReturn(mockCameraIds);
    when(mockCameraManager.getCameraCharacteristics("usb:1")).thenReturn(mockCameraCharacteristics);
    when(mockCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION))
        .thenReturn(mockSensorOrientation);
    when(mockCameraCharacteristics.get(CameraCharacteristics.LENS_FACING))
        .thenReturn(mockLensFacing);

    List<Messages.PlatformCameraDescription> availableCameras =
        CameraUtils.getAvailableCameras(mockActivity);

    boolean foundUsbCamera = false;
    for (Messages.PlatformCameraDescription camera : availableCameras) {
      if ("usb:1".equals(camera.getName())) {
        foundUsbCamera = true;
        break;
      }
    }
    assertTrue(
        "External USB camera 'usb:1' should be recognized and returned in available cameras list",
        foundUsbCamera);
  }
}
