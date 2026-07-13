// Copyright 2013 The Flutter Authors
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class ViewTest {
  @Test
  public void scrollTo() {
    final PigeonApiView api = new TestProxyApiRegistrar().getPigeonApiView();

    final View instance = mock(View.class);
    final Context context = mock(Context.class);
    final Resources resources = mock(Resources.class);
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    displayMetrics.density = 2.0f;
    when(instance.getContext()).thenReturn(context);
    when(context.getResources()).thenReturn(resources);
    when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

    final long x = 10L;
    final long y = 20L;
    api.scrollTo(instance, x, y);

    verify(instance).scrollTo(20, 40);
  }

  @Test
  public void scrollBy() {
    final PigeonApiView api = new TestProxyApiRegistrar().getPigeonApiView();

    final View instance = mock(View.class);
    final Context context = mock(Context.class);
    final Resources resources = mock(Resources.class);
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    displayMetrics.density = 2.0f;
    when(instance.getContext()).thenReturn(context);
    when(context.getResources()).thenReturn(resources);
    when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

    final long x = 10L;
    final long y = 20L;
    api.scrollBy(instance, x, y);

    verify(instance).scrollBy(20, 40);
  }

  @Test
  public void getScrollPosition() {
    final PigeonApiView api = new TestProxyApiRegistrar().getPigeonApiView();

    final View instance = mock(View.class);
    final Context context = mock(Context.class);
    final Resources resources = mock(Resources.class);
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    displayMetrics.density = 2.0f;
    when(instance.getContext()).thenReturn(context);
    when(context.getResources()).thenReturn(resources);
    when(resources.getDisplayMetrics()).thenReturn(displayMetrics);

    when(instance.getScrollX()).thenReturn(20);
    when(instance.getScrollY()).thenReturn(40);

    assertEquals(10L, api.getScrollPosition(instance).getX());
    assertEquals(20L, api.getScrollPosition(instance).getY());
  }

  @Test
  public void setVerticalScrollBarEnabled() {
    final PigeonApiView api = new TestProxyApiRegistrar().getPigeonApiView();

    final View instance = mock(View.class);
    final boolean enabled = true;
    api.setVerticalScrollBarEnabled(instance, enabled);

    verify(instance).setVerticalScrollBarEnabled(enabled);
  }

  @Test
  public void setHorizontalScrollBarEnabled() {
    final PigeonApiView api = new TestProxyApiRegistrar().getPigeonApiView();

    final View instance = mock(View.class);
    final boolean enabled = false;
    api.setHorizontalScrollBarEnabled(instance, enabled);

    verify(instance).setHorizontalScrollBarEnabled(enabled);
  }

  @Test
  public void setOverScrollMode() {
    final PigeonApiView api = new TestProxyApiRegistrar().getPigeonApiView();

    final View instance = mock(View.class);
    final OverScrollMode mode = io.flutter.plugins.webviewflutter.OverScrollMode.ALWAYS;
    api.setOverScrollMode(instance, mode);

    verify(instance).setOverScrollMode(View.OVER_SCROLL_ALWAYS);
  }

  @Test
  public void setInsetListenerToSetInsetsToZero() {
    final PigeonApiView api = new TestProxyApiRegistrar().getPigeonApiView();

    final View instance = mock(View.class);
    final WindowInsetsCompat originalInsets =
        new WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(1, 2, 3, 4))
            .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(4, 5, 6, 7))
            .build();

    try (MockedStatic<ViewCompat> viewCompatMockedStatic = mockStatic(ViewCompat.class)) {
      api.setInsetListenerToSetInsetsToZero(
          instance, List.of(WindowInsetsType.SYSTEM_BARS, WindowInsetsType.DISPLAY_CUTOUT));

      final ArgumentCaptor<OnApplyWindowInsetsListener> listenerCaptor =
          ArgumentCaptor.forClass(OnApplyWindowInsetsListener.class);
      viewCompatMockedStatic.verify(
          () -> ViewCompat.setOnApplyWindowInsetsListener(eq(instance), listenerCaptor.capture()));

      final WindowInsetsCompat newInsets =
          listenerCaptor.getValue().onApplyWindowInsets(instance, originalInsets);

      assertEquals(Insets.NONE, newInsets.getInsets(WindowInsetsCompat.Type.systemBars()));
      assertEquals(Insets.NONE, newInsets.getInsets(WindowInsetsCompat.Type.displayCutout()));
    }
  }
}
