package com.mapbox.services.android.navigation.ui.v5;

import com.mapbox.navigation.base.route.model.Route;
import com.mapbox.navigation.ui.v5.NavigationViewRouter;
import com.mapbox.navigation.ui.v5.OfflineRouteFoundCallback;
import com.mapbox.services.android.navigation.v5.navigation.OfflineError;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OfflineRouteFoundCallbackTest {

  @Test
  public void onRouteFound_routerIsUpdated() {
    NavigationViewRouter router = mock(NavigationViewRouter.class);
    Route offlineRoute = mock(Route.class);
    OfflineRouteFoundCallback callback = new OfflineRouteFoundCallback(router);

    callback.onRouteFound(offlineRoute);

    verify(router).updateCurrentRoute(offlineRoute);
  }

  @Test
  public void onRouteFound_callStatusIsUpdated() {
    NavigationViewRouter router = mock(NavigationViewRouter.class);
    Route offlineRoute = mock(Route.class);
    OfflineRouteFoundCallback callback = new OfflineRouteFoundCallback(router);

    callback.onRouteFound(offlineRoute);

    verify(router).updateCallStatusReceived();
  }

  @Test
  public void onError_routerReceivesErrorMessage() {
    NavigationViewRouter router = mock(NavigationViewRouter.class);
    OfflineError error = mock(OfflineError.class);
    String errorMessage = "error message";
    when(error.getMessage()).thenReturn(errorMessage);
    OfflineRouteFoundCallback callback = new OfflineRouteFoundCallback(router);

    callback.onError(error);

    verify(router).onRequestError(eq(errorMessage));
  }

  @Test
  public void onError_callStatusIsUpdated() {
    NavigationViewRouter router = mock(NavigationViewRouter.class);
    OfflineError error = mock(OfflineError.class);
    String errorMessage = "error message";
    when(error.getMessage()).thenReturn(errorMessage);
    OfflineRouteFoundCallback callback = new OfflineRouteFoundCallback(router);

    callback.onError(error);

    verify(router).updateCallStatusReceived();
  }
}