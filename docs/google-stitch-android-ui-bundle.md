# Breathe Android UI Bundle For Google Stitch

This document combines the key Android presentation files and the extension style reference into one markdown bundle.

## Notes

- Preserve Jetpack Compose structure.
- Keep file boundaries and screen names the same.
- Match the calm sage/cream visual language from the extension CSS.
- Focus changes on presentation, theme, layout, and reusable UI components.
- Avoid changing repository, domain, or backend contract logic unless absolutely needed for UI composition.

## File: `android/app/build.gradle.kts`

```kotlin
1: plugins {
2:   id("com.android.application")
3:   kotlin("android")
4:   id("org.jetbrains.kotlin.plugin.compose")
5:   id("com.google.devtools.ksp")
6:   id("com.google.dagger.hilt.android")
7: }
8: 
9: android {
10:   namespace = "com.breathe"
11:   compileSdk = 35
12: 
13:   defaultConfig {
14:     applicationId = "com.breathe"
15:     minSdk = 29
16:     targetSdk = 35
17:     versionCode = 2
18:     versionName = "0.1.1-dev"
19:     testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
20: 
21:     buildConfigField("String", "DEFAULT_SERVER_URL", "\"http://10.0.2.2:3000/\"")
22:   }
23: 
24:   buildTypes {
25:     release {
26:       isMinifyEnabled = false
27:       proguardFiles(
28:         getDefaultProguardFile("proguard-android-optimize.txt"),
29:         "proguard-rules.pro"
30:       )
31:     }
32:   }
33: 
34:   compileOptions {
35:     sourceCompatibility = JavaVersion.VERSION_17
36:     targetCompatibility = JavaVersion.VERSION_17
37:   }
38: 
39:   kotlinOptions {
40:     jvmTarget = "17"
41:   }
42: 
43:   buildFeatures {
44:     compose = true
45:     buildConfig = true
46:   }
47: 
48:   packaging {
49:     resources {
50:       excludes += "/META-INF/{AL2.0,LGPL2.1}"
51:     }
52:   }
53: }
54: 
55: dependencies {
56:   implementation(platform("androidx.compose:compose-bom:2024.12.01"))
57:   androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
58: 
59:   implementation("androidx.compose.material3:material3")
60:   implementation("androidx.compose.ui:ui")
61:   implementation("androidx.compose.ui:ui-tooling-preview")
62:   implementation("androidx.activity:activity-compose:1.9.3")
63:   implementation("androidx.navigation:navigation-compose:2.8.5")
64:   implementation("com.google.android.material:material:1.12.0")
65: 
66:   implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
67:   implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
68:   implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
69: 
70:   implementation("com.google.dagger:hilt-android:2.51")
71:   ksp("com.google.dagger:hilt-android-compiler:2.51")
72:   implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
73:   implementation("androidx.hilt:hilt-work:1.2.0")
74: 
75:   implementation("com.squareup.retrofit2:retrofit:2.11.0")
76:   implementation("com.squareup.retrofit2:converter-gson:2.11.0")
77:   implementation("com.squareup.okhttp3:okhttp:4.12.0")
78:   implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
79:   implementation("com.google.code.gson:gson:2.11.0")
80: 
81:   implementation("androidx.room:room-runtime:2.6.1")
82:   implementation("androidx.room:room-ktx:2.6.1")
83:   ksp("androidx.room:room-compiler:2.6.1")
84: 
85:   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
86: 
87:   implementation("androidx.security:security-crypto:1.1.0-alpha06")
88: 
89:   implementation("androidx.work:work-runtime-ktx:2.9.1")
90:   implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
91: 
92:   debugImplementation("androidx.compose.ui:ui-tooling")
93:   debugImplementation("androidx.compose.ui:ui-test-manifest")
94: }
```

## File: `android/app/src/main/java/com/breathe/MainActivity.kt`

```kotlin
1: package com.breathe
2: 
3: import android.os.Bundle
4: import androidx.activity.ComponentActivity
5: import androidx.activity.compose.setContent
6: import androidx.compose.foundation.layout.Box
7: import androidx.compose.foundation.layout.padding
8: import androidx.compose.material3.MaterialTheme
9: import androidx.compose.material3.Surface
10: import androidx.compose.material3.Text
11: import androidx.compose.runtime.Composable
12: import androidx.compose.ui.Alignment
13: import androidx.compose.ui.Modifier
14: import androidx.compose.ui.unit.dp
15: import com.breathe.presentation.navigation.BreatheNavGraph
16: import com.breathe.presentation.theme.BreatheTheme
17: import dagger.hilt.android.AndroidEntryPoint
18: 
19: @AndroidEntryPoint
20: class MainActivity : ComponentActivity() {
21:   override fun onCreate(savedInstanceState: Bundle?) {
22:     super.onCreate(savedInstanceState)
23:     setContent {
24:       BreatheTheme {
25:         Surface {
26:           Box {
27:             BreatheNavGraph()
28:             BuildBadge(
29:               modifier = Modifier
30:                 .align(Alignment.TopEnd)
31:                 .padding(12.dp)
32:             )
33:           }
34:         }
35:       }
36:     }
37:   }
38: }
39: 
40: @Composable
41: private fun BuildBadge(modifier: Modifier = Modifier) {
42:   Surface(
43:     modifier = modifier,
44:     color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
45:     tonalElevation = 4.dp,
46:     shadowElevation = 2.dp,
47:     shape = MaterialTheme.shapes.small
48:   ) {
49:     Text(
50:       text = "Breathe ${BuildConfig.VERSION_NAME}",
51:       modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
52:       style = MaterialTheme.typography.labelMedium,
53:       color = MaterialTheme.colorScheme.onSurface
54:     )
55:   }
56: }
```

## File: `android/app/src/main/java/com/breathe/presentation/theme/Color.kt`

```kotlin
1: package com.breathe.presentation.theme
2: 
3: import androidx.compose.ui.graphics.Color
4: 
5: val BreatheCanvas = Color(0xFFF7FCF9)
6: val BreatheCanvasSoft = Color(0xFFF5FBF7)
7: val BreatheOverlay = Color(0xFFEBF6F0)
8: val BreatheInk = Color(0xFF1F302A)
9: val BreatheMutedInk = Color(0xFF4B6258)
10: val BreatheAccent = Color(0xFF4F8B78)
11: val BreatheAccentStrong = Color(0xFF2F6F5B)
12: val BreatheGreen = Color(0xFF3A8D61)
13: val BreatheYellow = Color(0xFFC9962F)
14: val BreatheRed = Color(0xFFA84040)
15: val BreatheBorder = Color(0xFFB7CCC1)
16: val BreatheCardSurface = Color(0xFFFDFEFD)
```

## File: `android/app/src/main/java/com/breathe/presentation/theme/Type.kt`

```kotlin
1: package com.breathe.presentation.theme
2: 
3: import androidx.compose.material3.Typography
4: 
5: val BreatheTypography = Typography()
```

## File: `android/app/src/main/java/com/breathe/presentation/theme/BreatheTheme.kt`

```kotlin
1: package com.breathe.presentation.theme
2: 
3: import androidx.compose.material3.MaterialTheme
4: import androidx.compose.material3.lightColorScheme
5: import androidx.compose.runtime.Composable
6: 
7: private val LightColors = lightColorScheme(
8:   primary = BreatheAccent,
9:   secondary = BreatheGreen,
10:   tertiary = BreatheYellow,
11:   background = BreatheCanvas,
12:   surface = BreatheCardSurface,
13:   surfaceVariant = BreatheOverlay,
14:   onPrimary = BreatheCanvas,
15:   onSurface = BreatheInk
16: )
17: 
18: @Composable
19: fun BreatheTheme(content: @Composable () -> Unit) {
20:   MaterialTheme(
21:     colorScheme = LightColors,
22:     typography = BreatheTypography,
23:     content = content
24:   )
25: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/common/AppChrome.kt`

```kotlin
1: package com.breathe.presentation.ui.common
2: 
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.border
5: import androidx.compose.foundation.layout.Arrangement
6: import androidx.compose.foundation.layout.Box
7: import androidx.compose.foundation.layout.Column
8: import androidx.compose.foundation.layout.Row
9: import androidx.compose.foundation.layout.Spacer
10: import androidx.compose.foundation.layout.fillMaxSize
11: import androidx.compose.foundation.layout.fillMaxWidth
12: import androidx.compose.foundation.layout.height
13: import androidx.compose.foundation.layout.padding
14: import androidx.compose.foundation.layout.size
15: import androidx.compose.foundation.shape.CircleShape
16: import androidx.compose.foundation.shape.RoundedCornerShape
17: import androidx.compose.material3.Card
18: import androidx.compose.material3.CardDefaults
19: import androidx.compose.material3.MaterialTheme
20: import androidx.compose.material3.Text
21: import androidx.compose.runtime.Composable
22: import androidx.compose.ui.Alignment
23: import androidx.compose.ui.Modifier
24: import androidx.compose.ui.graphics.Brush
25: import androidx.compose.ui.graphics.Color
26: import androidx.compose.ui.text.font.FontWeight
27: import androidx.compose.ui.unit.dp
28: import com.breathe.domain.model.StatusLevel
29: import com.breathe.presentation.theme.BreatheAccent
30: import com.breathe.presentation.theme.BreatheAccentStrong
31: import com.breathe.presentation.theme.BreatheBorder
32: import com.breathe.presentation.theme.BreatheCanvas
33: import com.breathe.presentation.theme.BreatheCanvasSoft
34: import com.breathe.presentation.theme.BreatheCardSurface
35: import com.breathe.presentation.theme.BreatheGreen
36: import com.breathe.presentation.theme.BreatheInk
37: import com.breathe.presentation.theme.BreatheMutedInk
38: import com.breathe.presentation.theme.BreatheOverlay
39: import com.breathe.presentation.theme.BreatheRed
40: import com.breathe.presentation.theme.BreatheYellow
41: 
42: @Composable
43: fun AppScreen(
44:   title: String,
45:   subtitle: String,
46:   modifier: Modifier = Modifier,
47:   content: @Composable ColumnScopeWithSpacing.() -> Unit
48: ) {
49:   Box(
50:     modifier = modifier
51:       .fillMaxSize()
52:       .background(
53:         Brush.verticalGradient(
54:           colors = listOf(BreatheCanvas, BreatheOverlay)
55:         )
56:       )
57:   ) {
58:     Column(
59:       modifier = Modifier
60:         .fillMaxSize()
61:         .padding(horizontal = 20.dp, vertical = 24.dp),
62:       verticalArrangement = Arrangement.spacedBy(16.dp)
63:     ) {
64:       Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
65:       Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
66:       ColumnScopeWithSpacing.content()
67:     }
68:   }
69: }
70: 
71: object ColumnScopeWithSpacing
72: 
73: @Composable
74: fun BreatheCard(
75:   modifier: Modifier = Modifier,
76:   content: @Composable () -> Unit
77: ) {
78:   Card(
79:     modifier = modifier
80:       .fillMaxWidth()
81:       .border(1.dp, BreatheBorder.copy(alpha = 0.55f), RoundedCornerShape(22.dp)),
82:     colors = CardDefaults.cardColors(containerColor = BreatheCardSurface),
83:     shape = RoundedCornerShape(22.dp),
84:     elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
85:   ) {
86:     Box(modifier = Modifier.padding(18.dp)) {
87:       content()
88:     }
89:   }
90: }
91: 
92: @Composable
93: fun StatusPill(label: String, status: StatusLevel?, modifier: Modifier = Modifier) {
94:   val (bg, fg) = when (status) {
95:     StatusLevel.GREEN -> BreatheGreen.copy(alpha = 0.15f) to Color(0xFF245739)
96:     StatusLevel.YELLOW -> BreatheYellow.copy(alpha = 0.20f) to Color(0xFF6B4F18)
97:     StatusLevel.RED -> BreatheRed.copy(alpha = 0.15f) to Color(0xFF6F2D2D)
98:     null -> BreatheAccent.copy(alpha = 0.08f) to BreatheMutedInk
99:   }
100: 
101:   Row(
102:     modifier = modifier
103:       .background(bg, RoundedCornerShape(999.dp))
104:       .border(1.dp, BreatheBorder.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
105:       .padding(horizontal = 12.dp, vertical = 8.dp),
106:     verticalAlignment = Alignment.CenterVertically,
107:     horizontalArrangement = Arrangement.spacedBy(8.dp)
108:   ) {
109:     Box(
110:       modifier = Modifier
111:         .size(9.dp)
112:         .background(fg, CircleShape)
113:     )
114:     Text(label, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
115:   }
116: }
117: 
118: @Composable
119: fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
120:   Column(modifier = modifier) {
121:     Text(label, style = MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
122:     Spacer(modifier = Modifier.height(4.dp))
123:     Text(value, style = MaterialTheme.typography.titleMedium, color = BreatheInk, fontWeight = FontWeight.SemiBold)
124:   }
125: }
126: 
127: @Composable
128: fun SectionTitle(text: String) {
129:   Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = BreatheAccentStrong)
130: }
```

## File: `android/app/src/main/java/com/breathe/presentation/navigation/Screen.kt`

```kotlin
1: package com.breathe.presentation.navigation
2: 
3: sealed class Screen(val route: String, val title: String) {
4:   data object Loading : Screen("loading", "Loading")
5:   data object Pairing : Screen("pairing", "Pairing")
6:   data object Home : Screen("home", "Home")
7:   data object Calm : Screen("calm", "Calm")
8:   data object Timeout : Screen("timeout", "Timeout")
9:   data object Status : Screen("status", "Status")
10:   data object Voice : Screen("voice", "Voice Studio")
11:   data object Log : Screen("log", "Conflict Log")
12:   data object Insights : Screen("insights", "Insights")
13: }
```

## File: `android/app/src/main/java/com/breathe/presentation/navigation/AppEntryViewModel.kt`

```kotlin
1: package com.breathe.presentation.navigation
2: 
3: import androidx.lifecycle.ViewModel
4: import androidx.lifecycle.viewModelScope
5: import com.breathe.domain.usecase.BootstrapSessionUseCase
6: import dagger.hilt.android.lifecycle.HiltViewModel
7: import javax.inject.Inject
8: import kotlinx.coroutines.flow.MutableStateFlow
9: import kotlinx.coroutines.flow.StateFlow
10: import kotlinx.coroutines.flow.asStateFlow
11: import kotlinx.coroutines.flow.collect
12: import kotlinx.coroutines.flow.update
13: import kotlinx.coroutines.launch
14: 
15: data class AppEntryUiState(
16:   val isLoading: Boolean = true,
17:   val targetRoute: String? = null
18: )
19: 
20: @HiltViewModel
21: class AppEntryViewModel @Inject constructor(
22:   private val bootstrapSessionUseCase: BootstrapSessionUseCase
23: ) : ViewModel() {
24:   private val _uiState = MutableStateFlow(AppEntryUiState())
25:   val uiState: StateFlow<AppEntryUiState> = _uiState.asStateFlow()
26: 
27:   init {
28:     viewModelScope.launch {
29:       bootstrapSessionUseCase.bootstrap()
30:       bootstrapSessionUseCase().collect { session ->
31:         val targetRoute = if (session.isOfflineMode || (session.hasToken && session.isPaired && session.pairingCode.isNullOrBlank())) {
32:           Screen.Home.route
33:         } else {
34:           Screen.Pairing.route
35:         }
36: 
37:         _uiState.update {
38:           it.copy(isLoading = false, targetRoute = targetRoute)
39:         }
40:       }
41:     }
42:   }
43: }
```

## File: `android/app/src/main/java/com/breathe/presentation/navigation/LoadingScreen.kt`

```kotlin
1: package com.breathe.presentation.navigation
2: 
3: import androidx.compose.foundation.layout.Arrangement
4: import androidx.compose.foundation.layout.Column
5: import androidx.compose.foundation.layout.fillMaxSize
6: import androidx.compose.foundation.layout.padding
7: import androidx.compose.material3.CircularProgressIndicator
8: import androidx.compose.material3.Text
9: import androidx.compose.runtime.Composable
10: import androidx.compose.ui.Modifier
11: import androidx.compose.ui.unit.dp
12: 
13: @Composable
14: fun LoadingScreen() {
15:   Column(
16:     modifier = Modifier
17:       .fillMaxSize()
18:       .padding(24.dp),
19:     verticalArrangement = Arrangement.spacedBy(16.dp)
20:   ) {
21:     CircularProgressIndicator()
22:     Text("Checking your private Breathe session...")
23:   }
24: }
```

## File: `android/app/src/main/java/com/breathe/presentation/navigation/NavGraph.kt`

```kotlin
1: package com.breathe.presentation.navigation
2: 
3: import androidx.compose.runtime.Composable
4: import androidx.compose.runtime.LaunchedEffect
5: import androidx.compose.runtime.getValue
6: import androidx.hilt.navigation.compose.hiltViewModel
7: import androidx.navigation.compose.NavHost
8: import androidx.navigation.compose.composable
9: import androidx.navigation.compose.rememberNavController
10: import androidx.lifecycle.compose.collectAsStateWithLifecycle
11: import com.breathe.presentation.ui.calm.CalmScreen
12: import com.breathe.presentation.ui.calm.CalmViewModel
13: import com.breathe.presentation.ui.home.HomeScreen
14: import com.breathe.presentation.ui.home.HomeViewModel
15: import com.breathe.presentation.ui.insights.InsightsScreen
16: import com.breathe.presentation.ui.insights.InsightsViewModel
17: import com.breathe.presentation.ui.log.ConflictLogScreen
18: import com.breathe.presentation.ui.log.LogViewModel
19: import com.breathe.presentation.ui.pair.PairingScreen
20: import com.breathe.presentation.ui.pair.PairingViewModel
21: import com.breathe.presentation.ui.status.StatusScreen
22: import com.breathe.presentation.ui.status.StatusViewModel
23: import com.breathe.presentation.ui.timeout.TimeoutScreen
24: import com.breathe.presentation.ui.timeout.TimeoutViewModel
25: import com.breathe.presentation.ui.voice.VoiceStudioScreen
26: import com.breathe.presentation.ui.voice.VoiceViewModel
27: 
28: @Composable
29: fun BreatheNavGraph() {
30:   val navController = rememberNavController()
31:   val appEntryViewModel = hiltViewModel<AppEntryViewModel>()
32:   val appEntryState by appEntryViewModel.uiState.collectAsStateWithLifecycle()
33: 
34:   NavHost(
35:     navController = navController,
36:     startDestination = Screen.Loading.route
37:   ) {
38:     composable(Screen.Loading.route) {
39:       LaunchedEffect(appEntryState.isLoading, appEntryState.targetRoute) {
40:         val targetRoute = appEntryState.targetRoute
41:         if (!appEntryState.isLoading && targetRoute != null) {
42:           navController.navigate(targetRoute) {
43:             popUpTo(Screen.Loading.route) { inclusive = true }
44:             launchSingleTop = true
45:           }
46:         }
47:       }
48: 
49:       LoadingScreen()
50:     }
51: 
52:     composable(Screen.Pairing.route) {
53:       PairingScreen(
54:         viewModel = hiltViewModel<PairingViewModel>(),
55:         onContinue = { navController.navigate(Screen.Home.route) }
56:       )
57:     }
58: 
59:     composable(Screen.Home.route) {
60:       HomeScreen(
61:         viewModel = hiltViewModel<HomeViewModel>(),
62:         onNavigate = { route -> navController.navigate(route) }
63:       )
64:     }
65: 
66:     composable(Screen.Calm.route) {
67:       CalmScreen(viewModel = hiltViewModel<CalmViewModel>())
68:     }
69: 
70:     composable(Screen.Timeout.route) {
71:       TimeoutScreen(viewModel = hiltViewModel<TimeoutViewModel>())
72:     }
73: 
74:     composable(Screen.Status.route) {
75:       StatusScreen(viewModel = hiltViewModel<StatusViewModel>())
76:     }
77: 
78:     composable(Screen.Voice.route) {
79:       VoiceStudioScreen(viewModel = hiltViewModel<VoiceViewModel>())
80:     }
81: 
82:     composable(Screen.Log.route) {
83:       ConflictLogScreen(viewModel = hiltViewModel<LogViewModel>())
84:     }
85: 
86:     composable(Screen.Insights.route) {
87:       InsightsScreen(viewModel = hiltViewModel<InsightsViewModel>())
88:     }
89:   }
90: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/home/HomeScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.home
2: 
3: import androidx.compose.foundation.layout.Arrangement
4: import androidx.compose.foundation.layout.Column
5: import androidx.compose.foundation.layout.fillMaxWidth
6: import androidx.compose.foundation.layout.Row
7: import androidx.compose.material3.Button
8: import androidx.compose.material3.ButtonDefaults
9: import androidx.compose.material3.Text
10: import androidx.compose.runtime.Composable
11: import androidx.compose.runtime.getValue
12: import androidx.compose.ui.Modifier
13: import androidx.compose.ui.unit.dp
14: import androidx.hilt.navigation.compose.hiltViewModel
15: import androidx.lifecycle.ViewModel
16: import androidx.lifecycle.compose.collectAsStateWithLifecycle
17: import androidx.lifecycle.viewModelScope
18: import com.breathe.domain.model.StatusLevel
19: import com.breathe.domain.repository.StatusRepository
20: import com.breathe.presentation.navigation.Screen
21: import com.breathe.presentation.theme.BreatheAccentStrong
22: import com.breathe.presentation.theme.BreatheCanvas
23: import com.breathe.presentation.ui.common.AppScreen
24: import com.breathe.presentation.ui.common.BreatheCard
25: import com.breathe.presentation.ui.common.MiniStat
26: import com.breathe.presentation.ui.common.SectionTitle
27: import com.breathe.presentation.ui.common.StatusPill
28: import dagger.hilt.android.lifecycle.HiltViewModel
29: import kotlinx.coroutines.flow.SharingStarted
30: import kotlinx.coroutines.flow.StateFlow
31: import kotlinx.coroutines.flow.combine
32: import kotlinx.coroutines.flow.stateIn
33: import javax.inject.Inject
34: 
35: data class HomeUiState(
36:   val ownStatus: StatusLevel? = null,
37:   val partnerStatus: StatusLevel? = null,
38:   val wsConnected: Boolean = false
39: )
40: 
41: sealed interface HomeUiEvent {
42:   data object NoOp : HomeUiEvent
43: }
44: 
45: @HiltViewModel
46: class HomeViewModel @Inject constructor(
47:   statusRepository: StatusRepository
48: ) : ViewModel() {
49:   val uiState: StateFlow<HomeUiState> = combine(
50:     statusRepository.observeOwnStatus(),
51:     statusRepository.observePartnerStatus(),
52:     statusRepository.observeWsConnection()
53:   ) { own, partner, connected ->
54:     HomeUiState(ownStatus = own, partnerStatus = partner, wsConnected = connected)
55:   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
56: 
57:   fun onEvent(event: HomeUiEvent) = Unit
58: }
59: 
60: @Composable
61: fun HomeScreen(
62:   onNavigate: (String) -> Unit,
63:   viewModel: HomeViewModel = hiltViewModel()
64: ) {
65:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
66: 
67:   AppScreen(
68:     title = "Breathe",
69:     subtitle = "A calmer surface for checking in before conflict becomes momentum."
70:   ) {
71:     BreatheCard {
72:       Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
73:         SectionTitle("Live state")
74:         StatusPill(label = "You: ${uiState.ownStatus?.name ?: "Unset"}", status = uiState.ownStatus)
75:         StatusPill(label = "Partner: ${uiState.partnerStatus?.name ?: "Unknown"}", status = uiState.partnerStatus)
76:         MiniStat("Connection", if (uiState.wsConnected) "Realtime linked" else "Offline-first mode")
77:       }
78:     }
79: 
80:     BreatheCard {
81:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
82:         SectionTitle("Regulation tools")
83:         Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
84:           Button(
85:             onClick = { onNavigate(Screen.Status.route) },
86:             modifier = Modifier.weight(1f),
87:             colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
88:           ) { Text("Status") }
89:           Button(
90:             onClick = { onNavigate(Screen.Calm.route) },
91:             modifier = Modifier.weight(1f),
92:             colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
93:           ) { Text("Calm") }
94:         }
95:         Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
96:           Button(
97:             onClick = { onNavigate(Screen.Timeout.route) },
98:             modifier = Modifier.weight(1f),
99:             colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
100:           ) { Text("Timeout") }
101:           Button(
102:             onClick = { onNavigate(Screen.Voice.route) },
103:             modifier = Modifier.weight(1f),
104:             colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
105:           ) { Text("Voice") }
106:         }
107:         Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
108:           Button(
109:             onClick = { onNavigate(Screen.Log.route) },
110:             modifier = Modifier.weight(1f),
111:             colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
112:           ) { Text("Log") }
113:           Button(
114:             onClick = { onNavigate(Screen.Insights.route) },
115:             modifier = Modifier.weight(1f),
116:             colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
117:           ) { Text("Insights") }
118:         }
119:       }
120:     }
121:   }
122: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/pair/PairingScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.pair
2: 
3: import com.breathe.BuildConfig
4: import androidx.compose.foundation.layout.Arrangement
5: import androidx.compose.foundation.layout.Column
6: import androidx.compose.foundation.layout.fillMaxWidth
7: import androidx.compose.material3.Button
8: import androidx.compose.material3.ButtonDefaults
9: import androidx.compose.material3.CircularProgressIndicator
10: import androidx.compose.material3.MaterialTheme
11: import androidx.compose.material3.OutlinedTextField
12: import androidx.compose.material3.Text
13: import androidx.compose.runtime.Composable
14: import androidx.compose.runtime.LaunchedEffect
15: import androidx.compose.runtime.getValue
16: import androidx.compose.ui.Modifier
17: import androidx.compose.ui.unit.dp
18: import androidx.hilt.navigation.compose.hiltViewModel
19: import androidx.lifecycle.ViewModel
20: import androidx.lifecycle.compose.collectAsStateWithLifecycle
21: import androidx.lifecycle.viewModelScope
22: import com.breathe.domain.usecase.BootstrapSessionUseCase
23: import com.breathe.domain.usecase.ContinueOfflineUseCase
24: import com.breathe.domain.usecase.CreatePairingCodeUseCase
25: import com.breathe.domain.usecase.JoinPairUseCase
26: import com.breathe.domain.usecase.LoginUseCase
27: import com.breathe.domain.usecase.RegisterAccountUseCase
28: import com.breathe.presentation.theme.BreatheAccentStrong
29: import com.breathe.presentation.theme.BreatheCanvas
30: import com.breathe.presentation.ui.common.AppScreen
31: import com.breathe.presentation.ui.common.BreatheCard
32: import com.breathe.presentation.ui.common.SectionTitle
33: import dagger.hilt.android.lifecycle.HiltViewModel
34: import kotlinx.coroutines.flow.MutableStateFlow
35: import kotlinx.coroutines.flow.StateFlow
36: import kotlinx.coroutines.flow.asStateFlow
37: import kotlinx.coroutines.flow.collect
38: import kotlinx.coroutines.flow.update
39: import kotlinx.coroutines.launch
40: import retrofit2.HttpException
41: import javax.inject.Inject
42: 
43: data class PairingUiState(
44:   val username: String = "",
45:   val password: String = "",
46:   val pairingCode: String = "",
47:   val pairingExpiresAt: String? = null,
48:   val joinCode: String = "",
49:   val isRegistered: Boolean = false,
50:   val isPaired: Boolean = false,
51:   val isOfflineMode: Boolean = false,
52:   val isLoading: Boolean = false,
53:   val error: String? = null
54: )
55: 
56: sealed interface PairingUiEvent {
57:   data class UsernameChanged(val value: String) : PairingUiEvent
58:   data class PasswordChanged(val value: String) : PairingUiEvent
59:   data class JoinCodeChanged(val value: String) : PairingUiEvent
60:   data object ContinueOffline : PairingUiEvent
61:   data object RegisterAccount : PairingUiEvent
62:   data object SignIn : PairingUiEvent
63:   data object CreatePairingCode : PairingUiEvent
64:   data object JoinPair : PairingUiEvent
65:   data object ClearError : PairingUiEvent
66: }
67: 
68: @HiltViewModel
69: class PairingViewModel @Inject constructor(
70:   private val bootstrapSessionUseCase: BootstrapSessionUseCase,
71:   private val continueOfflineUseCase: ContinueOfflineUseCase,
72:   private val registerAccountUseCase: RegisterAccountUseCase,
73:   private val loginUseCase: LoginUseCase,
74:   private val createPairingCodeUseCase: CreatePairingCodeUseCase,
75:   private val joinPairUseCase: JoinPairUseCase
76: ) : ViewModel() {
77:   private val _uiState = MutableStateFlow(PairingUiState())
78:   val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()
79: 
80:   init {
81:     viewModelScope.launch {
82:       bootstrapSessionUseCase().collect { session ->
83:         _uiState.update {
84:           it.copy(
85:             pairingCode = session.pairingCode.orEmpty(),
86:             pairingExpiresAt = session.pairingExpiresAt,
87:             isRegistered = session.hasToken,
88:             isPaired = session.isPaired,
89:             isOfflineMode = session.isOfflineMode
90:           )
91:         }
92:       }
93:     }
94:   }
95: 
96:   fun onEvent(event: PairingUiEvent) {
97:     when (event) {
98:       is PairingUiEvent.UsernameChanged -> {
99:         _uiState.update { it.copy(username = event.value, error = null) }
100:       }
101: 
102:       is PairingUiEvent.PasswordChanged -> {
103:         _uiState.update { it.copy(password = event.value, error = null) }
104:       }
105: 
106:       is PairingUiEvent.JoinCodeChanged -> {
107:         _uiState.update { it.copy(joinCode = event.value, error = null) }
108:       }
109: 
110:       PairingUiEvent.ContinueOffline -> submitContinueOffline()
111:       PairingUiEvent.RegisterAccount -> submitRegistration()
112:       PairingUiEvent.SignIn -> submitLogin()
113:       PairingUiEvent.CreatePairingCode -> submitCreatePairingCode()
114:       PairingUiEvent.JoinPair -> submitJoinPair()
115: 
116:       PairingUiEvent.ClearError -> {
117:         _uiState.update { it.copy(error = null) }
118:       }
119:     }
120:   }
121: 
122:   private fun submitRegistration() {
123:     val username = _uiState.value.username.trim()
124:     val password = _uiState.value.password
125: 
126:     if (username.isBlank()) {
127:       _uiState.update { it.copy(error = "Enter a username.") }
128:       return
129:     }
130: 
131:     if (password.length < 6) {
132:       _uiState.update { it.copy(error = "Password must be at least 6 characters.") }
133:       return
134:     }
135: 
136:     viewModelScope.launch {
137:       setLoading(true)
138:       runCatching {
139:         registerAccountUseCase(username, password)
140:       }.onFailure { error ->
141:         _uiState.update { it.copy(error = error.toUserMessage()) }
142:       }
143:       setLoading(false)
144:     }
145:   }
146: 
147:   private fun submitCreatePairingCode() {
148:     if (!_uiState.value.isRegistered) {
149:       _uiState.update { it.copy(error = "Create your private account first.") }
150:       return
151:     }
152: 
153:     viewModelScope.launch {
154:       setLoading(true)
155:       runCatching {
156:         createPairingCodeUseCase()
157:       }.onFailure { error ->
158:         _uiState.update { it.copy(error = error.toUserMessage()) }
159:       }
160:       setLoading(false)
161:     }
162:   }
163: 
164:   private fun submitLogin() {
165:     val username = _uiState.value.username.trim()
166:     val password = _uiState.value.password
167: 
168:     if (username.isBlank()) {
169:       _uiState.update { it.copy(error = "Enter a username.") }
170:       return
171:     }
172: 
173:     if (password.isBlank()) {
174:       _uiState.update { it.copy(error = "Enter your password.") }
175:       return
176:     }
177: 
178:     viewModelScope.launch {
179:       setLoading(true)
180:       runCatching {
181:         loginUseCase(username, password)
182:       }.onFailure { error ->
183:         _uiState.update { it.copy(error = error.toUserMessage()) }
184:       }
185:       setLoading(false)
186:     }
187:   }
188: 
189:   private fun submitContinueOffline() {
190:     viewModelScope.launch {
191:       setLoading(true)
192:       runCatching {
193:         continueOfflineUseCase()
194:       }.onFailure { error ->
195:         _uiState.update { it.copy(error = error.toUserMessage()) }
196:       }
197:       setLoading(false)
198:     }
199:   }
200: 
201:   private fun submitJoinPair() {
202:     if (!_uiState.value.isRegistered) {
203:       _uiState.update { it.copy(error = "Create your private account first.") }
204:       return
205:     }
206: 
207:     val sanitized = _uiState.value.joinCode.filter(Char::isDigit)
208:     if (sanitized.length != 6) {
209:       _uiState.update { it.copy(error = "Enter a 6-digit pairing code.") }
210:       return
211:     }
212: 
213:     viewModelScope.launch {
214:       setLoading(true)
215:       runCatching {
216:         joinPairUseCase(sanitized)
217:       }.onFailure { error ->
218:         _uiState.update { it.copy(error = error.toUserMessage()) }
219:       }
220:       setLoading(false)
221:     }
222:   }
223: 
224:   private fun setLoading(isLoading: Boolean) {
225:     _uiState.update { it.copy(isLoading = isLoading) }
226:   }
227: 
228:   private fun Throwable.toUserMessage(): String {
229:     return when (this) {
230:       is HttpException -> "Server request failed (${code()})."
231:       else -> message ?: "Something went wrong."
232:     }
233:   }
234: }
235: 
236: @Composable
237: fun PairingScreen(
238:   onContinue: () -> Unit,
239:   viewModel: PairingViewModel = hiltViewModel()
240: ) {
241:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
242: 
243:   LaunchedEffect(uiState.isPaired, uiState.pairingCode, uiState.isOfflineMode) {
244:     if (uiState.isOfflineMode || (uiState.isPaired && uiState.pairingCode.isBlank())) {
245:       onContinue()
246:     }
247:   }
248: 
249:   AppScreen(
250:     title = "Pair while calm",
251:     subtitle = "Set up trust before conflict. Use a private account, a pairing code, or continue offline while features are still under construction."
252:   ) {
253:     BreatheCard {
254:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
255:         SectionTitle("Private account")
256:         OutlinedTextField(
257:           modifier = Modifier.fillMaxWidth(),
258:           value = uiState.username,
259:           onValueChange = { viewModel.onEvent(PairingUiEvent.UsernameChanged(it)) },
260:           label = { Text("Username") }
261:         )
262: 
263:         OutlinedTextField(
264:           modifier = Modifier.fillMaxWidth(),
265:           value = uiState.password,
266:           onValueChange = { viewModel.onEvent(PairingUiEvent.PasswordChanged(it)) },
267:           label = { Text("Password") }
268:         )
269: 
270:         Button(
271:           onClick = { viewModel.onEvent(PairingUiEvent.RegisterAccount) },
272:           enabled = !uiState.isLoading && !uiState.isRegistered,
273:           colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
274:         ) {
275:           Text(if (uiState.isRegistered) "Account ready" else "Create private account")
276:         }
277: 
278:         Button(
279:           onClick = { viewModel.onEvent(PairingUiEvent.SignIn) },
280:           enabled = !uiState.isLoading && !uiState.isRegistered
281:         ) {
282:           Text("Sign in")
283:         }
284: 
285:         if (BuildConfig.DEBUG) {
286:           Button(
287:             onClick = { viewModel.onEvent(PairingUiEvent.ContinueOffline) },
288:             enabled = !uiState.isLoading
289:           ) {
290:             Text("Continue offline")
291:           }
292:         }
293:       }
294:     }
295: 
296:     BreatheCard {
297:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
298:         SectionTitle("Pairing code")
299:         Button(
300:           onClick = { viewModel.onEvent(PairingUiEvent.CreatePairingCode) },
301:           enabled = !uiState.isLoading && uiState.isRegistered,
302:           colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
303:         ) {
304:           Text(if (uiState.pairingCode.isBlank()) "Create pairing code" else "Refresh pairing code")
305:         }
306: 
307:         if (uiState.pairingCode.isNotBlank()) {
308:           Text("Your code: ${uiState.pairingCode}", style = MaterialTheme.typography.titleMedium)
309:           uiState.pairingExpiresAt?.let { Text("Expires at: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
310:         }
311: 
312:         OutlinedTextField(
313:           modifier = Modifier.fillMaxWidth(),
314:           value = uiState.joinCode,
315:           onValueChange = { viewModel.onEvent(PairingUiEvent.JoinCodeChanged(it)) },
316:           label = { Text("Join code") }
317:         )
318: 
319:         Button(
320:           onClick = { viewModel.onEvent(PairingUiEvent.JoinPair) },
321:           enabled = !uiState.isLoading && uiState.isRegistered
322:         ) {
323:           Text("Join pair")
324:         }
325: 
326:         if (uiState.isRegistered && (uiState.isPaired || uiState.pairingCode.isNotBlank() || uiState.isOfflineMode)) {
327:           Button(onClick = onContinue, enabled = !uiState.isLoading) {
328:             Text("Continue to home")
329:           }
330:         }
331:       }
332:     }
333: 
334:     if (uiState.isLoading) {
335:       CircularProgressIndicator()
336:     }
337: 
338:     uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
339:   }
340: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/calm/CalmScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.calm
2: 
3: import androidx.compose.foundation.layout.Arrangement
4: import androidx.compose.foundation.layout.Column
5: import androidx.compose.material3.Button
6: import androidx.compose.material3.ButtonDefaults
7: import androidx.compose.material3.Text
8: import androidx.compose.runtime.Composable
9: import androidx.compose.runtime.getValue
10: import androidx.compose.ui.Modifier
11: import androidx.compose.ui.unit.dp
12: import androidx.hilt.navigation.compose.hiltViewModel
13: import androidx.lifecycle.ViewModel
14: import androidx.lifecycle.compose.collectAsStateWithLifecycle
15: import androidx.lifecycle.viewModelScope
16: import com.breathe.domain.model.CalmSession
17: import com.breathe.domain.model.SessionFeature
18: import com.breathe.domain.repository.SessionRepository
19: import com.breathe.domain.usecase.EndSessionUseCase
20: import com.breathe.domain.usecase.SendPeaceUseCase
21: import com.breathe.domain.usecase.StartSessionUseCase
22: import com.breathe.presentation.theme.BreatheAccentStrong
23: import com.breathe.presentation.theme.BreatheCanvas
24: import com.breathe.presentation.ui.common.AppScreen
25: import com.breathe.presentation.ui.common.BreatheCard
26: import com.breathe.presentation.ui.common.MiniStat
27: import com.breathe.presentation.ui.common.SectionTitle
28: import dagger.hilt.android.lifecycle.HiltViewModel
29: import kotlinx.coroutines.Job
30: import kotlinx.coroutines.delay
31: import kotlinx.coroutines.flow.MutableStateFlow
32: import kotlinx.coroutines.flow.SharingStarted
33: import kotlinx.coroutines.flow.StateFlow
34: import kotlinx.coroutines.flow.asStateFlow
35: import kotlinx.coroutines.flow.collect
36: import kotlinx.coroutines.flow.update
37: import kotlinx.coroutines.launch
38: import javax.inject.Inject
39: 
40: data class CalmUiState(
41:   val secondsRemaining: Int = 0,
42:   val voiceTrack: String? = null,
43:   val sessionId: Long? = null,
44:   val isActive: Boolean = false
45: )
46: 
47: sealed interface CalmUiEvent {
48:   data object StartSession : CalmUiEvent
49:   data object CompleteSession : CalmUiEvent
50:   data object SendPeace : CalmUiEvent
51: }
52: 
53: @HiltViewModel
54: class CalmViewModel @Inject constructor(
55:   sessionRepository: SessionRepository,
56:   private val startSessionUseCase: StartSessionUseCase,
57:   private val endSessionUseCase: EndSessionUseCase,
58:   private val sendPeaceUseCase: SendPeaceUseCase
59: ) : ViewModel() {
60:   private val _uiState = MutableStateFlow(CalmUiState())
61:   val uiState: StateFlow<CalmUiState> = _uiState.asStateFlow()
62:   private var countdownJob: Job? = null
63: 
64:   init {
65:     viewModelScope.launch {
66:       sessionRepository.observeActiveCalmSession().collect { session ->
67:         countdownJob?.cancel()
68:         if (session == null) {
69:           _uiState.value = CalmUiState()
70:         } else {
71:           _uiState.value = session.toUiState()
72:           startCountdown(session.sessionId, session.secondsRemaining)
73:         }
74:       }
75:     }
76:   }
77: 
78:   fun onEvent(event: CalmUiEvent) {
79:     when (event) {
80:       CalmUiEvent.StartSession -> viewModelScope.launch { startSessionUseCase(SessionFeature.CALM) }
81:       CalmUiEvent.CompleteSession -> viewModelScope.launch {
82:         _uiState.value.sessionId?.let { endSessionUseCase(it) }
83:       }
84:       CalmUiEvent.SendPeace -> viewModelScope.launch { sendPeaceUseCase() }
85:     }
86:   }
87: 
88:   private fun startCountdown(sessionId: Long?, initialSeconds: Int) {
89:     if (sessionId == null) {
90:       return
91:     }
92: 
93:     countdownJob = viewModelScope.launch {
94:       var remaining = initialSeconds
95:       while (remaining > 0) {
96:         delay(1_000)
97:         remaining -= 1
98:         _uiState.update {
99:           if (it.sessionId == sessionId) {
100:             it.copy(secondsRemaining = remaining.coerceAtLeast(0))
101:           } else {
102:             it
103:           }
104:         }
105:       }
106:       endSessionUseCase(sessionId)
107:     }
108:   }
109: 
110:   private fun CalmSession?.toUiState(): CalmUiState = CalmUiState(
111:     secondsRemaining = this?.secondsRemaining ?: 0,
112:     voiceTrack = this?.voiceTrack,
113:     sessionId = this?.sessionId,
114:     isActive = this?.sessionId != null && (this.secondsRemaining > 0)
115:   )
116: }
117: 
118: @Composable
119: fun CalmScreen(viewModel: CalmViewModel = hiltViewModel()) {
120:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
121: 
122:   AppScreen(
123:     title = "Calm session",
124:     subtitle = "A soft reset before words get faster than your nervous system can handle."
125:   ) {
126:     BreatheCard {
127:       Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
128:         SectionTitle("Session state")
129:         MiniStat("Seconds remaining", uiState.secondsRemaining.toString())
130:         MiniStat("Voice track", uiState.voiceTrack ?: "Not selected")
131:       }
132:     }
133: 
134:     BreatheCard {
135:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
136:         SectionTitle("Actions")
137:         Button(
138:           onClick = { viewModel.onEvent(CalmUiEvent.StartSession) },
139:           enabled = !uiState.isActive,
140:           colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
141:         ) {
142:           Text(if (uiState.isActive) "Calm session active" else "Start calm session")
143:         }
144:         if (uiState.isActive) {
145:           Button(onClick = { viewModel.onEvent(CalmUiEvent.CompleteSession) }) { Text("Complete calm session") }
146:         }
147:         Button(onClick = { viewModel.onEvent(CalmUiEvent.SendPeace) }) { Text("Send peace") }
148:       }
149:     }
150:   }
151: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/timeout/TimeoutScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.timeout
2: 
3: import androidx.compose.foundation.layout.Arrangement
4: import androidx.compose.foundation.layout.Column
5: import androidx.compose.material3.Button
6: import androidx.compose.material3.ButtonDefaults
7: import androidx.compose.material3.Text
8: import androidx.compose.runtime.Composable
9: import androidx.compose.runtime.getValue
10: import androidx.compose.ui.Modifier
11: import androidx.compose.ui.unit.dp
12: import androidx.hilt.navigation.compose.hiltViewModel
13: import androidx.lifecycle.ViewModel
14: import androidx.lifecycle.compose.collectAsStateWithLifecycle
15: import androidx.lifecycle.viewModelScope
16: import com.breathe.domain.model.SessionFeature
17: import com.breathe.domain.model.TimeoutLock
18: import com.breathe.domain.repository.SessionRepository
19: import com.breathe.domain.usecase.EndSessionUseCase
20: import com.breathe.domain.usecase.StartSessionUseCase
21: import com.breathe.presentation.theme.BreatheAccentStrong
22: import com.breathe.presentation.theme.BreatheCanvas
23: import com.breathe.presentation.ui.common.AppScreen
24: import com.breathe.presentation.ui.common.BreatheCard
25: import com.breathe.presentation.ui.common.MiniStat
26: import com.breathe.presentation.ui.common.SectionTitle
27: import dagger.hilt.android.lifecycle.HiltViewModel
28: import kotlinx.coroutines.Job
29: import kotlinx.coroutines.delay
30: import kotlinx.coroutines.flow.MutableStateFlow
31: import kotlinx.coroutines.flow.StateFlow
32: import kotlinx.coroutines.flow.asStateFlow
33: import kotlinx.coroutines.flow.collect
34: import kotlinx.coroutines.flow.update
35: import kotlinx.coroutines.launch
36: import javax.inject.Inject
37: 
38: data class TimeoutUiState(
39:   val sessionId: Long? = null,
40:   val secondsRemaining: Int = 0,
41:   val isLocked: Boolean = false,
42:   val unlocksAt: String? = null
43: )
44: 
45: sealed interface TimeoutUiEvent {
46:   data object StartTimeout : TimeoutUiEvent
47: }
48: 
49: @HiltViewModel
50: class TimeoutViewModel @Inject constructor(
51:   sessionRepository: SessionRepository,
52:   private val startSessionUseCase: StartSessionUseCase,
53:   private val endSessionUseCase: EndSessionUseCase
54: ) : ViewModel() {
55:   private val _uiState = MutableStateFlow(TimeoutUiState())
56:   val uiState: StateFlow<TimeoutUiState> = _uiState.asStateFlow()
57:   private var countdownJob: Job? = null
58: 
59:   init {
60:     viewModelScope.launch {
61:       sessionRepository.observeTimeoutLock().collect { lock ->
62:         countdownJob?.cancel()
63:         _uiState.value = lock.toUiState()
64:         if (lock.sessionId != null && lock.isLocked) {
65:           startCountdown(lock.sessionId, lock.secondsRemaining)
66:         }
67:       }
68:     }
69:   }
70: 
71:   fun onEvent(event: TimeoutUiEvent) {
72:     when (event) {
73:       TimeoutUiEvent.StartTimeout -> viewModelScope.launch { startSessionUseCase(SessionFeature.TIMEOUT) }
74:     }
75:   }
76: 
77:   private fun startCountdown(sessionId: Long, initialSeconds: Int) {
78:     countdownJob = viewModelScope.launch {
79:       var remaining = initialSeconds
80:       while (remaining > 0) {
81:         delay(1_000)
82:         remaining -= 1
83:         _uiState.update {
84:           if (it.sessionId == sessionId) {
85:             it.copy(secondsRemaining = remaining.coerceAtLeast(0), isLocked = remaining > 0)
86:           } else {
87:             it
88:           }
89:         }
90:       }
91:       endSessionUseCase(sessionId)
92:     }
93:   }
94: 
95:   private fun TimeoutLock.toUiState(): TimeoutUiState = TimeoutUiState(
96:     sessionId = sessionId,
97:     secondsRemaining = secondsRemaining,
98:     isLocked = isLocked,
99:     unlocksAt = unlocksAt
100:   )
101: }
102: 
103: @Composable
104: fun TimeoutScreen(viewModel: TimeoutViewModel = hiltViewModel()) {
105:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
106: 
107:   AppScreen(
108:     title = "Structured timeout",
109:     subtitle = "A hard pause for your body first, problem-solving later."
110:   ) {
111:     BreatheCard {
112:       Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
113:         SectionTitle("Lock state")
114:         MiniStat("Locked", if (uiState.isLocked) "Yes" else "No")
115:         MiniStat("Seconds remaining", uiState.secondsRemaining.toString())
116:         MiniStat("Unlocks at", uiState.unlocksAt ?: "Not active")
117:       }
118:     }
119: 
120:     BreatheCard {
121:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
122:         SectionTitle("Actions")
123:         Button(
124:           onClick = { viewModel.onEvent(TimeoutUiEvent.StartTimeout) },
125:           enabled = !uiState.isLocked,
126:           colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
127:         ) {
128:           Text(if (uiState.isLocked) "Timeout in progress" else "Start timeout")
129:         }
130:         if (!uiState.isLocked && uiState.sessionId != null) {
131:           Text("Re-entry window is open. Move slowly before restarting a timeout.")
132:         }
133:       }
134:     }
135:   }
136: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/status/StatusScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.status
2: 
3: import androidx.compose.foundation.layout.Arrangement
4: import androidx.compose.foundation.layout.Column
5: import androidx.compose.foundation.layout.Row
6: import androidx.compose.foundation.layout.fillMaxWidth
7: import androidx.compose.material3.Button
8: import androidx.compose.material3.ButtonDefaults
9: import androidx.compose.material3.Text
10: import androidx.compose.runtime.Composable
11: import androidx.compose.runtime.getValue
12: import androidx.compose.ui.Modifier
13: import androidx.compose.ui.unit.dp
14: import androidx.hilt.navigation.compose.hiltViewModel
15: import androidx.lifecycle.ViewModel
16: import androidx.lifecycle.compose.collectAsStateWithLifecycle
17: import androidx.lifecycle.viewModelScope
18: import com.breathe.domain.model.StatusLevel
19: import com.breathe.domain.repository.StatusRepository
20: import com.breathe.domain.usecase.SetStatusUseCase
21: import com.breathe.presentation.theme.BreatheAccentStrong
22: import com.breathe.presentation.theme.BreatheCanvas
23: import com.breathe.presentation.ui.common.AppScreen
24: import com.breathe.presentation.ui.common.BreatheCard
25: import com.breathe.presentation.ui.common.MiniStat
26: import com.breathe.presentation.ui.common.SectionTitle
27: import com.breathe.presentation.ui.common.StatusPill
28: import dagger.hilt.android.lifecycle.HiltViewModel
29: import kotlinx.coroutines.flow.SharingStarted
30: import kotlinx.coroutines.flow.StateFlow
31: import kotlinx.coroutines.flow.combine
32: import kotlinx.coroutines.flow.stateIn
33: import kotlinx.coroutines.launch
34: import javax.inject.Inject
35: 
36: data class StatusUiState(
37:   val selectedStatus: StatusLevel? = null,
38:   val partnerStatus: StatusLevel? = null,
39:   val wsConnected: Boolean = false
40: )
41: 
42: sealed interface StatusUiEvent {
43:   data class SelectStatus(val status: StatusLevel) : StatusUiEvent
44: }
45: 
46: @HiltViewModel
47: class StatusViewModel @Inject constructor(
48:   statusRepository: StatusRepository,
49:   private val setStatusUseCase: SetStatusUseCase
50: ) : ViewModel() {
51:   val uiState: StateFlow<StatusUiState> = combine(
52:     statusRepository.observeOwnStatus(),
53:     statusRepository.observePartnerStatus(),
54:     statusRepository.observeWsConnection()
55:   ) { own, partner, connected ->
56:     StatusUiState(selectedStatus = own, partnerStatus = partner, wsConnected = connected)
57:   }
58:     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatusUiState())
59: 
60:   fun onEvent(event: StatusUiEvent) {
61:     when (event) {
62:       is StatusUiEvent.SelectStatus -> viewModelScope.launch { setStatusUseCase(event.status) }
63:     }
64:   }
65: }
66: 
67: @Composable
68: fun StatusScreen(viewModel: StatusViewModel = hiltViewModel()) {
69:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
70: 
71:   AppScreen(
72:     title = "Status check-in",
73:     subtitle = "Signal your nervous-system state early so the next step can fit the moment."
74:   ) {
75:     BreatheCard {
76:       Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
77:         SectionTitle("Live state")
78:         StatusPill("You: ${uiState.selectedStatus?.name ?: "Unset"}", uiState.selectedStatus)
79:         StatusPill("Partner: ${uiState.partnerStatus?.name ?: "Unknown"}", uiState.partnerStatus)
80:         MiniStat("Sync", if (uiState.wsConnected) "Realtime linked" else "Offline-first mode")
81:       }
82:     }
83: 
84:     BreatheCard {
85:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
86:         SectionTitle("Set your status")
87:         Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
88:           Button(
89:             onClick = { viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.GREEN)) },
90:             modifier = Modifier.weight(1f),
91:             colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
92:           ) { Text("Green") }
93:           Button(
94:             onClick = { viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.YELLOW)) },
95:             modifier = Modifier.weight(1f)
96:           ) { Text("Yellow") }
97:           Button(
98:             onClick = { viewModel.onEvent(StatusUiEvent.SelectStatus(StatusLevel.RED)) },
99:             modifier = Modifier.weight(1f)
100:           ) { Text("Red") }
101:         }
102: 
103:         Text(
104:           when (uiState.selectedStatus) {
105:             StatusLevel.GREEN -> "Green means open, steady, and able to stay in contact."
106:             StatusLevel.YELLOW -> "Yellow means tension is rising and you need slower, safer wording."
107:             StatusLevel.RED -> "Red means stop pushing content and switch to regulation or timeout."
108:             null -> "Choose the closest status first. The goal is clarity, not perfection."
109:           }
110:         )
111:       }
112:     }
113:   }
114: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/voice/VoiceStudioScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.voice
2: 
3: import androidx.compose.foundation.layout.Arrangement
4: import androidx.compose.foundation.layout.Column
5: import androidx.compose.foundation.layout.fillMaxWidth
6: import androidx.compose.material3.Button
7: import androidx.compose.material3.ButtonDefaults
8: import androidx.compose.material3.Text
9: import androidx.compose.runtime.Composable
10: import androidx.compose.runtime.getValue
11: import androidx.compose.ui.Modifier
12: import androidx.compose.ui.unit.dp
13: import androidx.hilt.navigation.compose.hiltViewModel
14: import androidx.lifecycle.ViewModel
15: import androidx.lifecycle.compose.collectAsStateWithLifecycle
16: import androidx.lifecycle.viewModelScope
17: import com.breathe.domain.model.VoicePrompt
18: import com.breathe.domain.repository.VoiceRepository
19: import com.breathe.presentation.theme.BreatheAccentStrong
20: import com.breathe.presentation.theme.BreatheCanvas
21: import com.breathe.presentation.ui.common.AppScreen
22: import com.breathe.presentation.ui.common.BreatheCard
23: import com.breathe.presentation.ui.common.MiniStat
24: import com.breathe.presentation.ui.common.SectionTitle
25: import dagger.hilt.android.lifecycle.HiltViewModel
26: import javax.inject.Inject
27: import kotlinx.coroutines.flow.MutableStateFlow
28: import kotlinx.coroutines.flow.SharingStarted
29: import kotlinx.coroutines.flow.StateFlow
30: import kotlinx.coroutines.flow.combine
31: import kotlinx.coroutines.flow.stateIn
32: import kotlinx.coroutines.flow.update
33: 
34: data class VoiceUiState(
35:   val prompts: List<VoicePrompt> = emptyList(),
36:   val isRecording: Boolean = false,
37:   val currentSlot: Int? = null,
38:   val currentPromptLabel: String? = null
39: )
40: 
41: sealed interface VoiceUiEvent {
42:   data class SelectSlot(val slot: Int) : VoiceUiEvent
43:   data object ToggleRecording : VoiceUiEvent
44: }
45: 
46: @HiltViewModel
47: class VoiceViewModel @Inject constructor(
48:   voiceRepository: VoiceRepository
49: ) : ViewModel() {
50:   private val selectedSlot = MutableStateFlow<Int?>(null)
51:   private val recording = MutableStateFlow(false)
52: 
53:   val uiState: StateFlow<VoiceUiState> = combine(
54:     voiceRepository.observePrompts(),
55:     selectedSlot,
56:     recording
57:   ) { prompts, slot, isRecording ->
58:     val selectedPrompt = prompts.firstOrNull { it.slot == slot }
59:     VoiceUiState(
60:       prompts = prompts,
61:       isRecording = isRecording,
62:       currentSlot = slot,
63:       currentPromptLabel = selectedPrompt?.label
64:     )
65:   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VoiceUiState())
66: 
67:   fun onEvent(event: VoiceUiEvent) {
68:     when (event) {
69:       is VoiceUiEvent.SelectSlot -> {
70:         selectedSlot.update { event.slot }
71:         recording.update { false }
72:       }
73: 
74:       VoiceUiEvent.ToggleRecording -> {
75:         if (uiState.value.currentSlot != null) {
76:           recording.update { !it }
77:         }
78:       }
79:     }
80:   }
81: }
82: 
83: @Composable
84: fun VoiceStudioScreen(viewModel: VoiceViewModel = hiltViewModel()) {
85:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
86: 
87:   AppScreen(
88:     title = "Voice Studio",
89:     subtitle = "Prepare warm, regulating phrases while calm so your future self does not have to invent them in the middle of stress."
90:   ) {
91:     BreatheCard {
92:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
93:         SectionTitle("Current take")
94:         MiniStat("Recording", if (uiState.isRecording) "In progress" else "Idle")
95:         MiniStat("Slot", uiState.currentSlot?.toString() ?: "None")
96:         Text(uiState.currentPromptLabel ?: "Choose a slot below to preview the prompt you want to record.")
97:         Button(
98:           onClick = { viewModel.onEvent(VoiceUiEvent.ToggleRecording) },
99:           enabled = uiState.currentSlot != null,
100:           colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
101:         ) {
102:           Text(if (uiState.isRecording) "Stop recording" else "Start recording")
103:         }
104:       }
105:     }
106: 
107:     BreatheCard {
108:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
109:         SectionTitle("Prompt slots")
110:         uiState.prompts.forEach { prompt ->
111:           Button(
112:             onClick = { viewModel.onEvent(VoiceUiEvent.SelectSlot(prompt.slot)) },
113:             modifier = Modifier.fillMaxWidth(),
114:             colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
115:           ) {
116:             Text("Slot ${prompt.slot}: ${prompt.label}")
117:           }
118:         }
119:       }
120:     }
121:   }
122: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/log/ConflictLogScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.log
2: 
3: import androidx.compose.foundation.layout.Arrangement
4: import androidx.compose.foundation.layout.Column
5: import androidx.compose.foundation.layout.fillMaxWidth
6: import androidx.compose.foundation.lazy.LazyColumn
7: import androidx.compose.foundation.lazy.items
8: import androidx.compose.material3.Button
9: import androidx.compose.material3.ButtonDefaults
10: import androidx.compose.material3.Text
11: import androidx.compose.runtime.Composable
12: import androidx.compose.runtime.getValue
13: import androidx.compose.ui.Modifier
14: import androidx.compose.ui.unit.dp
15: import androidx.hilt.navigation.compose.hiltViewModel
16: import androidx.lifecycle.ViewModel
17: import androidx.lifecycle.compose.collectAsStateWithLifecycle
18: import androidx.lifecycle.viewModelScope
19: import com.breathe.domain.model.ConflictLogEntry
20: import com.breathe.domain.repository.SessionRepository
21: import com.breathe.presentation.theme.BreatheAccentStrong
22: import com.breathe.presentation.theme.BreatheCanvas
23: import com.breathe.presentation.ui.common.AppScreen
24: import com.breathe.presentation.ui.common.BreatheCard
25: import com.breathe.presentation.ui.common.MiniStat
26: import com.breathe.presentation.ui.common.SectionTitle
27: import dagger.hilt.android.lifecycle.HiltViewModel
28: import javax.inject.Inject
29: import kotlinx.coroutines.flow.MutableStateFlow
30: import kotlinx.coroutines.flow.SharingStarted
31: import kotlinx.coroutines.flow.StateFlow
32: import kotlinx.coroutines.flow.combine
33: import kotlinx.coroutines.flow.stateIn
34: import kotlinx.coroutines.flow.update
35: 
36: data class LogUiState(
37:   val entries: List<ConflictLogEntry> = emptyList(),
38:   val selectedEntry: ConflictLogEntry? = null,
39:   val isShared: Boolean = false
40: )
41: 
42: sealed interface LogUiEvent {
43:   data class SelectEntry(val sessionId: Long) : LogUiEvent
44: }
45: 
46: @HiltViewModel
47: class LogViewModel @Inject constructor(
48:   sessionRepository: SessionRepository
49: ) : ViewModel() {
50:   private val selectedSessionId = MutableStateFlow<Long?>(null)
51: 
52:   val uiState: StateFlow<LogUiState> = combine(
53:     sessionRepository.observeConflictLogs(),
54:     selectedSessionId
55:   ) { entries, selectedId ->
56:     val selectedEntry = entries.firstOrNull { it.sessionId == selectedId } ?: entries.firstOrNull()
57:     LogUiState(
58:       entries = entries,
59:       selectedEntry = selectedEntry,
60:       isShared = selectedEntry?.isShared == true
61:     )
62:   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogUiState())
63: 
64:   fun onEvent(event: LogUiEvent) {
65:     when (event) {
66:       is LogUiEvent.SelectEntry -> selectedSessionId.update { event.sessionId }
67:     }
68:   }
69: }
70: 
71: @Composable
72: fun ConflictLogScreen(viewModel: LogViewModel = hiltViewModel()) {
73:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
74: 
75:   AppScreen(
76:     title = "Conflict log",
77:     subtitle = "A private record of repair attempts and the shape each difficult moment took."
78:   ) {
79:     BreatheCard {
80:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
81:         SectionTitle("Overview")
82:         MiniStat("Entries", uiState.entries.size.toString())
83:         MiniStat("Selected", uiState.selectedEntry?.feature?.name ?: "None yet")
84:         MiniStat("Shared", if (uiState.isShared) "Yes" else "No")
85:       }
86:     }
87: 
88:     BreatheCard {
89:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
90:         SectionTitle("Selected entry")
91:         val entry = uiState.selectedEntry
92:         if (entry == null) {
93:           Text("No sessions have been logged yet.")
94:         } else {
95:           MiniStat("Feature", entry.feature.name)
96:           MiniStat("Started", entry.startedAt)
97:           MiniStat("Duration", entry.durationSeconds?.let { "$it sec" } ?: "Active or unavailable")
98:           MiniStat("Mood shift", buildMoodShift(entry))
99:           Text(entry.privateNote ?: "No private note saved for this session.")
100:         }
101:       }
102:     }
103: 
104:     BreatheCard {
105:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
106:         SectionTitle("Entries")
107:         if (uiState.entries.isEmpty()) {
108:           Text("Your local log is still empty. Calm and Timeout sessions will appear here.")
109:         } else {
110:           LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
111:             items(uiState.entries, key = { it.sessionId }) { entry ->
112:               Button(
113:                 onClick = { viewModel.onEvent(LogUiEvent.SelectEntry(entry.sessionId)) },
114:                 modifier = Modifier.fillMaxWidth(),
115:                 colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
116:               ) {
117:                 Text("${entry.feature.name} · ${entry.startedAt}")
118:               }
119:             }
120:           }
121:         }
122:       }
123:     }
124:   }
125: }
126: 
127: private fun buildMoodShift(entry: ConflictLogEntry): String {
128:   val before = entry.moodBefore
129:   val after = entry.moodAfter
130:   return if (before != null && after != null) {
131:     val delta = after - before
132:     if (delta >= 0) "+$delta" else delta.toString()
133:   } else {
134:     "n/a"
135:   }
136: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/insights/InsightsScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.insights
2: 
3: import androidx.compose.foundation.layout.Arrangement
4: import androidx.compose.foundation.layout.Column
5: import androidx.compose.material3.Button
6: import androidx.compose.material3.ButtonDefaults
7: import androidx.compose.material3.Text
8: import androidx.compose.runtime.Composable
9: import androidx.compose.runtime.getValue
10: import androidx.compose.ui.Modifier
11: import androidx.compose.ui.unit.dp
12: import androidx.hilt.navigation.compose.hiltViewModel
13: import androidx.lifecycle.ViewModel
14: import androidx.lifecycle.compose.collectAsStateWithLifecycle
15: import androidx.lifecycle.viewModelScope
16: import com.breathe.domain.model.MoodTrend
17: import com.breathe.domain.model.WeeklySummary
18: import com.breathe.domain.usecase.GetInsightsUseCase
19: import com.breathe.presentation.theme.BreatheAccentStrong
20: import com.breathe.presentation.theme.BreatheCanvas
21: import com.breathe.presentation.ui.common.AppScreen
22: import com.breathe.presentation.ui.common.BreatheCard
23: import com.breathe.presentation.ui.common.MiniStat
24: import com.breathe.presentation.ui.common.SectionTitle
25: import dagger.hilt.android.lifecycle.HiltViewModel
26: import kotlinx.coroutines.flow.SharingStarted
27: import kotlinx.coroutines.flow.StateFlow
28: import kotlinx.coroutines.flow.map
29: import kotlinx.coroutines.flow.stateIn
30: import kotlinx.coroutines.launch
31: import javax.inject.Inject
32: 
33: data class InsightsUiState(
34:   val weeklySummary: WeeklySummary = WeeklySummary(),
35:   val moodTrend: MoodTrend = MoodTrend(),
36:   val topFeature: String? = null,
37:   val headline: String = "",
38:   val recommendations: List<String> = emptyList()
39: )
40: 
41: sealed interface InsightsUiEvent {
42:   data object Refresh : InsightsUiEvent
43: }
44: 
45: @HiltViewModel
46: class InsightsViewModel @Inject constructor(
47:   private val getInsightsUseCase: GetInsightsUseCase
48: ) : ViewModel() {
49:   val uiState: StateFlow<InsightsUiState> = getInsightsUseCase()
50:     .map {
51:       InsightsUiState(
52:         weeklySummary = it.weeklySummary,
53:         moodTrend = it.moodTrend,
54:         topFeature = it.topFeature,
55:         headline = it.headline,
56:         recommendations = it.recommendations
57:       )
58:     }
59:     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())
60: 
61:   init {
62:     onEvent(InsightsUiEvent.Refresh)
63:   }
64: 
65:   fun onEvent(event: InsightsUiEvent) {
66:     when (event) {
67:       InsightsUiEvent.Refresh -> viewModelScope.launch { getInsightsUseCase.refresh() }
68:     }
69:   }
70: }
71: 
72: @Composable
73: fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
74:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
75: 
76:   AppScreen(
77:     title = "Weekly insights",
78:     subtitle = "A softer review of patterns, not a scoreboard."
79:   ) {
80:     BreatheCard {
81:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
82:         SectionTitle("Snapshot")
83:         Text(uiState.headline.ifBlank { "No insight yet." })
84:         MiniStat("Top feature", uiState.topFeature ?: "none")
85:         MiniStat("Average mood", uiState.weeklySummary.averageMood?.toString() ?: "n/a")
86:         MiniStat("Mood direction", uiState.moodTrend.direction)
87:       }
88:     }
89: 
90:     BreatheCard {
91:       Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
92:         SectionTitle("Recommendations")
93:         if (uiState.recommendations.isEmpty()) {
94:           Text("No recommendations yet.")
95:         } else {
96:           uiState.recommendations.forEach { Text("• $it") }
97:         }
98:         Button(
99:           onClick = { viewModel.onEvent(InsightsUiEvent.Refresh) },
100:           colors = ButtonDefaults.buttonColors(containerColor = BreatheAccentStrong, contentColor = BreatheCanvas)
101:         ) { Text("Refresh") }
102:       }
103:     }
104:   }
105: }
```

## File: `styles.css`

```css
1: 
2: :root {
3:   --breathe-bg-soft: rgba(245, 251, 247, 0.97);
4:   --breathe-border: rgba(92, 125, 112, 0.35);
5:   --breathe-shadow: 0 16px 36px rgba(34, 53, 47, 0.18);
6:   --breathe-text: #1f302a;
7:   --breathe-accent: #4f8b78;
8:   --breathe-accent-strong: #2f6f5b;
9:   --breathe-overlay: rgba(235, 246, 240, 0.96);
10:   --breathe-danger: #b84e4e;
11:   --breathe-yellow: #c9962f;
12:   --breathe-green: #3a8d61;
13:   --breathe-red: #a84040;
14: }
15: 
16: #breathe-widget-root {
17:   position: fixed;
18:   right: 16px;
19:   bottom: 16px;
20:   z-index: 2147483000;
21:   font-family: "Trebuchet MS", "Gill Sans", sans-serif;
22:   color: var(--breathe-text);
23: }
24: 
25: .breathe-widget {
26:   display: flex;
27:   flex-direction: column;
28:   align-items: flex-end;
29:   gap: 10px;
30: }
31: 
32: .breathe-widget-toggle {
33:   border: 1px solid var(--breathe-border);
34:   border-radius: 999px;
35:   background: linear-gradient(145deg, rgba(251, 255, 253, 0.96), rgba(230, 242, 236, 0.94));
36:   box-shadow: var(--breathe-shadow);
37:   color: var(--breathe-text);
38:   cursor: grab;
39:   display: inline-flex;
40:   align-items: center;
41:   gap: 8px;
42:   min-height: 40px;
43:   padding: 8px 12px;
44: }
45: 
46: .breathe-widget-toggle:active {
47:   cursor: grabbing;
48: }
49: 
50: .breathe-widget-icon {
51:   width: 20px;
52:   height: 20px;
53:   display: block;
54:   color: var(--breathe-accent-strong);
55: }
56: 
57: .breathe-widget-title {
58:   font-weight: 600;
59:   font-size: 14px;
60: }
61: 
62: .breathe-status-dot {
63:   width: 9px;
64:   height: 9px;
65:   border-radius: 50%;
66:   background: transparent;
67:   border: 1px solid transparent;
68:   display: inline-block;
69:   opacity: 0;
70: }
71: 
72: .breathe-status-dot.is-visible {
73:   opacity: 1;
74:   border-color: rgba(31, 48, 42, 0.16);
75: }
76: 
77: .breathe-status-dot.is-partner {
78:   box-shadow: 0 0 0 2px rgba(31, 48, 42, 0.18);
79: }
80: 
81: .breathe-status-dot.is-green {
82:   background: var(--breathe-green);
83: }
84: 
85: .breathe-status-dot.is-yellow {
86:   background: var(--breathe-yellow);
87: }
88: 
89: .breathe-status-dot.is-red {
90:   background: var(--breathe-red);
91: }
92: 
93: .breathe-widget-panel {
94:   width: min(320px, calc(100vw - 24px));
95:   border: 1px solid var(--breathe-border);
96:   border-radius: 16px;
97:   background: var(--breathe-bg-soft);
98:   box-shadow: var(--breathe-shadow);
99:   padding: 10px;
100: }
101: 
102: .breathe-widget.is-collapsed .breathe-widget-panel {
103:   display: none;
104: }
105: 
106: .breathe-buttons-row {
107:   display: grid;
108:   grid-template-columns: 1fr 1fr;
109:   gap: 8px;
110: }
111: 
112: .breathe-partner-row {
113:   display: flex;
114:   align-items: center;
115:   gap: 8px;
116:   padding: 7px 10px;
117:   border-radius: 8px;
118:   background: rgba(79, 139, 120, 0.07);
119:   border: 1px solid rgba(79, 139, 120, 0.15);
120:   margin-bottom: 8px;
121:   font-size: 12px;
122:   color: var(--breathe-text);
123: }
124: 
125: .breathe-partner-row.is-hidden {
126:   display: none;
127: }
128: 
129: .breathe-partner-dot {
130:   width: 9px;
131:   height: 9px;
132:   border-radius: 50%;
133:   flex-shrink: 0;
134:   background: transparent;
135: }
136: 
137: .breathe-partner-dot.is-green {
138:   background: var(--breathe-green);
139: }
140: 
141: .breathe-partner-dot.is-yellow {
142:   background: var(--breathe-yellow);
143: }
144: 
145: .breathe-partner-dot.is-red {
146:   background: var(--breathe-red);
147: }
148: 
149: .breathe-partner-label {
150:   font-size: 12px;
151:   font-weight: 500;
152: }
153: 
154: .breathe-partner-row:has(.breathe-partner-dot.is-red) {
155:   background: rgba(168, 64, 64, 0.07);
156:   border-color: rgba(168, 64, 64, 0.18);
157: }
158: 
159: .breathe-action-button {
160:   border: 1px solid rgba(93, 123, 112, 0.3);
161:   border-radius: 11px;
162:   padding: 9px 10px;
163:   background: #f7fcf9;
164:   color: var(--breathe-text);
165:   font-size: 13px;
166:   font-weight: 600;
167:   cursor: pointer;
168:   display: inline-flex;
169:   align-items: center;
170:   justify-content: center;
171:   gap: 6px;
172: }
173: 
174: .breathe-action-icon {
175:   width: 14px;
176:   height: 14px;
177:   display: block;
178:   flex: 0 0 auto;
179: }
180: 
181: .breathe-action-label {
182:   display: inline-block;
183: }
184: 
185: .breathe-action-button:hover {
186:   background: #edf7f2;
187: }
188: 
189: .breathe-action-button.is-timeout {
190:   color: #7a413f;
191: }
192: 
193: .breathe-subpanel {
194:   margin-top: 10px;
195:   border-top: 1px solid rgba(84, 112, 101, 0.19);
196:   padding-top: 10px;
197: }
198: 
199: .breathe-subpanel.is-hidden {
200:   display: none;
201: }
202: 
203: .breathe-subpanel-title {
204:   margin: 0 0 8px;
205:   font-size: 13px;
206:   font-weight: 600;
207: }
208: 
209: .breathe-status-choices {
210:   display: flex;
211:   gap: 8px;
212: }
213: 
214: .breathe-status-choice {
215:   flex: 1;
216:   border-radius: 999px;
217:   border: 1px solid transparent;
218:   padding: 7px 8px;
219:   font-size: 12px;
220:   font-weight: 600;
221:   cursor: pointer;
222: }
223: 
224: .breathe-status-green {
225:   background: rgba(61, 145, 99, 0.15);
226:   color: #245739;
227: }
228: 
229: .breathe-status-yellow {
230:   background: rgba(201, 150, 47, 0.2);
231:   color: #6b4f18;
232: }
233: 
234: .breathe-status-red {
235:   background: rgba(168, 64, 64, 0.15);
236:   color: #6f2d2d;
237: }
238: 
239: .breathe-status-choice.is-selected {
240:   border-color: rgba(30, 50, 43, 0.45);
241:   box-shadow: inset 0 0 0 1px rgba(30, 50, 43, 0.18);
242: }
243: 
244: .breathe-status-nudge {
245:   margin: 8px 0 0;
246:   font-size: 12px;
247:   line-height: 1.35;
248:   color: #6f3f3f;
249:   display: none;
250: }
251: 
252: .breathe-status-nudge.is-visible {
253:   display: block;
254: }
255: 
256: .breathe-reentry-row {
257:   margin: 0 0 7px;
258:   font-size: 12px;
259:   display: flex;
260:   align-items: center;
261:   gap: 6px;
262: }
263: 
264: .breathe-reentry-input {
265:   flex: 1;
266:   border: 1px solid rgba(83, 112, 101, 0.31);
267:   border-radius: 8px;
268:   background: #fcfffd;
269:   color: #253933;
270:   padding: 6px 8px;
271:   font-size: 12px;
272: }
273: 
274: .breathe-reentry-footer {
275:   display: flex;
276:   flex-direction: column;
277:   gap: 6px;
278: }
279: 
280: .breathe-primary-button {
281:   border: 1px solid rgba(61, 110, 91, 0.3);
282:   border-radius: 10px;
283:   background: linear-gradient(160deg, #f6fff9, #e6f2ec);
284:   color: #1d3029;
285:   padding: 9px 10px;
286:   font-size: 13px;
287:   font-weight: 600;
288:   cursor: pointer;
289: }
290: 
291: .breathe-overlay-controls {
292:   display: flex;
293:   justify-content: center;
294:   gap: 10px;
295:   margin-top: 14px;
296: }
297: 
298: .breathe-ghost-button {
299:   border: none;
300:   background: transparent;
301:   color: #4f8b78;
302:   font-size: 12px;
303:   font-weight: 600;
304:   cursor: pointer;
305:   padding: 6px 12px;
306:   border-radius: 8px;
307: }
308: 
309: .breathe-ghost-button:hover {
310:   background: rgba(79, 139, 120, 0.08);
311: }
312: 
313: .breathe-ghost-button.is-danger {
314:   color: #a84040;
315: }
316: 
317: .breathe-ghost-button.is-danger:hover {
318:   background: rgba(168, 64, 64, 0.07);
319: }
320: 
321: .breathe-reentry-feedback {
322:   margin: 0;
323:   font-size: 12px;
324:   color: #2d5a47;
325: }
326: 
327: .breathe-reentry-feedback.is-error {
328:   color: #8d3737;
329: }
330: 
331: .breathe-overlay {
332:   position: fixed;
333:   inset: 0;
334:   z-index: 2147483640;
335:   background: var(--breathe-overlay);
336:   display: flex;
337:   align-items: center;
338:   justify-content: center;
339:   padding: 24px;
340: }
341: 
342: .breathe-overlay-shell {
343:   width: min(420px, 100%);
344:   border: 1px solid var(--breathe-border);
345:   border-radius: 20px;
346:   padding: 24px;
347:   background: rgba(251, 255, 253, 0.97);
348:   box-shadow: 0 22px 42px rgba(23, 40, 33, 0.16);
349:   text-align: center;
350: }
351: 
352: .breathe-overlay-title {
353:   margin: 0 0 10px;
354:   font-size: 26px;
355:   line-height: 1.2;
356:   font-weight: 600;
357: }
358: 
359: .breathe-overlay-body {
360:   margin: 0;
361:   font-size: 15px;
362:   line-height: 1.45;
363:   color: #334b42;
364: }
365: 
366: .breathe-overlay-timer {
367:   margin-top: 18px;
368:   font-size: clamp(30px, 8vw, 48px);
369:   letter-spacing: 0.05em;
370:   font-weight: 700;
371:   color: #263f36;
372: }
373: 
374: .breathe-orb {
375:   width: 122px;
376:   height: 122px;
377:   margin: 20px auto 0;
378:   border-radius: 50%;
379:   background: radial-gradient(circle at 30% 30%, rgba(173, 222, 198, 0.88), rgba(86, 145, 120, 0.8));
380:   box-shadow: 0 0 0 14px rgba(139, 202, 173, 0.17);
381:   will-change: transform;
382:   isolation: isolate;
383: }
384: 
385: .breathe-timeout-overlay {
386:   background: rgba(238, 245, 241, 0.92);
387: }
388: 
389: .breathe-timeout-shell {
390:   background: rgba(255, 255, 255, 0.96);
391: }
392: 
393: .breathe-timeout-blurred {
394:   filter: blur(8px) saturate(0.85);
395:   transition: filter 0.24s ease;
396: }
397: 
398: .breathe-timer-pill {
399:   display: inline-flex;
400:   align-items: center;
401:   gap: 8px;
402:   padding: 6px 12px 6px 8px;
403:   border-radius: 999px;
404:   border: 1px solid var(--breathe-border);
405:   background: var(--breathe-bg-soft);
406:   font-size: 12px;
407:   font-weight: 600;
408:   color: #263f36;
409:   cursor: pointer;
410:   box-shadow: var(--breathe-shadow);
411:   position: fixed;
412:   z-index: 2147483001;
413:   bottom: 64px;
414:   right: 16px;
415:   transition: opacity 0.2s;
416: }
417: 
418: .breathe-timer-pill:hover {
419:   opacity: 0.88;
420: }
421: 
422: .breathe-timer-pill-orb {
423:   width: 22px;
424:   height: 22px;
425:   border-radius: 50%;
426:   background: radial-gradient(circle at 35% 35%, rgba(173, 222, 198, 0.9), rgba(86, 145, 120, 0.85));
427:   flex-shrink: 0;
428:   will-change: transform;
429:   isolation: isolate;
430: }
431: 
432: .breathe-timer-pill-orb.is-timeout {
433:   background: radial-gradient(circle at 35% 35%, rgba(250, 199, 117, 0.9), rgba(186, 117, 23, 0.75));
434: }
435: 
436: .breathe-timer-pill-expand {
437:   width: 13px;
438:   height: 13px;
439:   opacity: 0.55;
440:   flex-shrink: 0;
441: }
442: 
443: .breathe-calm-confirm {
444:   display: flex;
445:   align-items: center;
446:   gap: 12px;
447:   padding: 10px 14px;
448:   background: rgba(61, 145, 99, 0.1);
449:   border: 1px solid rgba(61, 145, 99, 0.3);
450:   border-radius: 10px;
451:   margin-top: 12px;
452:   cursor: pointer;
453:   transition: background 0.15s;
454: }
455: 
456: .breathe-calm-confirm:hover {
457:   background: rgba(61, 145, 99, 0.16);
458: }
459: 
460: .breathe-calm-confirm-icon {
461:   width: 22px;
462:   height: 22px;
463:   color: #3a8d61;
464:   flex-shrink: 0;
465: }
466: 
467: .breathe-calm-confirm-title {
468:   margin: 0;
469:   font-size: 13px;
470:   font-weight: 600;
471:   color: #245739;
472: }
473: 
474: .breathe-calm-confirm-sub {
475:   margin: 2px 0 0;
476:   font-size: 11px;
477:   color: #4a7a5e;
478: }
479: 
480: .breathe-sos-panel {
481:   background: rgba(252, 235, 235, 0.7);
482:   border: 1px solid rgba(168, 64, 64, 0.18);
483:   border-radius: 12px;
484:   padding: 12px;
485:   margin-top: 12px;
486:   text-align: left;
487: }
488: 
489: .breathe-sos-title {
490:   font-size: 12px;
491:   font-weight: 600;
492:   color: #6f2d2d;
493:   margin: 0 0 10px;
494: }
495: 
496: .breathe-sos-item {
497:   display: flex;
498:   align-items: flex-start;
499:   gap: 10px;
500:   padding: 8px 10px;
501:   background: rgba(255, 255, 255, 0.7);
502:   border: 1px solid rgba(168, 64, 64, 0.1);
503:   border-radius: 8px;
504:   margin-bottom: 6px;
505: }
506: 
507: .breathe-sos-item:last-child {
508:   margin-bottom: 0;
509: }
510: 
511: .breathe-sos-item.is-actionable {
512:   cursor: pointer;
513:   transition: background 0.15s;
514: }
515: 
516: .breathe-sos-item.is-actionable:hover {
517:   background: rgba(255, 255, 255, 0.95);
518: }
519: 
520: .breathe-sos-icon {
521:   font-size: 16px;
522:   width: 28px;
523:   height: 28px;
524:   border-radius: 7px;
525:   background: rgba(255, 255, 255, 0.8);
526:   display: flex;
527:   align-items: center;
528:   justify-content: center;
529:   flex-shrink: 0;
530: }
531: 
532: .breathe-sos-label {
533:   margin: 0;
534:   font-size: 12px;
535:   font-weight: 600;
536:   color: #3d2020;
537: }
538: 
539: .breathe-sos-sub {
540:   margin: 2px 0 0;
541:   font-size: 11px;
542:   color: #9b5c5c;
543: }
544: 
545: .breathe-private-note {
546:   margin-top: 8px;
547: }
548: 
549: .breathe-note-textarea {
550:   width: 100%;
551:   border: 1px solid rgba(83, 112, 101, 0.31);
552:   border-radius: 8px;
553:   background: #fcfffd;
554:   color: #253933;
555:   padding: 8px 10px;
556:   font-size: 12px;
557:   font-family: inherit;
558:   resize: vertical;
559:   line-height: 1.5;
560:   box-sizing: border-box;
561: }
562: 
563: .breathe-note-textarea:focus {
564:   outline: none;
565:   border-color: #4f8b78;
566: }
567: 
568: @media (max-width: 640px) {
569:   #breathe-widget-root {
570:     left: 10px;
571:     right: 10px;
572:     bottom: 10px;
573:   }
574: 
575:   .breathe-widget {
576:     align-items: stretch;
577:   }
578: 
579:   .breathe-widget-toggle {
580:     width: 100%;
581:     justify-content: center;
582:   }
583: 
584:   .breathe-widget-panel {
585:     width: 100%;
586:   }
587: 
588:   .breathe-buttons-row {
589:     grid-template-columns: 1fr;
590:   }
591: 
592:   .breathe-overlay-shell {
593:     padding: 18px;
594:   }
595: 
596:   .breathe-overlay-title {
597:     font-size: 22px;
598:   }
599: }
600: 
601: .breathe-dark-mode #breathe-widget-root {
602:   color: #d6e5dd;
603: }
604: 
605: .breathe-dark-mode .breathe-widget-toggle {
606:   border-color: rgba(122, 157, 144, 0.45);
607:   background: linear-gradient(145deg, rgba(26, 36, 33, 0.95), rgba(18, 28, 24, 0.94));
608:   box-shadow: 0 16px 36px rgba(2, 10, 8, 0.48);
609:   color: #d6e5dd;
610: }
611: 
612: .breathe-dark-mode .breathe-widget-icon {
613:   color: #88c2ad;
614: }
615: 
616: .breathe-dark-mode .breathe-status-dot.is-visible {
617:   border-color: rgba(214, 229, 221, 0.25);
618: }
619: 
620: .breathe-dark-mode .breathe-status-dot.is-partner {
621:   box-shadow: 0 0 0 2px rgba(214, 229, 221, 0.2);
622: }
623: 
624: .breathe-dark-mode .breathe-widget-panel {
625:   border-color: rgba(122, 157, 144, 0.42);
626:   background: rgba(18, 29, 25, 0.96);
627:   box-shadow: 0 18px 38px rgba(2, 10, 8, 0.5);
628: }
629: 
630: .breathe-dark-mode .breathe-partner-row {
631:   background: rgba(79, 139, 120, 0.1);
632:   border-color: rgba(122, 157, 144, 0.25);
633: }
634: 
635: .breathe-dark-mode .breathe-partner-label {
636:   color: #d6e5dd;
637: }
638: 
639: .breathe-dark-mode .breathe-partner-row:has(.breathe-partner-dot.is-red) {
640:   background: rgba(168, 64, 64, 0.12);
641:   border-color: rgba(168, 64, 64, 0.25);
642: }
643: 
644: .breathe-dark-mode .breathe-action-button {
645:   border-color: rgba(124, 158, 145, 0.34);
646:   background: #1f2d29;
647:   color: #d6e5dd;
648: }
649: 
650: .breathe-dark-mode .breathe-action-button:hover {
651:   background: #273834;
652: }
653: 
654: .breathe-dark-mode .breathe-action-button.is-timeout {
655:   color: #e4a3a3;
656: }
657: 
658: .breathe-dark-mode .breathe-subpanel {
659:   border-top-color: rgba(122, 157, 144, 0.32);
660: }
661: 
662: .breathe-dark-mode .breathe-subpanel-title {
663:   color: #d2e2da;
664: }
665: 
666: .breathe-dark-mode .breathe-status-choice.breathe-status-green {
667:   background: rgba(77, 161, 110, 0.22);
668:   color: #cde8d8;
669: }
670: 
671: .breathe-dark-mode .breathe-status-choice.breathe-status-yellow {
672:   background: rgba(201, 150, 47, 0.28);
673:   color: #f0dfbc;
674: }
675: 
676: .breathe-dark-mode .breathe-status-choice.breathe-status-red {
677:   background: rgba(168, 64, 64, 0.28);
678:   color: #f0cbcb;
679: }
680: 
681: .breathe-dark-mode .breathe-status-choice.is-selected {
682:   border-color: rgba(220, 233, 225, 0.48);
683:   box-shadow: inset 0 0 0 1px rgba(220, 233, 225, 0.24);
684: }
685: 
686: .breathe-dark-mode .breathe-status-nudge {
687:   color: #f0baba;
688: }
689: 
690: .breathe-dark-mode .breathe-reentry-row {
691:   color: #cfddd6;
692: }
693: 
694: .breathe-dark-mode .breathe-reentry-input {
695:   border-color: rgba(124, 158, 145, 0.45);
696:   background: #13201c;
697:   color: #d6e5dd;
698: }
699: 
700: .breathe-dark-mode .breathe-primary-button {
701:   border-color: rgba(125, 170, 149, 0.38);
702:   background: linear-gradient(160deg, #20312c, #182621);
703:   color: #e0ede7;
704: }
705: 
706: .breathe-dark-mode .breathe-reentry-feedback {
707:   color: #8cd2b1;
708: }
709: 
710: .breathe-dark-mode .breathe-reentry-feedback.is-error {
711:   color: #f1b9b9;
712: }
713: 
714: .breathe-dark-mode .breathe-ghost-button {
715:   color: #88c2ad;
716: }
717: 
718: .breathe-dark-mode .breathe-ghost-button:hover {
719:   background: rgba(136, 194, 173, 0.1);
720: }
721: 
722: .breathe-dark-mode .breathe-ghost-button.is-danger {
723:   color: #e4a3a3;
724: }
725: 
726: .breathe-dark-mode .breathe-overlay {
727:   background: rgba(8, 16, 13, 0.86);
728: }
729: 
730: .breathe-dark-mode .breathe-overlay-shell,
731: .breathe-dark-mode .breathe-timeout-shell {
732:   border-color: rgba(122, 157, 144, 0.46);
733:   background: rgba(16, 25, 22, 0.96);
734:   box-shadow: 0 24px 44px rgba(2, 10, 8, 0.55);
735: }
736: 
737: .breathe-dark-mode .breathe-overlay-title {
738:   color: #e2eee8;
739: }
740: 
741: .breathe-dark-mode .breathe-overlay-body {
742:   color: #b9cdc4;
743: }
744: 
745: .breathe-dark-mode .breathe-overlay-timer {
746:   color: #e2eee8;
747: }
748: 
749: .breathe-dark-mode .breathe-orb {
750:   background: radial-gradient(circle at 30% 30%, rgba(109, 186, 153, 0.92), rgba(56, 108, 88, 0.86));
751:   box-shadow: 0 0 0 14px rgba(86, 145, 120, 0.2);
752: }
753: 
754: .breathe-dark-mode .breathe-timeout-overlay {
755:   background: rgba(6, 12, 10, 0.82);
756: }
757: 
758: .breathe-dark-mode .breathe-timer-pill {
759:   background: rgba(18, 29, 25, 0.96);
760:   border-color: rgba(122, 157, 144, 0.42);
761:   color: #d6e5dd;
762:   box-shadow: 0 16px 36px rgba(2, 10, 8, 0.48);
763: }
764: 
765: .breathe-dark-mode .breathe-calm-confirm {
766:   background: rgba(77, 161, 110, 0.15);
767:   border-color: rgba(77, 161, 110, 0.3);
768: }
769: 
770: .breathe-dark-mode .breathe-calm-confirm-title {
771:   color: #cde8d8;
772: }
773: 
774: .breathe-dark-mode .breathe-calm-confirm-sub {
775:   color: #8abba0;
776: }
777: 
778: .breathe-dark-mode .breathe-calm-confirm-icon {
779:   color: #5dcaa5;
780: }
781: 
782: .breathe-dark-mode .breathe-sos-panel {
783:   background: rgba(60, 20, 20, 0.4);
784:   border-color: rgba(168, 64, 64, 0.3);
785: }
786: 
787: .breathe-dark-mode .breathe-sos-title {
788:   color: #f0baba;
789: }
790: 
791: .breathe-dark-mode .breathe-sos-item {
792:   background: rgba(255, 255, 255, 0.05);
793:   border-color: rgba(168, 64, 64, 0.15);
794: }
795: 
796: .breathe-dark-mode .breathe-sos-label {
797:   color: #f0cbcb;
798: }
799: 
800: .breathe-dark-mode .breathe-sos-sub {
801:   color: #c08080;
802: }
803: 
804: .breathe-dark-mode .breathe-sos-icon {
805:   background: rgba(255, 255, 255, 0.07);
806: }
807: 
808: .breathe-dark-mode .breathe-note-textarea {
809:   background: #13201c;
810:   color: #d6e5dd;
811:   border-color: rgba(124, 158, 145, 0.45);
812: }
813: 
814: /* Partner notification styling */
815: .breathe-partner-notification {
816:   position: fixed;
817:   top: 20px;
818:   left: 50%;
819:   transform: translateX(-50%);
820:   background-color: rgba(0, 0, 0, 0.8);
821:   color: white;
822:   padding: 12px 24px;
823:   border-radius: 4px;
824:   font-size: 14px;
825:   z-index: 1000000;
826:   pointer-events: none;
827:   animation: fadeInOut 0.3s ease-in-out;
828: }
829: 
830: @keyframes fadeInOut {
831:   0% { opacity: 0; transform: translateX(-50%) translateY(-10px); }
832:   10% { opacity: 1; transform: translateX(-50%) translateY(0); }
833:   90% { opacity: 1; transform: translateX(-50%) translateY(0); }
834:   100% { opacity: 0; transform: translateX(-50%) translateY(10px); }
835: }
836: 
837: .breathe-dark-mode .breathe-partner-notification {
838:   background-color: rgba(0, 0, 0, 0.9);
839: }
```
