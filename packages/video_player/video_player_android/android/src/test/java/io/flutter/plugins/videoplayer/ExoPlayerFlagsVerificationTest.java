package io.flutter.plugins.videoplayer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import org.junit.Test;

/**
 * Verification test to prove that the ExoPlayer flags requested in
 * flutter/flutter#75304 (specifically for configuring MPEG-TS flags like
 * FLAG_DETECT_ACCESS_UNITS and FLAG_ALLOW_NON_IDR_KEYFRAMES)
 * are present in the public configuration options of the video_player plugin.
 */
public class ExoPlayerFlagsVerificationTest {

  @Test
  public void testExtractorFlagsFeatureIsPresent() {
    try {
      Class<?> optionsClass = CreationOptions.class;
      
      Field flagDetectAccessUnitsField = null;
      Field flagAllowNonIdrKeyframesField = null;
      for (Field field : optionsClass.getDeclaredFields()) {
        if (field.getName().equals("flagDetectAccessUnits")) {
          flagDetectAccessUnitsField = field;
        } else if (field.getName().equals("flagAllowNonIdrKeyframes")) {
          flagAllowNonIdrKeyframesField = field;
        }
      }
      
      assertNotNull("CreationOptions should contain flagDetectAccessUnits field.", flagDetectAccessUnitsField);
      assertNotNull("CreationOptions should contain flagAllowNonIdrKeyframes field.", flagAllowNonIdrKeyframesField);
      
    } catch (SecurityException e) {
      fail("SecurityException while inspecting CreationOptions: " + e.getMessage());
    }
  }
}
