package io.flutter.plugins.camera;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Range;
import android.util.Size;
import androidx.annotation.NonNull;
import io.flutter.plugins.camera.features.CameraFeatureFactory;
import io.flutter.plugins.camera.features.autofocus.AutoFocusFeature;
import io.flutter.plugins.camera.features.exposurelock.ExposureLockFeature;
import io.flutter.plugins.camera.features.exposureoffset.ExposureOffsetFeature;
import io.flutter.plugins.camera.features.exposurepoint.ExposurePointFeature;
import io.flutter.plugins.camera.features.flash.FlashFeature;
import io.flutter.plugins.camera.features.focuspoint.FocusPointFeature;
import io.flutter.plugins.camera.features.fpsrange.FpsRangeFeature;
import io.flutter.plugins.camera.features.noisereduction.NoiseReductionFeature;
import io.flutter.plugins.camera.features.resolution.ResolutionFeature;
import io.flutter.plugins.camera.features.resolution.ResolutionPreset;
import io.flutter.plugins.camera.features.sensororientation.SensorOrientationFeature;
import io.flutter.plugins.camera.features.zoomlevel.ZoomLevelFeature;
import io.flutter.view.TextureRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowCameraCharacteristics;
import org.robolectric.shadows.ShadowCameraManager;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ReproductionTest {

  private static class TestCameraFeatureFactory implements CameraFeatureFactory {
    final AutoFocusFeature mockAutoFocusFeature = mock(AutoFocusFeature.class);
    final ExposureLockFeature mockExposureLockFeature = mock(ExposureLockFeature.class);
    final ExposureOffsetFeature mockExposureOffsetFeature = mock(ExposureOffsetFeature.class);
    final ExposurePointFeature mockExposurePointFeature = mock(ExposurePointFeature.class);
    final FlashFeature mockFlashFeature = mock(FlashFeature.class);
    final FocusPointFeature mockFocusPointFeature = mock(FocusPointFeature.class);
    final FpsRangeFeature mockFpsRangeFeature = mock(FpsRangeFeature.class);
    final NoiseReductionFeature mockNoiseReductionFeature = mock(NoiseReductionFeature.class);
    final ResolutionFeature mockResolutionFeature = mock(ResolutionFeature.class);
    final SensorOrientationFeature mockSensorOrientationFeature = mock(SensorOrientationFeature.class);
    final ZoomLevelFeature mockZoomLevelFeature = mock(ZoomLevelFeature.class);

    @Override
    public AutoFocusFeature createAutoFocusFeature(
        @NonNull CameraProperties cameraProperties, boolean recordingVideo) {
      return mockAutoFocusFeature;
    }

    @Override
    public ExposureLockFeature createExposureLockFeature(
        @NonNull CameraProperties cameraProperties) {
      return mockExposureLockFeature;
    }

    @Override
    public ExposureOffsetFeature createExposureOffsetFeature(
        @NonNull CameraProperties cameraProperties) {
      return mockExposureOffsetFeature;
    }

    @Override
    public ExposurePointFeature createExposurePointFeature(
        @NonNull CameraProperties cameraProperties,
        @NonNull SensorOrientationFeature sensorOrientationFeature) {
      return mockExposurePointFeature;
    }

    @Override
    public FlashFeature createFlashFeature(@NonNull CameraProperties cameraProperties) {
      return mockFlashFeature;
    }

    @Override
    public FocusPointFeature createFocusPointFeature(
        @NonNull CameraProperties cameraProperties,
        @NonNull SensorOrientationFeature sensorOrientationFeature) {
      return mockFocusPointFeature;
    }

    @Override
    public FpsRangeFeature createFpsRangeFeature(@NonNull CameraProperties cameraProperties) {
      return mockFpsRangeFeature;
    }

    @Override
    public NoiseReductionFeature createNoiseReductionFeature(
        @NonNull CameraProperties cameraProperties) {
      return mockNoiseReductionFeature;
    }

    @Override
    public ResolutionFeature createResolutionFeature(
        @NonNull CameraProperties cameraProperties,
        @NonNull ResolutionPreset resolutionPreset,
        @NonNull String cameraName) {
      return mockResolutionFeature;
    }

    @Override
    public SensorOrientationFeature createSensorOrientationFeature(
        @NonNull CameraProperties cameraProperties,
        @NonNull Activity activity,
        @NonNull DartMessenger dartMessenger) {
      return mockSensorOrientationFeature;
    }

    @Override
    public ZoomLevelFeature createZoomLevelFeature(@NonNull CameraProperties cameraProperties) {
      return mockZoomLevelFeature;
    }
  }

  @Test
  public void testCloseLifecycleIssue() throws Exception {
    // 1. Setup Robolectric activity and ShadowCameraManager
    Activity activity = org.robolectric.Robolectric.buildActivity(Activity.class).create().get();
    CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    ShadowCameraManager shadowCameraManager = shadowOf(cameraManager);

    // Register a camera "0"
    CameraCharacteristics characteristics = ShadowCameraCharacteristics.newCameraCharacteristics();
    shadowCameraManager.addCamera("0", characteristics);

    // 2. Setup mocks for Camera
    TextureRegistry.SurfaceTextureEntry mockTexture = mock(TextureRegistry.SurfaceTextureEntry.class);
    CameraProperties mockProperties = mock(CameraProperties.class);
    DartMessenger mockMessenger = mock(DartMessenger.class);
    TestCameraFeatureFactory featureFactory = new TestCameraFeatureFactory();

    // Stub mockProperties to avoid NPEs during Camera creation
    when(mockProperties.getCameraName()).thenReturn("0");
    
    @SuppressWarnings("unchecked")
    final Range<Integer>[] mockRanges =
        (Range<Integer>[]) new Range<?>[] {new Range<Integer>(10, 20)};
    when(mockProperties.getControlAutoExposureAvailableTargetFpsRanges()).thenReturn(mockRanges);

    // Stub resolution feature to avoid NPE during camera.open()
    Size mockSize = new Size(1280, 720);
    when(featureFactory.mockResolutionFeature.checkIsSupported()).thenReturn(true);
    when(featureFactory.mockResolutionFeature.getCaptureSize()).thenReturn(mockSize);
    when(featureFactory.mockResolutionFeature.getPreviewSize()).thenReturn(mockSize);

    final boolean[] closeCalled = new boolean[1];
    // Create Camera with overridden startPreview to do nothing
    Camera camera = new Camera(
        activity,
        mockTexture,
        featureFactory,
        mockMessenger,
        mockProperties,
        new Camera.VideoCaptureSettings(ResolutionPreset.high, false)
    ) {
      @Override
      public void startPreview() {
        // do nothing
      }
      @Override
      public void close() {
        closeCalled[0] = true;
        super.close();
      }
    };

    // 3. Open camera
    camera.open(ImageFormat.YUV_420_888);

    // Idle the background looper to let the openCamera callback run.
    assertNotNull(camera.backgroundHandler);
    ShadowLooper shadowBackgroundLooper = shadowOf(camera.backgroundHandler.getLooper());
    shadowBackgroundLooper.idle();

    // Verify no error was sent
    verify(mockMessenger, never()).sendCameraErrorEvent(anyString());
    // Verify close was not called prematurely
    assertTrue("Camera was closed prematurely", !closeCalled[0]);

    // Verify cameraDevice is set (means onOpened was called)
    assertNotNull(camera.cameraDevice);

    // 4. Close camera
    camera.close();

    // In real Android, the camera service will now asynchronously post onClosed to the background handler.
    // Robolectric's ShadowCameraDevice should also do this.
    // If it does, we need to idle the background looper again to let it process.
    // But the background thread has been quit.
    // If Robolectric tries to post to the quit looper, it should throw or fail.
    // Let's idle the background looper if possible, or see if it already failed.
    
    // Note: if the looper is quit, shadowBackgroundLooper.idle() might throw or do nothing.
    // If Robolectric's CameraDevice.close() posted the message before the thread was quit,
    // it might be in the queue.
    // Let's try to idle it.
    shadowBackgroundLooper.idle();
  }
}
