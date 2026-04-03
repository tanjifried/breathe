package com.breathe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.breathe.presentation.ui.voice.VoiceStudioScreen
import com.breathe.presentation.ui.voice.VoiceViewModel

@Composable
fun BreatheNavGraph() {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = Screen.Pairing.route
  ) {
    composable(Screen.Pairing.route) {
      PairingScreen(
        viewModel = hiltViewModel<PairingViewModel>(),
        onContinue = { navController.navigate(Screen.Home.route) }
      )
    }

    composable(Screen.Home.route) {
      HomeScreen(
        viewModel = hiltViewModel<HomeViewModel>(),
        onNavigate = { route -> navController.navigate(route) }
      )
    }

    composable(Screen.Calm.route) {
      CalmScreen(viewModel = hiltViewModel<CalmViewModel>())
    }

    composable(Screen.Timeout.route) {
      TimeoutScreen(viewModel = hiltViewModel<TimeoutViewModel>())
    }

    composable(Screen.Status.route) {
      StatusScreen(viewModel = hiltViewModel<StatusViewModel>())
    }

    composable(Screen.Voice.route) {
      VoiceStudioScreen(viewModel = hiltViewModel<VoiceViewModel>())
    }

    composable(Screen.Log.route) {
      ConflictLogScreen(viewModel = hiltViewModel<LogViewModel>())
    }

    composable(Screen.Insights.route) {
      InsightsScreen(viewModel = hiltViewModel<InsightsViewModel>())
    }
  }
}
