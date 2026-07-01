package io.flutter.plugins.googlemaps;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import io.flutter.plugin.common.BinaryMessenger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
public class InfoWindowMultilineReproduceTest {

  private Context context;
  private ComponentActivity activity;

  AutoCloseable mockCloseable;
  @Mock BinaryMessenger mockMessenger;
  @Mock GoogleMap mockGoogleMap;
  @Mock Messages.MapsCallbackApi flutterApi;
  @Mock ClusterManagersController mockClusterManagersController;
  @Mock MarkersController mockMarkersController;
  @Mock PolygonsController mockPolygonsController;
  @Mock PolylinesController mockPolylinesController;
  @Mock CirclesController mockCirclesController;
  @Mock HeatmapsController mockHeatmapsController;
  @Mock TileOverlaysController mockTileOverlaysController;
  @Mock GroundOverlaysController mockGroundOverlaysController;

  @Before
  @SuppressWarnings("deprecation")
  public void before() {
    mockCloseable = MockitoAnnotations.openMocks(this);
    context = ApplicationProvider.getApplicationContext();
    activity = Robolectric.setupActivity(ComponentActivity.class);
  }

  @After
  public void tearDown() throws Exception {
    mockCloseable.close();
  }

  public GoogleMapController getGoogleMapControllerWithMockedDependencies() {
    GoogleMapController googleMapController =
        new GoogleMapController(
            0,
            context,
            mockMessenger,
            flutterApi,
            activity::getLifecycle,
            null,
            mockClusterManagersController,
            mockMarkersController,
            mockPolygonsController,
            mockPolylinesController,
            mockCirclesController,
            mockHeatmapsController,
            mockTileOverlaysController,
            mockGroundOverlaysController);
    googleMapController.init();
    return googleMapController;
  }

  @Test
  public void testDefaultInfoWindowAdapterHandlesMultiline() {
    GoogleMapController.DefaultInfoWindowAdapter adapter =
        new GoogleMapController.DefaultInfoWindowAdapter(context, 1.0f);

    // Set up a marker with a multiline snippet.
    Marker mockMarker = mock(Marker.class);
    when(mockMarker.getTitle()).thenReturn("Title");
    when(mockMarker.getSnippet()).thenReturn("Line 1\nLine 2");

    // Get the info contents view.
    View view = adapter.getInfoContents(mockMarker);
    assertNotNull("InfoWindowAdapter.getInfoContents should return a view", view);

    // Verify the title is present.
    TextView titleView = findTextViewWithText(view, "Title");
    assertNotNull("Should find a TextView with the title", titleView);

    // Verify the multiline snippet is present.
    TextView snippetView = findTextViewWithText(view, "Line 1\nLine 2");
    assertNotNull("Should find a TextView with the multiline snippet", snippetView);

    // Verify the snippet TextView is not restricted to a single line.
    assertNotEquals(
        "Snippet TextView should not be limited to a single line",
        1,
        snippetView.getMaxLines());
  }

  @Test
  public void testMapReadyInitializesMarkerManager() {
    GoogleMapController googleMapController = getGoogleMapControllerWithMockedDependencies();
    googleMapController.onMapReady(mockGoogleMap);

    // Idle the main looper to execute the posted runnable in MarkerManager that sets listeners.
    ShadowLooper.idleMainLooper();

    // Verify that MarkerManager (which implements InfoWindowAdapter) is set on the map.
    verify(mockGoogleMap).setInfoWindowAdapter(any(GoogleMap.InfoWindowAdapter.class));
  }

  // Helper method to traverse the view hierarchy and find a TextView with specific text.
  private TextView findTextViewWithText(View view, String text) {
    if (view instanceof TextView) {
      TextView textView = (TextView) view;
      if (text.equals(textView.getText().toString())) {
        return textView;
      }
    }
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); i++) {
        TextView found = findTextViewWithText(group.getChildAt(i), text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
