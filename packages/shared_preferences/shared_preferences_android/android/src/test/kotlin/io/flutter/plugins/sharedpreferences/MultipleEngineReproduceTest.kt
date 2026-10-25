package io.flutter.plugins.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class MultipleEngineReproduceTest {
  private lateinit var flutterPluginBinding1: FlutterPluginBinding
  private lateinit var flutterPluginBinding2: FlutterPluginBinding

  @Before
  fun before() {
    val context = Mockito.mock(Context::class.java)
    val sharedPrefs = Mockito.mock(SharedPreferences::class.java)
    val editor = Mockito.mock(SharedPreferences.Editor::class.java)
    Mockito.`when`(sharedPrefs.edit()).thenReturn(editor)
    Mockito.`when`(
            context.getSharedPreferences(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()))
        .thenReturn(sharedPrefs)

    flutterPluginBinding1 = Mockito.mock(FlutterPluginBinding::class.java)
    Mockito.`when`(flutterPluginBinding1.applicationContext).thenReturn(context)

    flutterPluginBinding2 = Mockito.mock(FlutterPluginBinding::class.java)
    Mockito.`when`(flutterPluginBinding2.applicationContext).thenReturn(context)
  }

  @org.junit.After
  fun after() {
    LegacySharedPreferencesPlugin.clearRegisteredCount()
    SharedPreferencesPlugin.clearRegisteredCount()
  }

  @Test
  fun testDetachingSecondEngineClearsProcessWideMap() {
    // Start main engine and attach plugin
    val plugin1 = LegacySharedPreferencesPlugin()
    plugin1.onAttachedToEngine(flutterPluginBinding1)

    // Verify main engine registered the API
    assertNotNull(
        "API should be registered for main engine",
        SharedPreferencesApiRegistrar()
            .getInstance("PigeonDefaultClassName32uh4ui3lh445uh4h3l2l455g4y34u"))

    // Start background engine and attach second plugin instance
    val plugin2 = LegacySharedPreferencesPlugin()
    plugin2.onAttachedToEngine(flutterPluginBinding2)

    // Detach background engine
    plugin2.onDetachedFromEngine(flutterPluginBinding2)

    // The main engine API registration should NOT have been cleared.
    // This will fail on HEAD because detaching plugin2 registers null globally.
    assertNotNull(
        "API should STILL be registered for main engine after background engine is detached",
        SharedPreferencesApiRegistrar()
            .getInstance("PigeonDefaultClassName32uh4ui3lh445uh4h3l2l455g4y34u"))

    // Detach main engine
    plugin1.onDetachedFromEngine(flutterPluginBinding1)

    // Now it should be cleared
    org.junit.Assert.assertNull(
        "API should be cleared when last engine detaches",
        SharedPreferencesApiRegistrar()
            .getInstance("PigeonDefaultClassName32uh4ui3lh445uh4h3l2l455g4y34u"))
  }

  @Test
  fun testDetachingSecondEngineClearsProcessWideMapAsync() {
    // Start main engine and attach plugin
    val plugin1 = SharedPreferencesPlugin()
    plugin1.onAttachedToEngine(flutterPluginBinding1)

    // Verify main engine registered the APIs
    assertNotNull(
        "API should be registered for main engine (data_store)",
        SharedPreferencesAsyncApiRegistrar().getInstance("data_store"))
    assertNotNull(
        "API should be registered for main engine (shared_preferences)",
        SharedPreferencesAsyncApiRegistrar().getInstance("shared_preferences"))

    // Start background engine and attach second plugin instance
    val plugin2 = SharedPreferencesPlugin()
    plugin2.onAttachedToEngine(flutterPluginBinding2)

    // Detach background engine
    plugin2.onDetachedFromEngine(flutterPluginBinding2)

    // The main engine API registration should NOT have been cleared.
    assertNotNull(
        "API should STILL be registered for main engine after background engine is detached (data_store)",
        SharedPreferencesAsyncApiRegistrar().getInstance("data_store"))
    assertNotNull(
        "API should STILL be registered for main engine after background engine is detached (shared_preferences)",
        SharedPreferencesAsyncApiRegistrar().getInstance("shared_preferences"))

    // Detach main engine
    plugin1.onDetachedFromEngine(flutterPluginBinding1)

    // Now it should be cleared
    org.junit.Assert.assertNull(
        "API should be cleared when last engine detaches (data_store)",
        SharedPreferencesAsyncApiRegistrar().getInstance("data_store"))
    org.junit.Assert.assertNull(
        "API should be cleared when last engine detaches (shared_preferences)",
        SharedPreferencesAsyncApiRegistrar().getInstance("shared_preferences"))
  }
}
