package com.breathe.presentation.navigation

sealed class Screen(val route: String, val title: String) {
  data object Loading : Screen("loading", "Loading")
  data object Pairing : Screen("pairing", "Pairing")
  data object Home : Screen("home", "Home")
  data object Calm : Screen("calm", "Calm")
  data object Timeout : Screen("timeout", "Timeout")
  data object Status : Screen("status", "Status")
  data object Voice : Screen("voice", "Voice Studio")
  data object Log : Screen("log", "Conflict Log")
  data object Insights : Screen("insights", "Insights")
}
