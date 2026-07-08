// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera.features.resolution;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.EncoderProfiles;
import android.os.Build;
import android.util.Size;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.flutter.plugins.camera.CameraProperties;
import io.flutter.plugins.camera.SdkCapabilityChecker;
import io.flutter.plugins.camera.features.CameraFeature;
import java.util.List;

/**
 * Controls the resolutions configuration on the {@link android.hardware.camera2} API.
 *
 * <p>The {@link ResolutionFeature} is responsible for converting the platform independent {@link
 * ResolutionPreset} into a {@link android.media.CamcorderProfile} which contains all the properties
 * required to configure the resolution using the {@link android.hardware.camera2} API.
 */
public class ResolutionFeature extends CameraFeature<ResolutionPreset> {
  @Nullable private Size captureSize;
  @Nullable private Size previewSize;
  private CamcorderProfile recordingProfileLegacy;
  private EncoderProfiles recordingProfile;
  @NonNull private ResolutionPreset currentSetting;
  private int cameraId;

  /**
   * Creates a new instance of the {@link ResolutionFeature}.
   *
   * @param cameraProperties Collection of characteristics for the current camera device.
   * @param resolutionPreset Platform agnostic enum containing resolution information.
   * @param cameraName Camera identifier of the camera for which to configure the resolution.
   */
  public ResolutionFeature(
      @NonNull CameraProperties cameraProperties,
      @NonNull ResolutionPreset resolutionPreset,
      @NonNull String cameraName) {
    super(cameraProperties);
    this.currentSetting = resolutionPreset;
    try {
      this.cameraId = Integer.parseInt(cameraName, 10);
    } catch (NumberFormatException e) {
      this.cameraId = -1;
    }
    configureResolution(resolutionPreset, cameraId);
  }

  /**
   * Gets the {@link android.media.CamcorderProfile} containing the information to configure the
   * resolution using the {@link android.hardware.camera2} API.
   *
   * @return Resolution information to configure the {@link android.hardware.camera2} API.
   */
  @Nullable
  public CamcorderProfile getRecordingProfileLegacy() {
    return this.recordingProfileLegacy;
  }

  @Nullable
  public EncoderProfiles getRecordingProfile() {
    return this.recordingProfile;
  }

  /**
   * Gets the optimal preview size based on the configured resolution.
   *
   * @return The optimal preview size.
   */
  @Nullable
  public Size getPreviewSize() {
    return this.previewSize;
  }

  /**
   * Gets the optimal capture size based on the configured resolution.
   *
   * @return The optimal capture size.
   */
  @Nullable
  public Size getCaptureSize() {
    return this.captureSize;
  }

  @NonNull
  @Override
  public String getDebugName() {
    return "ResolutionFeature";
  }

  @SuppressLint("KotlinPropertyAccess")
  @NonNull
  @Override
  public ResolutionPreset getValue() {
    return currentSetting;
  }

  @Override
  public void setValue(@NonNull ResolutionPreset value) {
    this.currentSetting = value;
    configureResolution(currentSetting, cameraId);
  }

  @Override
  public final boolean checkIsSupported() {
    return true;
  }

  @Override
  public void updateBuilder(@NonNull CaptureRequest.Builder requestBuilder) {
    // No-op: when setting a resolution there is no need to update the request builder.
  }

  @VisibleForTesting
  static Size computeBestPreviewSize(int cameraId, ResolutionPreset preset)
      throws IndexOutOfBoundsException {
    if (preset.ordinal() > ResolutionPreset.high.ordinal()) {
      preset = ResolutionPreset.high;
    }
    if (SdkCapabilityChecker.supportsEncoderProfiles()) {
      EncoderProfiles profile =
          getBestAvailableCamcorderProfileForResolutionPreset(cameraId, preset);
      List<EncoderProfiles.VideoProfile> videoProfiles = profile.getVideoProfiles();
      EncoderProfiles.VideoProfile defaultVideoProfile = videoProfiles.get(0);

      if (defaultVideoProfile != null) {
        return new Size(defaultVideoProfile.getWidth(), defaultVideoProfile.getHeight());
      }
    }

    // TODO(camsim99): Suppression is currently safe because legacy code is used as a fallback for
    // SDK < S.
    // This should be removed when reverting that fallback behavior:
    // https://github.com/flutter/flutter/issues/119668.
    CamcorderProfile profile =
        getBestAvailableCamcorderProfileForResolutionPresetLegacy(cameraId, preset);
    return new Size(profile.videoFrameWidth, profile.videoFrameHeight);
  }

  /**
   * Gets the best possible {@link android.media.CamcorderProfile} for the supplied {@link
   * ResolutionPreset}. Supports SDK < 31.
   *
   * @param cameraId Camera identifier which indicates the device's camera for which to select a
   *     {@link android.media.CamcorderProfile}.
   * @param preset The {@link ResolutionPreset} for which is to be translated to a {@link
   *     android.media.CamcorderProfile}.
   * @return The best possible {@link android.media.CamcorderProfile} that matches the supplied
   *     {@link ResolutionPreset}.
   */
  @SuppressLint("UseRequiresApi")
  @TargetApi(Build.VERSION_CODES.R)
  // All of these cases deliberately fall through to get the best available profile.
  @SuppressWarnings({"fallthrough", "deprecation"})
  @NonNull
  public static CamcorderProfile getBestAvailableCamcorderProfileForResolutionPresetLegacy(
      int cameraId, @NonNull ResolutionPreset preset) {
    if (cameraId < 0) {
      throw new AssertionError(
          "getBestAvailableCamcorderProfileForResolutionPreset can only be used with valid (>=0)"
              + " camera identifiers.");
    }

    switch (preset) {
      case max:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
        }
      // fall through
      case ultraHigh:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_2160P);
        }
      // fall through
      case veryHigh:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
        }
      // fall through
      case high:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
        }
      // fall through
      case medium:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
        }
      // fall through
      case low:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
        }
      // fall through
      default:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
          return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
        } else {
          throw new IllegalArgumentException(
              "No capture session available for current capture session.");
        }
    }
  }

  @SuppressLint("UseRequiresApi")
  @TargetApi(Build.VERSION_CODES.S)
  // All of these cases deliberately fall through to get the best available profile.
  @SuppressWarnings("fallthrough")
  @NonNull
  public static EncoderProfiles getBestAvailableCamcorderProfileForResolutionPreset(
      int cameraId, @NonNull ResolutionPreset preset) {
    if (cameraId < 0) {
      throw new AssertionError(
          "getBestAvailableCamcorderProfileForResolutionPreset can only be used with valid (>=0)"
              + " camera identifiers.");
    }

    String cameraIdString = Integer.toString(cameraId);

    switch (preset) {
      case max:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
          return CamcorderProfile.getAll(cameraIdString, CamcorderProfile.QUALITY_HIGH);
        }
      // fall through
      case ultraHigh:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P)) {
          return CamcorderProfile.getAll(cameraIdString, CamcorderProfile.QUALITY_2160P);
        }
      // fall through
      case veryHigh:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
          return CamcorderProfile.getAll(cameraIdString, CamcorderProfile.QUALITY_1080P);
        }
      // fall through
      case high:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
          return CamcorderProfile.getAll(cameraIdString, CamcorderProfile.QUALITY_720P);
        }
      // fall through
      case medium:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
          return CamcorderProfile.getAll(cameraIdString, CamcorderProfile.QUALITY_480P);
        }
      // fall through
      case low:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA)) {
          return CamcorderProfile.getAll(cameraIdString, CamcorderProfile.QUALITY_QVGA);
        }
      // fall through
      default:
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
          return CamcorderProfile.getAll(cameraIdString, CamcorderProfile.QUALITY_LOW);
        }

        throw new IllegalArgumentException(
            "No capture session available for current capture session.");
    }
  }

  private void configureResolution(ResolutionPreset resolutionPreset, int cameraId)
      throws IndexOutOfBoundsException {
    if (!checkIsSupported()) {
      return;
    }
    boolean captureSizeCalculated = false;

    if (cameraId >= 0) {
      if (SdkCapabilityChecker.supportsEncoderProfiles()) {
        recordingProfileLegacy = null;
        try {
          recordingProfile =
              getBestAvailableCamcorderProfileForResolutionPreset(cameraId, resolutionPreset);
          List<EncoderProfiles.VideoProfile> videoProfiles = recordingProfile.getVideoProfiles();
          EncoderProfiles.VideoProfile defaultVideoProfile = videoProfiles.get(0);
          if (defaultVideoProfile != null) {
            captureSizeCalculated = true;
            captureSize = new Size(defaultVideoProfile.getWidth(), defaultVideoProfile.getHeight());
          }
        } catch (Exception e) {
          // Fall through to legacy if error occurs
        }
      }

      if (!captureSizeCalculated) {
        recordingProfile = null;
        try {
          CamcorderProfile camcorderProfile =
              getBestAvailableCamcorderProfileForResolutionPresetLegacy(cameraId, resolutionPreset);
          recordingProfileLegacy = camcorderProfile;
          captureSize =
              new Size(
                  recordingProfileLegacy.videoFrameWidth, recordingProfileLegacy.videoFrameHeight);
          captureSizeCalculated = true;
        } catch (Exception e) {
          // Fall through
        }
      }

      if (captureSizeCalculated) {
        previewSize = computeBestPreviewSize(cameraId, resolutionPreset);
      }
    }

    if (!captureSizeCalculated) {
      // Fallback for non-integer camera IDs (or when CamcorderProfile query fails).
      // We use StreamConfigurationMap to choose the best supported sizes.
      StreamConfigurationMap streamConfigurationMap = cameraProperties.getStreamConfigurationMap();
      if (streamConfigurationMap != null) {
        Size[] sizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
        if (sizes != null && sizes.length > 0) {
          captureSize = getBestSize(sizes, resolutionPreset);
          previewSize = getBestSize(sizes, resolutionPreset);
          captureSizeCalculated = true;
        }
      }

      // We still need a template profile for MediaRecorder configuration when recording video.
      // We attempt to get the template profile using camera 0.
      int templateCameraId = 0;
      if (SdkCapabilityChecker.supportsEncoderProfiles()) {
        recordingProfileLegacy = null;
        try {
          recordingProfile =
              getBestAvailableCamcorderProfileForResolutionPreset(
                  templateCameraId, resolutionPreset);
        } catch (Exception e) {
          recordingProfile = null;
        }
      }
      if (recordingProfile == null) {
        recordingProfileLegacy = null;
        try {
          recordingProfileLegacy =
              getBestAvailableCamcorderProfileForResolutionPresetLegacy(
                  templateCameraId, resolutionPreset);
        } catch (Exception e) {
          recordingProfileLegacy = null;
        }
      }
    }

    if (captureSize == null) {
      captureSize = new Size(640, 480);
    }
    if (previewSize == null) {
      previewSize = new Size(640, 480);
    }
  }

  private static Size getBestSize(Size[] sizes, ResolutionPreset preset) {
    if (sizes == null || sizes.length == 0) {
      return new Size(640, 480);
    }
    int targetHeight;
    switch (preset) {
      case low:
        targetHeight = 240;
        break;
      case medium:
        targetHeight = 480;
        break;
      case high:
        targetHeight = 720;
        break;
      case veryHigh:
        targetHeight = 1080;
        break;
      case ultraHigh:
        targetHeight = 2160;
        break;
      case max:
      default:
        targetHeight = Integer.MAX_VALUE;
        break;
    }

    Size bestSize = null;
    int minDiff = Integer.MAX_VALUE;
    for (Size size : sizes) {
      int diff = Math.abs(size.getHeight() - targetHeight);
      if (diff < minDiff) {
        minDiff = diff;
        bestSize = size;
      } else if (diff == minDiff) {
        if (bestSize == null || size.getWidth() > bestSize.getWidth()) {
          bestSize = size;
        }
      }
    }
    return bestSize;
  }
}
