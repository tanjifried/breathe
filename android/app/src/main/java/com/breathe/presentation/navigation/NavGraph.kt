package com.breathe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breathe.presentation.ui.calm.CalmScreen
import com.breathe.presentation.ui.calm.CalmViewModel
import com.breathe.presentation.ui.home.HomeScreen
import com.breathe.presentation.ui.home.HomeViewModel
import com.breathe.presentation.ui.insights.InsightsScreen
import com.breathe.presentation.ui.insights.InsightsViewModel
import com.breathe.presentation.ui.log.ConflictLogScreen
import com.breathe.presentation.ui.log.LogViewModel
import com.breathe.presentation.ui.pair.PairingScreen
import com.breathe.presentation.ui.pair.PairingViewModel
import com.breathe.presentation.ui.status.StatusScreen
import com.breathe.presentation.ui.status.StatusViewModel
import com.breathe.presentation.ui.timeout.TimeoutScreen
import com.breathe.presentation.ui.timeout.TimeoutViewModel
import com.breathe.presentation.ui.updates.QuickUpdatesScreen
import com.breathe.presentation.ui.updates.QuickUpdatesViewModel
import com.breathe.presentation.ui.voice.VoiceStudioScreen
import com.breathe.presentation.ui.voice.VoiceViewModel

@Composable
fun BreatheNavGraph() {
  val navController = rememberNavController()
  val appEntryViewModel = hiltViewModel<AppEntryViewModel>()
  val appEntryState by appEntryViewModel.uiState.collectAsStateWithLifecycle()

  val navigateTo: (String) -> Unit = { route ->
    navController.navigateSingleTop(route)
  }

  NavHost(
    navController = navController,
    startDestination = Screen.Loading.route
  ) {
    composable(Screen.Loading.route) {
      LaunchedEffect(appEntryState.isLoading, appEntryState.targetRoute) {
        val targetRoute = appEntryState.targetRoute
        if (!appEntryState.isLoading && targetRoute != null) {
          navController.navigate(targetRoute) {
            popUpTo(Screen.Loading.route) { inclusive = true }
            launchSingleTop = true
          }
        }
      }

      LoadingScreen()
    }

    composable(Screen.Pairing.route) {
      PairingScreen(
        viewModel = hiltViewModel<PairingViewModel>(),
        onContinue = { navController.navigateSingleTop(Screen.Home.route) }
      )
    }

    composable(Screen.Home.route) {
      HomeScreen(
        viewModel = hiltViewModel<HomeViewModel>(),
        onNavigate = navigateTo
      )
    }

    composable(Screen.Calm.route) {
      CalmScreen(
        viewModel = hiltViewModel<CalmViewModel>(),
        onNavigate = navigateTo
      )
    }

    composable(Screen.Timeout.route) {
      TimeoutScreen(
        viewModel = hiltViewModel<TimeoutViewModel>(),
        onNavigate = navigateTo
      )
    }

    composable(Screen.Status.route) {
      StatusScreen(
        viewModel = hiltViewModel<StatusViewModel>(),
        onNavigate = navigateTo
      )
    }

    composable(Screen.Updates.route) {
      QuickUpdatesScreen(
        viewModel = hiltViewModel<QuickUpdatesViewModel>(),
        onNavigate = navigateTo
      )
    }

    composable(Screen.Voice.route) {
      VoiceStudioScreen(
        viewModel = hiltViewModel<VoiceViewModel>(),
        onNavigate = navigateTo
      )
    }

    composable(Screen.Log.route) {
      ConflictLogScreen(
        viewModel = hiltViewModel<LogViewModel>(),
        onNavigate = navigateTo
      )
    }

    composable(Screen.Insights.route) {
      InsightsScreen(
        viewModel = hiltViewModel<InsightsViewModel>(),
        onNavigate = navigateTo
      )
    }
  }
}

private fun NavHostController.navigateSingleTop(route: String) {
  navigate(route) {
    launchSingleTop = true
    restoreState = true
  }
}
