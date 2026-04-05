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
9: val breatheServerUrl = System.getenv("BREATHE_SERVER_URL") ?: "http://10.0.2.2:3000/"
10: val breatheVersionName = "0.3.2"
11: 
12: fun semverVersionCode(version: String): Int {
13:   val (major, minor, patch) = version.split('.').map(String::toInt)
14:   return (major * 10_000) + (minor * 100) + patch
15: }
16: 
17: android {
18:   namespace = "com.breathe"
19:   compileSdk = 35
20: 
21:   defaultConfig {
22:     applicationId = "com.breathe"
23:     minSdk = 29
24:     targetSdk = 35
25:     versionCode = semverVersionCode(breatheVersionName)
26:     versionName = breatheVersionName
27:     testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
28: 
29:     buildConfigField("String", "DEFAULT_SERVER_URL", "\"$breatheServerUrl\"")
30:   }
31: 
32:   buildTypes {
33:     release {
34:       isMinifyEnabled = false
35:       proguardFiles(
36:         getDefaultProguardFile("proguard-android-optimize.txt"),
37:         "proguard-rules.pro"
38:       )
39:     }
40:   }
41: 
42:   compileOptions {
43:     sourceCompatibility = JavaVersion.VERSION_17
44:     targetCompatibility = JavaVersion.VERSION_17
45:   }
46: 
47:   kotlinOptions {
48:     jvmTarget = "17"
49:   }
50: 
51:   buildFeatures {
52:     compose = true
53:     buildConfig = true
54:   }
55: 
56:   packaging {
57:     resources {
58:       excludes += "/META-INF/{AL2.0,LGPL2.1}"
59:     }
60:   }
61: }
62: 
63: dependencies {
64:   implementation(platform("androidx.compose:compose-bom:2024.12.01"))
65:   androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
66: 
67:   implementation("androidx.compose.material3:material3")
68:   implementation("androidx.compose.material:material-icons-extended")
69:   implementation("androidx.compose.ui:ui")
70:   implementation("androidx.compose.ui:ui-tooling-preview")
71:   implementation("androidx.activity:activity-compose:1.9.3")
72:   implementation("androidx.navigation:navigation-compose:2.8.5")
73:   implementation("com.google.android.material:material:1.12.0")
74: 
75:   implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
76:   implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
77:   implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
78: 
79:   implementation("com.google.dagger:hilt-android:2.51")
80:   ksp("com.google.dagger:hilt-android-compiler:2.51")
81:   implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
82:   implementation("androidx.hilt:hilt-work:1.2.0")
83: 
84:   implementation("com.squareup.retrofit2:retrofit:2.11.0")
85:   implementation("com.squareup.retrofit2:converter-gson:2.11.0")
86:   implementation("com.squareup.okhttp3:okhttp:4.12.0")
87:   implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
88:   implementation("com.google.code.gson:gson:2.11.0")
89: 
90:   implementation("androidx.room:room-runtime:2.6.1")
91:   implementation("androidx.room:room-ktx:2.6.1")
92:   ksp("androidx.room:room-compiler:2.6.1")
93: 
94:   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
95: 
96:   implementation("androidx.security:security-crypto:1.1.0-alpha06")
97: 
98:   implementation("androidx.work:work-runtime-ktx:2.9.1")
99:   implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
100: 
101:   debugImplementation("androidx.compose.ui:ui-tooling")
102:   debugImplementation("androidx.compose.ui:ui-test-manifest")
103: }
```

## File: `android/app/src/main/java/com/breathe/MainActivity.kt`

```kotlin
1: package com.breathe
2: 
3: import android.os.Bundle
4: import androidx.activity.ComponentActivity
5: import androidx.activity.compose.setContent
6: import androidx.compose.material3.Surface
7: import com.breathe.presentation.navigation.BreatheNavGraph
8: import com.breathe.presentation.theme.BreatheTheme
9: import dagger.hilt.android.AndroidEntryPoint
10: 
11: @AndroidEntryPoint
12: class MainActivity : ComponentActivity() {
13:   override fun onCreate(savedInstanceState: Bundle?) {
14:     super.onCreate(savedInstanceState)
15:     setContent {
16:       BreatheTheme {
17:         Surface {
18:           BreatheNavGraph()
19:         }
20:       }
21:     }
22:   }
23: }
```

## File: `android/app/src/main/java/com/breathe/presentation/theme/Color.kt`

```kotlin
1: package com.breathe.presentation.theme
2: 
3: import androidx.compose.ui.graphics.Color
4: 
5: val BreatheCanvas = Color(0xFFFBFBE2)
6: val BreatheCanvasSoft = Color(0xFFF5F5DC)
7: val BreatheOverlay = Color(0xFFEFEFD7)
8: val BreatheInk = Color(0xFF1B1D0E)
9: val BreatheMutedInk = Color(0xFF46483C)
10: val BreatheAccent = Color(0xFF879A6B)
11: val BreatheAccentStrong = Color(0xFF53643A)
12: val BreatheGreen = Color(0xFF3A8D61)
13: val BreatheYellow = Color(0xFFC9962F)
14: val BreatheRed = Color(0xFF8F4B3F)
15: val BreatheBorder = Color(0xFFB9BAAB)
16: val BreatheCardSurface = Color(0xFFFCFBEA)
```

## File: `android/app/src/main/java/com/breathe/presentation/theme/Type.kt`

```kotlin
1: package com.breathe.presentation.theme
2: 
3: import androidx.compose.material3.Typography
4: import androidx.compose.ui.text.TextStyle
5: import androidx.compose.ui.text.font.FontFamily
6: import androidx.compose.ui.text.font.FontWeight
7: import androidx.compose.ui.unit.sp
8: 
9: val BreatheTypography = Typography(
10:   headlineLarge = TextStyle(
11:     fontFamily = FontFamily.Serif,
12:     fontWeight = FontWeight.SemiBold,
13:     fontSize = 34.sp,
14:     lineHeight = 40.sp,
15:     letterSpacing = (-0.4).sp
16:   ),
17:   headlineMedium = TextStyle(
18:     fontFamily = FontFamily.Serif,
19:     fontWeight = FontWeight.SemiBold,
20:     fontSize = 28.sp,
21:     lineHeight = 34.sp
22:   ),
23:   titleMedium = TextStyle(
24:     fontFamily = FontFamily.SansSerif,
25:     fontWeight = FontWeight.SemiBold,
26:     fontSize = 18.sp,
27:     lineHeight = 24.sp
28:   ),
29:   bodyLarge = TextStyle(
30:     fontFamily = FontFamily.SansSerif,
31:     fontWeight = FontWeight.Normal,
32:     fontSize = 16.sp,
33:     lineHeight = 24.sp
34:   ),
35:   bodyMedium = TextStyle(
36:     fontFamily = FontFamily.SansSerif,
37:     fontWeight = FontWeight.Normal,
38:     fontSize = 15.sp,
39:     lineHeight = 22.sp
40:   ),
41:   bodySmall = TextStyle(
42:     fontFamily = FontFamily.SansSerif,
43:     fontWeight = FontWeight.Normal,
44:     fontSize = 13.sp,
45:     lineHeight = 18.sp
46:   ),
47:   labelLarge = TextStyle(
48:     fontFamily = FontFamily.SansSerif,
49:     fontWeight = FontWeight.SemiBold,
50:     fontSize = 14.sp,
51:     lineHeight = 20.sp
52:   ),
53:   labelMedium = TextStyle(
54:     fontFamily = FontFamily.SansSerif,
55:     fontWeight = FontWeight.Medium,
56:     fontSize = 12.sp,
57:     lineHeight = 16.sp,
58:     letterSpacing = 0.3.sp
59:   )
60: )
```

## File: `android/app/src/main/java/com/breathe/presentation/theme/BreatheTheme.kt`

```kotlin
1: package com.breathe.presentation.theme
2: 
3: import androidx.compose.material3.MaterialTheme
4: import androidx.compose.material3.Shapes
5: import androidx.compose.material3.lightColorScheme
6: import androidx.compose.runtime.Composable
7: import androidx.compose.foundation.shape.RoundedCornerShape
8: import androidx.compose.ui.unit.dp
9: 
10: private val LightColors = lightColorScheme(
11:   primary = BreatheAccent,
12:   secondary = BreatheGreen,
13:   tertiary = BreatheYellow,
14:   background = BreatheCanvas,
15:   surface = BreatheCardSurface,
16:   surfaceVariant = BreatheOverlay,
17:   onPrimary = BreatheCanvas,
18:   onSurface = BreatheInk,
19:   onSurfaceVariant = BreatheMutedInk,
20:   outline = BreatheBorder
21: )
22: 
23: private val BreatheShapes = Shapes(
24:   small = RoundedCornerShape(18.dp),
25:   medium = RoundedCornerShape(26.dp),
26:   large = RoundedCornerShape(34.dp)
27: )
28: 
29: @Composable
30: fun BreatheTheme(content: @Composable () -> Unit) {
31:   MaterialTheme(
32:     colorScheme = LightColors,
33:     typography = BreatheTypography,
34:     shapes = BreatheShapes,
35:     content = content
36:   )
37: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/common/AppChrome.kt`

```kotlin
1: package com.breathe.presentation.ui.common
2: 
3: import com.breathe.BuildConfig
4: import androidx.compose.foundation.background
5: import androidx.compose.foundation.border
6: import androidx.compose.foundation.clickable
7: import androidx.compose.foundation.layout.Arrangement
8: import androidx.compose.foundation.layout.Box
9: import androidx.compose.foundation.layout.BoxWithConstraints
10: import androidx.compose.foundation.layout.Column
11: import androidx.compose.foundation.layout.ColumnScope
12: import androidx.compose.foundation.layout.PaddingValues
13: import androidx.compose.foundation.layout.Row
14: import androidx.compose.foundation.layout.Spacer
15: import androidx.compose.foundation.layout.fillMaxSize
16: import androidx.compose.foundation.layout.fillMaxWidth
17: import androidx.compose.foundation.layout.height
18: import androidx.compose.foundation.layout.heightIn
19: import androidx.compose.foundation.layout.padding
20: import androidx.compose.foundation.layout.size
21: import androidx.compose.foundation.layout.width
22: import androidx.compose.foundation.rememberScrollState
23: import androidx.compose.foundation.shape.CircleShape
24: import androidx.compose.foundation.shape.RoundedCornerShape
25: import androidx.compose.foundation.verticalScroll
26: import androidx.compose.material.icons.Icons
27: import androidx.compose.material.icons.rounded.AutoGraph
28: import androidx.compose.material.icons.rounded.EditNote
29: import androidx.compose.material.icons.rounded.Favorite
30: import androidx.compose.material.icons.rounded.Home
31: import androidx.compose.material3.Button
32: import androidx.compose.material3.ButtonDefaults
33: import androidx.compose.material3.Card
34: import androidx.compose.material3.CardDefaults
35: import androidx.compose.material3.Icon
36: import androidx.compose.material3.MaterialTheme
37: import androidx.compose.material3.OutlinedButton
38: import androidx.compose.material3.Scaffold
39: import androidx.compose.material3.Text
40: import androidx.compose.material3.Surface
41: import androidx.compose.runtime.Composable
42: import androidx.compose.ui.Alignment
43: import androidx.compose.ui.Modifier
44: import androidx.compose.ui.graphics.Brush
45: import androidx.compose.ui.graphics.Color
46: import androidx.compose.ui.graphics.vector.ImageVector
47: import androidx.compose.ui.text.font.FontStyle
48: import androidx.compose.ui.text.font.FontWeight
49: import androidx.compose.ui.text.style.TextAlign
50: import androidx.compose.ui.text.style.TextOverflow
51: import androidx.compose.ui.unit.Dp
52: import androidx.compose.ui.unit.dp
53: import com.breathe.domain.model.StatusLevel
54: import com.breathe.presentation.navigation.Screen
55: import com.breathe.presentation.theme.BreatheAccent
56: import com.breathe.presentation.theme.BreatheAccentStrong
57: import com.breathe.presentation.theme.BreatheBorder
58: import com.breathe.presentation.theme.BreatheCanvas
59: import com.breathe.presentation.theme.BreatheCanvasSoft
60: import com.breathe.presentation.theme.BreatheCardSurface
61: import com.breathe.presentation.theme.BreatheGreen
62: import com.breathe.presentation.theme.BreatheInk
63: import com.breathe.presentation.theme.BreatheMutedInk
64: import com.breathe.presentation.theme.BreatheOverlay
65: import com.breathe.presentation.theme.BreatheRed
66: import com.breathe.presentation.theme.BreatheYellow
67: 
68: private data class BottomNavDestination(
69:   val route: String,
70:   val label: String,
71:   val icon: ImageVector
72: )
73: 
74: private val bottomNavDestinations = listOf(
75:   BottomNavDestination(Screen.Home.route, "Home", Icons.Rounded.Home),
76:   BottomNavDestination(Screen.Log.route, "Log", Icons.Rounded.EditNote),
77:   BottomNavDestination(Screen.Insights.route, "Insights", Icons.Rounded.AutoGraph),
78:   BottomNavDestination(Screen.Status.route, "Status", Icons.Rounded.Favorite)
79: )
80: 
81: private val SectionSpacing = 24.dp
82: private val CardSpacing = 24.dp
83: private val HeroPadding = 32.dp
84: 
85: @Composable
86: fun AppScreen(
87:   title: String,
88:   subtitle: String,
89:   modifier: Modifier = Modifier,
90:   showBottomNav: Boolean = false,
91:   selectedBottomRoute: String? = null,
92:   onNavigate: (String) -> Unit = {},
93:   content: @Composable ColumnScope.() -> Unit
94: ) {
95:   Scaffold(
96:     modifier = modifier.fillMaxSize(),
97:     containerColor = BreatheCanvas,
98:     topBar = { BreatheTopBar() },
99:     bottomBar = {
100:       if (showBottomNav) {
101:         BreatheBottomNav(selectedBottomRoute = selectedBottomRoute, onNavigate = onNavigate)
102:       }
103:     }
104:   ) { innerPadding ->
105:     Column(
106:       modifier = Modifier
107:         .fillMaxSize()
108:         .background(
109:           Brush.verticalGradient(
110:             colors = listOf(BreatheCanvas, BreatheCanvasSoft, BreatheOverlay)
111:           )
112:         )
113:         .verticalScroll(rememberScrollState())
114:         .padding(innerPadding)
115:         .padding(horizontal = 20.dp, vertical = 18.dp),
116:       verticalArrangement = Arrangement.spacedBy(SectionSpacing)
117:     ) {
118:       Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
119:         Text(
120:           text = title,
121:           style = MaterialTheme.typography.headlineLarge,
122:           color = BreatheInk
123:         )
124:         Text(
125:           text = subtitle,
126:           style = MaterialTheme.typography.bodyLarge,
127:           color = BreatheMutedInk
128:         )
129:       }
130: 
131:       content()
132: 
133:       Spacer(modifier = Modifier.height(6.dp))
134:     }
135:   }
136: }
137: 
138: @Composable
139: private fun BreatheTopBar() {
140:   Surface(
141:     color = BreatheCanvas.copy(alpha = 0.86f),
142:     tonalElevation = 0.dp,
143:     shadowElevation = 0.dp
144:   ) {
145:     Row(
146:       modifier = Modifier
147:         .fillMaxWidth()
148:         .padding(horizontal = 20.dp, vertical = 14.dp),
149:       verticalAlignment = Alignment.CenterVertically
150:       ) {
151:         Box(
152:         modifier = Modifier.width(76.dp),
153:         contentAlignment = Alignment.CenterStart
154:       ) {
155:         Box(
156:           modifier = Modifier
157:             .size(40.dp)
158:             .background(BreatheOverlay, CircleShape)
159:             .border(1.dp, BreatheBorder.copy(alpha = 0.45f), CircleShape),
160:           contentAlignment = Alignment.Center
161:         ) {
162:           Text(
163:             text = "B",
164:             color = BreatheAccentStrong,
165:             style = MaterialTheme.typography.titleMedium,
166:             fontStyle = FontStyle.Italic,
167:             fontWeight = FontWeight.SemiBold
168:           )
169:         }
170:       }
171: 
172:       Text(
173:         text = "Breathe",
174:         modifier = Modifier.weight(1f),
175:         style = MaterialTheme.typography.headlineMedium,
176:         color = BreatheAccentStrong,
177:         fontStyle = FontStyle.Italic,
178:         textAlign = TextAlign.Center,
179:         maxLines = 1,
180:         overflow = TextOverflow.Ellipsis
181:       )
182: 
183:       Box(
184:         modifier = Modifier.width(76.dp),
185:         contentAlignment = Alignment.CenterEnd
186:       ) {
187:         VersionPill()
188:       }
189:     }
190:   }
191: }
192: 
193: @Composable
194: private fun VersionPill() {
195:   Surface(
196:     color = BreatheOverlay.copy(alpha = 0.92f),
197:     shape = RoundedCornerShape(999.dp)
198:   ) {
199:     Text(
200:       text = "v${BuildConfig.VERSION_NAME}",
201:       modifier = Modifier
202:         .border(1.dp, BreatheBorder.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
203:         .padding(horizontal = 12.dp, vertical = 6.dp),
204:       style = MaterialTheme.typography.labelLarge,
205:       color = BreatheAccentStrong,
206:       maxLines = 1,
207:       overflow = TextOverflow.Ellipsis
208:     )
209:   }
210: }
211: 
212: @Composable
213: private fun BreatheBottomNav(selectedBottomRoute: String?, onNavigate: (String) -> Unit) {
214:   Surface(
215:     color = BreatheCanvas.copy(alpha = 0.88f),
216:     shape = RoundedCornerShape(topStart = 42.dp, topEnd = 42.dp),
217:     shadowElevation = 18.dp
218:   ) {
219:     Row(
220:       modifier = Modifier
221:         .fillMaxWidth()
222:         .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 22.dp),
223:       horizontalArrangement = Arrangement.spacedBy(4.dp)
224:     ) {
225:       bottomNavDestinations.forEach { item ->
226:         val isSelected = item.route == selectedBottomRoute
227:         Column(
228:           modifier = Modifier
229:             .weight(1f)
230:             .background(
231:               if (isSelected) BreatheOverlay else Color.Transparent,
232:               RoundedCornerShape(999.dp)
233:             )
234:             .heightIn(min = 56.dp)
235:             .clickable { onNavigate(item.route) }
236:             .padding(horizontal = 6.dp, vertical = 8.dp),
237:           horizontalAlignment = Alignment.CenterHorizontally,
238:           verticalArrangement = Arrangement.Center
239:         ) {
240:           Icon(
241:             imageVector = item.icon,
242:             contentDescription = item.label,
243:             tint = if (isSelected) BreatheAccentStrong else BreatheAccent.copy(alpha = 0.82f)
244:           )
245:           Spacer(modifier = Modifier.height(4.dp))
246:           Text(
247:             text = item.label,
248:             color = if (isSelected) BreatheAccentStrong else BreatheAccent.copy(alpha = 0.82f),
249:             style = MaterialTheme.typography.labelMedium,
250:             textAlign = TextAlign.Center,
251:             maxLines = 1,
252:             overflow = TextOverflow.Ellipsis
253:           )
254:         }
255:       }
256:     }
257:   }
258: }
259: 
260: @Composable
261: fun AdaptiveTwoPane(
262:   modifier: Modifier = Modifier,
263:   breakpoint: Dp = 340.dp,
264:   spacing: Dp = 16.dp,
265:   first: @Composable (Modifier) -> Unit,
266:   second: @Composable (Modifier) -> Unit
267: ) {
268:   BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
269:     if (maxWidth < breakpoint) {
270:       Column(
271:         modifier = Modifier.fillMaxWidth(),
272:         verticalArrangement = Arrangement.spacedBy(spacing)
273:       ) {
274:         first(Modifier.fillMaxWidth())
275:         second(Modifier.fillMaxWidth())
276:       }
277:     } else {
278:       Row(
279:         modifier = Modifier.fillMaxWidth(),
280:         horizontalArrangement = Arrangement.spacedBy(spacing)
281:       ) {
282:         first(Modifier.weight(1f))
283:         second(Modifier.weight(1f))
284:       }
285:     }
286:   }
287: }
288: 
289: @Composable
290: fun AdaptiveThreePane(
291:   modifier: Modifier = Modifier,
292:   breakpoint: Dp = 680.dp,
293:   spacing: Dp = 16.dp,
294:   first: @Composable (Modifier) -> Unit,
295:   second: @Composable (Modifier) -> Unit,
296:   third: @Composable (Modifier) -> Unit
297: ) {
298:   BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
299:     if (maxWidth < breakpoint) {
300:       Column(
301:         modifier = Modifier.fillMaxWidth(),
302:         verticalArrangement = Arrangement.spacedBy(spacing)
303:       ) {
304:         first(Modifier.fillMaxWidth())
305:         second(Modifier.fillMaxWidth())
306:         third(Modifier.fillMaxWidth())
307:       }
308:     } else {
309:       Row(
310:         modifier = Modifier.fillMaxWidth(),
311:         horizontalArrangement = Arrangement.spacedBy(spacing)
312:       ) {
313:         first(Modifier.weight(1f))
314:         second(Modifier.weight(1f))
315:         third(Modifier.weight(1f))
316:       }
317:     }
318:   }
319: }
320: 
321: @Composable
322: fun BreatheCard(
323:   modifier: Modifier = Modifier,
324:   containerColor: Color = BreatheCardSurface.copy(alpha = 0.96f),
325:   contentPadding: PaddingValues = PaddingValues(CardSpacing),
326:   content: @Composable ColumnScope.() -> Unit
327: ) {
328:   Card(
329:     modifier = modifier
330:       .fillMaxWidth()
331:       .border(1.dp, BreatheBorder.copy(alpha = 0.45f), RoundedCornerShape(30.dp)),
332:     colors = CardDefaults.cardColors(containerColor = containerColor),
333:     shape = RoundedCornerShape(30.dp),
334:     elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
335:   ) {
336:     Column(
337:       modifier = Modifier.padding(contentPadding),
338:       verticalArrangement = Arrangement.spacedBy(16.dp)
339:     ) {
340:       content()
341:     }
342:   }
343: }
344: 
345: @Composable
346: fun HeroCard(
347:   eyebrow: String,
348:   title: String,
349:   body: String,
350:   modifier: Modifier = Modifier,
351:   accent: Color = BreatheAccentStrong,
352:   content: @Composable ColumnScope.() -> Unit = {}
353: ) {
354:   BreatheCard(
355:     modifier = modifier,
356:     containerColor = BreatheCardSurface,
357:     contentPadding = PaddingValues(HeroPadding)
358:   ) {
359:     Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = accent)
360:     Text(title, style = MaterialTheme.typography.headlineMedium, color = BreatheInk)
361:     Text(body, style = MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
362:     content()
363:   }
364: }
365: 
366: @Composable
367: fun PrimaryActionButton(
368:   text: String,
369:   onClick: () -> Unit,
370:   modifier: Modifier = Modifier,
371:   enabled: Boolean = true,
372:   icon: ImageVector? = null
373: ) {
374:   val shape = RoundedCornerShape(999.dp)
375:   val brush = if (enabled) {
376:     Brush.horizontalGradient(listOf(BreatheAccentStrong, BreatheAccent))
377:   } else {
378:     Brush.horizontalGradient(listOf(BreatheBorder.copy(alpha = 0.75f), BreatheBorder.copy(alpha = 0.75f)))
379:   }
380: 
381:   Button(
382:     onClick = onClick,
383:     enabled = enabled,
384:     modifier = modifier
385:       .fillMaxWidth()
386:       .heightIn(min = 52.dp),
387:     shape = shape,
388:     colors = ButtonDefaults.buttonColors(
389:       containerColor = Color.Transparent,
390:       contentColor = BreatheCanvas,
391:       disabledContainerColor = Color.Transparent,
392:       disabledContentColor = BreatheMutedInk
393:     ),
394:     contentPadding = PaddingValues(0.dp)
395:   ) {
396:     Row(
397:       modifier = Modifier
398:         .fillMaxWidth()
399:         .background(brush = brush, shape = shape)
400:         .padding(horizontal = 24.dp, vertical = 16.dp),
401:       horizontalArrangement = Arrangement.Center,
402:       verticalAlignment = Alignment.CenterVertically
403:     ) {
404:       if (icon != null) {
405:         Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
406:         Spacer(modifier = Modifier.size(10.dp))
407:       }
408:       Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
409:     }
410:   }
411: }
412: 
413: @Composable
414: fun SecondaryActionButton(
415:   text: String,
416:   onClick: () -> Unit,
417:   modifier: Modifier = Modifier,
418:   enabled: Boolean = true,
419:   icon: ImageVector? = null
420: ) {
421:   OutlinedButton(
422:     onClick = onClick,
423:     enabled = enabled,
424:     modifier = modifier
425:       .fillMaxWidth()
426:       .heightIn(min = 52.dp),
427:     colors = ButtonDefaults.outlinedButtonColors(contentColor = BreatheAccentStrong),
428:     border = androidx.compose.foundation.BorderStroke(1.dp, BreatheBorder),
429:     shape = RoundedCornerShape(999.dp),
430:     contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
431:   ) {
432:     if (icon != null) {
433:       Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
434:       Spacer(modifier = Modifier.size(10.dp))
435:     }
436:     Text(text, style = MaterialTheme.typography.labelLarge)
437:   }
438: }
439: 
440: @Composable
441: fun StatusPill(label: String, status: StatusLevel?, modifier: Modifier = Modifier) {
442:   val (bg, fg) = when (status) {
443:     StatusLevel.GREEN -> BreatheGreen.copy(alpha = 0.16f) to Color(0xFF245739)
444:     StatusLevel.YELLOW -> BreatheYellow.copy(alpha = 0.23f) to Color(0xFF6B4F18)
445:     StatusLevel.RED -> BreatheRed.copy(alpha = 0.18f) to Color(0xFF6F2D2D)
446:     null -> BreatheAccent.copy(alpha = 0.10f) to BreatheMutedInk
447:   }
448: 
449:   Row(
450:     modifier = modifier
451:       .background(bg, RoundedCornerShape(999.dp))
452:       .border(1.dp, BreatheBorder.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
453:       .padding(horizontal = 14.dp, vertical = 9.dp),
454:     verticalAlignment = Alignment.CenterVertically,
455:     horizontalArrangement = Arrangement.spacedBy(8.dp)
456:   ) {
457:     Box(
458:       modifier = Modifier
459:         .size(8.dp)
460:         .background(fg, CircleShape)
461:     )
462:     Text(label, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
463:   }
464: }
465: 
466: @Composable
467: fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
468:   Column(modifier = modifier) {
469:     Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
470:     Spacer(modifier = Modifier.height(6.dp))
471:     Text(value, style = MaterialTheme.typography.titleMedium, color = BreatheInk, fontWeight = FontWeight.SemiBold)
472:   }
473: }
474: 
475: @Composable
476: fun SectionTitle(text: String) {
477:   Text(text, style = MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
478: }
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
10:   data object Updates : Screen("updates", "Quick Updates")
11:   data object Voice : Screen("voice", "Voice Studio")
12:   data object Log : Screen("log", "Conflict Log")
13:   data object Insights : Screen("insights", "Insights")
14: }
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
7: import androidx.navigation.NavHostController
8: import androidx.navigation.compose.NavHost
9: import androidx.navigation.compose.composable
10: import androidx.navigation.compose.rememberNavController
11: import androidx.lifecycle.compose.collectAsStateWithLifecycle
12: import com.breathe.presentation.ui.calm.CalmScreen
13: import com.breathe.presentation.ui.calm.CalmViewModel
14: import com.breathe.presentation.ui.home.HomeScreen
15: import com.breathe.presentation.ui.home.HomeViewModel
16: import com.breathe.presentation.ui.insights.InsightsScreen
17: import com.breathe.presentation.ui.insights.InsightsViewModel
18: import com.breathe.presentation.ui.log.ConflictLogScreen
19: import com.breathe.presentation.ui.log.LogViewModel
20: import com.breathe.presentation.ui.pair.PairingScreen
21: import com.breathe.presentation.ui.pair.PairingViewModel
22: import com.breathe.presentation.ui.status.StatusScreen
23: import com.breathe.presentation.ui.status.StatusViewModel
24: import com.breathe.presentation.ui.timeout.TimeoutScreen
25: import com.breathe.presentation.ui.timeout.TimeoutViewModel
26: import com.breathe.presentation.ui.updates.QuickUpdatesScreen
27: import com.breathe.presentation.ui.updates.QuickUpdatesViewModel
28: import com.breathe.presentation.ui.voice.VoiceStudioScreen
29: import com.breathe.presentation.ui.voice.VoiceViewModel
30: 
31: @Composable
32: fun BreatheNavGraph() {
33:   val navController = rememberNavController()
34:   val appEntryViewModel = hiltViewModel<AppEntryViewModel>()
35:   val appEntryState by appEntryViewModel.uiState.collectAsStateWithLifecycle()
36: 
37:   val navigateTo: (String) -> Unit = { route ->
38:     navController.navigateSingleTop(route)
39:   }
40: 
41:   NavHost(
42:     navController = navController,
43:     startDestination = Screen.Loading.route
44:   ) {
45:     composable(Screen.Loading.route) {
46:       LaunchedEffect(appEntryState.isLoading, appEntryState.targetRoute) {
47:         val targetRoute = appEntryState.targetRoute
48:         if (!appEntryState.isLoading && targetRoute != null) {
49:           navController.navigate(targetRoute) {
50:             popUpTo(Screen.Loading.route) { inclusive = true }
51:             launchSingleTop = true
52:           }
53:         }
54:       }
55: 
56:       LoadingScreen()
57:     }
58: 
59:     composable(Screen.Pairing.route) {
60:       PairingScreen(
61:         viewModel = hiltViewModel<PairingViewModel>(),
62:         onContinue = { navController.navigateSingleTop(Screen.Home.route) }
63:       )
64:     }
65: 
66:     composable(Screen.Home.route) {
67:       HomeScreen(
68:         viewModel = hiltViewModel<HomeViewModel>(),
69:         onNavigate = navigateTo
70:       )
71:     }
72: 
73:     composable(Screen.Calm.route) {
74:       CalmScreen(
75:         viewModel = hiltViewModel<CalmViewModel>(),
76:         onNavigate = navigateTo
77:       )
78:     }
79: 
80:     composable(Screen.Timeout.route) {
81:       TimeoutScreen(
82:         viewModel = hiltViewModel<TimeoutViewModel>(),
83:         onNavigate = navigateTo
84:       )
85:     }
86: 
87:     composable(Screen.Status.route) {
88:       StatusScreen(
89:         viewModel = hiltViewModel<StatusViewModel>(),
90:         onNavigate = navigateTo
91:       )
92:     }
93: 
94:     composable(Screen.Updates.route) {
95:       QuickUpdatesScreen(
96:         viewModel = hiltViewModel<QuickUpdatesViewModel>(),
97:         onNavigate = navigateTo
98:       )
99:     }
100: 
101:     composable(Screen.Voice.route) {
102:       VoiceStudioScreen(
103:         viewModel = hiltViewModel<VoiceViewModel>(),
104:         onNavigate = navigateTo
105:       )
106:     }
107: 
108:     composable(Screen.Log.route) {
109:       ConflictLogScreen(
110:         viewModel = hiltViewModel<LogViewModel>(),
111:         onNavigate = navigateTo
112:       )
113:     }
114: 
115:     composable(Screen.Insights.route) {
116:       InsightsScreen(
117:         viewModel = hiltViewModel<InsightsViewModel>(),
118:         onNavigate = navigateTo
119:       )
120:     }
121:   }
122: }
123: 
124: private fun NavHostController.navigateSingleTop(route: String) {
125:   navigate(route) {
126:     launchSingleTop = true
127:     restoreState = true
128:   }
129: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/home/HomeScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.home
2: 
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.border
5: import androidx.compose.foundation.clickable
6: import androidx.compose.foundation.layout.Arrangement
7: import androidx.compose.foundation.layout.Box
8: import androidx.compose.foundation.layout.BoxWithConstraints
9: import androidx.compose.foundation.layout.Column
10: import androidx.compose.foundation.layout.Row
11: import androidx.compose.foundation.layout.Spacer
12: import androidx.compose.foundation.layout.fillMaxWidth
13: import androidx.compose.foundation.layout.height
14: import androidx.compose.foundation.layout.heightIn
15: import androidx.compose.foundation.layout.padding
16: import androidx.compose.foundation.layout.size
17: import androidx.compose.foundation.shape.CircleShape
18: import androidx.compose.foundation.shape.RoundedCornerShape
19: import androidx.compose.material.icons.Icons
20: import androidx.compose.material.icons.rounded.AutoGraph
21: import androidx.compose.material.icons.rounded.EditNote
22: import androidx.compose.material.icons.rounded.Favorite
23: import androidx.compose.material.icons.rounded.FormatQuote
24: import androidx.compose.material.icons.rounded.Mic
25: import androidx.compose.material.icons.rounded.SelfImprovement
26: import androidx.compose.material.icons.rounded.Spa
27: import androidx.compose.material.icons.rounded.Timer
28: import androidx.compose.material3.Icon
29: import androidx.compose.material3.Text
30: import androidx.compose.runtime.Composable
31: import androidx.compose.runtime.getValue
32: import androidx.compose.ui.Alignment
33: import androidx.compose.ui.Modifier
34: import androidx.compose.ui.graphics.Color
35: import androidx.compose.ui.graphics.vector.ImageVector
36: import androidx.compose.ui.text.font.FontStyle
37: import androidx.compose.ui.text.font.FontWeight
38: import androidx.compose.ui.text.style.TextAlign
39: import androidx.compose.ui.text.style.TextOverflow
40: import androidx.compose.ui.unit.dp
41: import androidx.compose.ui.unit.sp
42: import androidx.hilt.navigation.compose.hiltViewModel
43: import androidx.lifecycle.ViewModel
44: import androidx.lifecycle.compose.collectAsStateWithLifecycle
45: import androidx.lifecycle.viewModelScope
46: import com.breathe.domain.model.QuickUpdate
47: import com.breathe.domain.model.StatusLevel
48: import com.breathe.domain.repository.QuickUpdateRepository
49: import com.breathe.domain.repository.StatusRepository
50: import com.breathe.presentation.navigation.Screen
51: import com.breathe.presentation.theme.BreatheAccent
52: import com.breathe.presentation.theme.BreatheAccentStrong
53: import com.breathe.presentation.theme.BreatheBorder
54: import com.breathe.presentation.theme.BreatheCanvas
55: import com.breathe.presentation.theme.BreatheCardSurface
56: import com.breathe.presentation.theme.BreatheGreen
57: import com.breathe.presentation.theme.BreatheInk
58: import com.breathe.presentation.theme.BreatheMutedInk
59: import com.breathe.presentation.theme.BreatheOverlay
60: import com.breathe.presentation.theme.BreatheRed
61: import com.breathe.presentation.theme.BreatheYellow
62: import com.breathe.presentation.ui.common.AdaptiveTwoPane
63: import com.breathe.presentation.ui.common.AppScreen
64: import com.breathe.presentation.ui.common.BreatheCard
65: import com.breathe.presentation.ui.common.SectionTitle
66: import dagger.hilt.android.lifecycle.HiltViewModel
67: import java.time.Duration
68: import java.time.Instant
69: import javax.inject.Inject
70: import kotlinx.coroutines.flow.SharingStarted
71: import kotlinx.coroutines.flow.StateFlow
72: import kotlinx.coroutines.flow.combine
73: import kotlinx.coroutines.flow.stateIn
74: 
75: data class HomeUiState(
76:   val ownStatus: StatusLevel? = null,
77:   val partnerStatus: StatusLevel? = null,
78:   val wsConnected: Boolean = false,
79:   val recentUpdates: List<QuickUpdate> = emptyList()
80: )
81: 
82: @HiltViewModel
83: class HomeViewModel @Inject constructor(
84:   statusRepository: StatusRepository,
85:   quickUpdateRepository: QuickUpdateRepository
86: ) : ViewModel() {
87:   val uiState: StateFlow<HomeUiState> = combine(
88:     statusRepository.observeOwnStatus(),
89:     statusRepository.observePartnerStatus(),
90:     statusRepository.observeWsConnection(),
91:     quickUpdateRepository.observeRecentUpdates(limit = 2)
92:   ) { own, partner, connected, updates ->
93:     HomeUiState(
94:       ownStatus = own,
95:       partnerStatus = partner,
96:       wsConnected = connected,
97:       recentUpdates = updates
98:     )
99:   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
100: }
101: 
102: @Composable
103: fun HomeScreen(
104:   onNavigate: (String) -> Unit,
105:   viewModel: HomeViewModel = hiltViewModel()
106: ) {
107:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
108: 
109:   AppScreen(
110:     title = "Breathe",
111:     subtitle = "A softer command center for slowing things down before conflict starts steering the room.",
112:     showBottomNav = true,
113:     selectedBottomRoute = Screen.Home.route,
114:     onNavigate = onNavigate
115:   ) {
116:     HomeLiveStateCard(uiState = uiState)
117: 
118:     BreatheCard {
119:       Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
120:         Text(
121:           text = "IMMEDIATE ACTIONS",
122:           style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
123:           color = BreatheMutedInk
124:         )
125:         SectionTitle("Regulation tools")
126:       }
127: 
128:       Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
129:         Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
130:           ToolGridButton(
131:             title = "Status",
132:             icon = Icons.Rounded.Favorite,
133:             onClick = { onNavigate(Screen.Status.route) },
134:             modifier = Modifier.weight(1f)
135:           )
136:           ToolGridButton(
137:             title = "Calm",
138:             icon = Icons.Rounded.SelfImprovement,
139:             onClick = { onNavigate(Screen.Calm.route) },
140:             modifier = Modifier.weight(1f)
141:           )
142:         }
143:         Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
144:           ToolGridButton(
145:             title = "Timeout",
146:             icon = Icons.Rounded.Timer,
147:             onClick = { onNavigate(Screen.Timeout.route) },
148:             modifier = Modifier.weight(1f)
149:           )
150:           ToolGridButton(
151:             title = "Voice",
152:             icon = Icons.Rounded.Mic,
153:             onClick = { onNavigate(Screen.Voice.route) },
154:             modifier = Modifier.weight(1f)
155:           )
156:         }
157:         Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
158:           ToolGridButton(
159:             title = "Log",
160:             icon = Icons.Rounded.EditNote,
161:             onClick = { onNavigate(Screen.Log.route) },
162:             modifier = Modifier.weight(1f)
163:           )
164:           ToolGridButton(
165:             title = "Insights",
166:             icon = Icons.Rounded.AutoGraph,
167:             onClick = { onNavigate(Screen.Insights.route) },
168:             modifier = Modifier.weight(1f)
169:           )
170:         }
171:       }
172:     }
173: 
174:     BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.16f)) {
175:       Row(
176:         modifier = Modifier.fillMaxWidth(),
177:         horizontalArrangement = Arrangement.spacedBy(16.dp),
178:         verticalAlignment = Alignment.Top
179:       ) {
180:         Icon(
181:           imageVector = Icons.Rounded.FormatQuote,
182:           contentDescription = null,
183:           tint = Color(0xFF775A00),
184:           modifier = Modifier.padding(top = 2.dp).size(32.dp)
185:         )
186:         Column(
187:           modifier = Modifier.weight(1f),
188:           verticalArrangement = Arrangement.spacedBy(8.dp)
189:         ) {
190:           Text(
191:             text = "In the pause between stimulus and response lies our growth and our freedom.",
192:             style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(lineHeight = 40.sp),
193:             color = Color(0xFF5A4300),
194:             fontStyle = FontStyle.Italic
195:           )
196:           Text(
197:             text = "- Viktor Frankl",
198:             style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
199:             color = Color(0xFF775A00)
200:           )
201:         }
202:       }
203:     }
204: 
205:     AdaptiveTwoPane(
206:       first = { paneModifier ->
207:         BreatheCard(modifier = paneModifier, containerColor = BreatheOverlay.copy(alpha = 0.55f)) {
208:           Text("Recent harmony", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheAccentStrong)
209:           Box(
210:             modifier = Modifier
211:               .fillMaxWidth()
212:               .height(132.dp)
213:               .background(BreatheCardSurface, RoundedCornerShape(18.dp)),
214:             contentAlignment = Alignment.Center
215:           ) {
216:             Text(
217:               text = "82%",
218:               style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
219:               color = BreatheAccentStrong
220:             )
221:           }
222:         }
223:       },
224:       second = { paneModifier ->
225:         BreatheCard(modifier = paneModifier, containerColor = BreatheOverlay.copy(alpha = 0.55f)) {
226:           Text("Daily intent", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheAccentStrong)
227:           Text(
228:             text = "Active listening without fixing.",
229:             style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
230:             color = BreatheMutedInk
231:           )
232:           Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
233:             Box(
234:               modifier = Modifier
235:                 .size(32.dp)
236:                 .background(BreatheAccent.copy(alpha = 0.55f), CircleShape)
237:                 .border(2.dp, BreatheCanvas, CircleShape)
238:             )
239:             Box(
240:               modifier = Modifier
241:                 .size(32.dp)
242:                 .background(BreatheYellow.copy(alpha = 0.6f), CircleShape)
243:                 .border(2.dp, BreatheCanvas, CircleShape)
244:             )
245:           }
246:         }
247:       }
248:     )
249:   }
250: }
251: 
252: @Composable
253: private fun HomeLiveStateCard(uiState: HomeUiState) {
254:   BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.74f)) {
255:     BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
256:       val compact = maxWidth < 380.dp
257: 
258:       Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
259:         Row(
260:           modifier = Modifier.fillMaxWidth(),
261:           horizontalArrangement = Arrangement.SpaceBetween,
262:           verticalAlignment = Alignment.CenterVertically
263:         ) {
264:           Text(
265:             text = "LIVE STATE",
266:             style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
267:             color = BreatheAccentStrong.copy(alpha = 0.66f)
268:           )
269:           StatusConnectionIndicator(connected = uiState.wsConnected, compact = compact)
270:         }
271:         Text(
272:           text = "Emotional Weather",
273:           style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
274:           color = BreatheInk
275:         )
276:       }
277:     }
278: 
279:     AdaptiveTwoPane(
280:       first = { paneModifier ->
281:         WeatherStatusCard(
282:           modifier = paneModifier,
283:           heading = "You",
284:           status = uiState.ownStatus,
285:           primary = true
286:         )
287:       },
288:       second = { paneModifier ->
289:         WeatherStatusCard(
290:           modifier = paneModifier,
291:           heading = "Partner",
292:           status = uiState.partnerStatus,
293:           primary = false
294:         )
295:       }
296:     )
297: 
298:     Column(
299:       modifier = Modifier
300:         .fillMaxWidth()
301:         .border(1.dp, BreatheBorder.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
302:         .background(BreatheCardSurface.copy(alpha = 0.54f), RoundedCornerShape(20.dp))
303:         .padding(16.dp),
304:       verticalArrangement = Arrangement.spacedBy(8.dp)
305:     ) {
306:       Text(
307:         text = "QUICK UPDATES",
308:         style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
309:         color = BreatheAccentStrong.copy(alpha = 0.58f)
310:       )
311: 
312:       buildDisplayUpdates(uiState.recentUpdates).forEach { item ->
313:         Row(
314:           modifier = Modifier
315:             .fillMaxWidth()
316:             .background(BreatheCanvas.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
317:             .padding(horizontal = 14.dp, vertical = 12.dp),
318:           horizontalArrangement = Arrangement.SpaceBetween,
319:           verticalAlignment = Alignment.CenterVertically
320:         ) {
321:           Row(
322:             modifier = Modifier.weight(1f),
323:             horizontalArrangement = Arrangement.spacedBy(10.dp),
324:             verticalAlignment = Alignment.CenterVertically
325:           ) {
326:             Box(
327:               modifier = Modifier
328:                 .size(6.dp)
329:                 .background(item.dotColor, CircleShape)
330:             )
331:             Text(
332:               text = item.label,
333:               style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
334:               color = BreatheInk,
335:               fontWeight = FontWeight.Medium,
336:               maxLines = 2,
337:               overflow = TextOverflow.Ellipsis
338:             )
339:           }
340:           Text(
341:             text = item.time,
342:             style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
343:             color = BreatheMutedInk,
344:             textAlign = TextAlign.End
345:           )
346:         }
347:       }
348:     }
349:   }
350: }
351: 
352: @Composable
353: private fun WeatherStatusCard(
354:   heading: String,
355:   status: StatusLevel?,
356:   primary: Boolean,
357:   modifier: Modifier = Modifier
358: ) {
359:   val icon = when (status) {
360:     StatusLevel.GREEN -> Icons.Rounded.Spa
361:     StatusLevel.YELLOW -> Icons.Rounded.Favorite
362:     StatusLevel.RED -> Icons.Rounded.Timer
363:     null -> if (primary) Icons.Rounded.Spa else Icons.Rounded.Favorite
364:   }
365:   val accent = when (status) {
366:     StatusLevel.GREEN -> BreatheGreen
367:     StatusLevel.YELLOW -> BreatheYellow
368:     StatusLevel.RED -> BreatheRed
369:     null -> if (primary) BreatheAccentStrong else BreatheYellow
370:   }
371:   val chipBackground = when (status) {
372:     StatusLevel.GREEN -> BreatheAccentStrong
373:     StatusLevel.YELLOW -> BreatheYellow.copy(alpha = 0.35f)
374:     StatusLevel.RED -> BreatheRed.copy(alpha = 0.18f)
375:     null -> BreatheBorder.copy(alpha = 0.45f)
376:   }
377:   val chipText = when (status) {
378:     StatusLevel.GREEN -> BreatheCanvas
379:     StatusLevel.YELLOW -> Color(0xFF5A4300)
380:     StatusLevel.RED -> Color(0xFF6F2D2D)
381:     null -> BreatheMutedInk
382:   }
383: 
384:   BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
385:     val compact = maxWidth < 172.dp
386:     val narrowChip = maxWidth < 140.dp
387:     val iconSize = if (compact) 56.dp else 64.dp
388: 
389:     Column(
390:       modifier = Modifier
391:         .fillMaxWidth()
392:         .heightIn(min = 176.dp)
393:         .background(BreatheCanvas.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
394:         .padding(horizontal = 16.dp, vertical = 16.dp),
395:       horizontalAlignment = Alignment.CenterHorizontally,
396:       verticalArrangement = Arrangement.spacedBy(16.dp)
397:     ) {
398:       Text(
399:         text = heading.uppercase(),
400:         style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
401:         color = BreatheAccentStrong.copy(alpha = 0.64f)
402:       )
403: 
404:       Box(
405:         modifier = Modifier
406:           .size(iconSize)
407:           .background(accent.copy(alpha = 0.14f), CircleShape),
408:         contentAlignment = Alignment.Center
409:       ) {
410:         Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(30.dp))
411:       }
412: 
413:       Text(
414:         text = homeStatusLabel(status),
415:         modifier = Modifier
416:           .fillMaxWidth(if (narrowChip) 1f else 0.86f)
417:           .background(chipBackground, RoundedCornerShape(999.dp))
418:           .padding(horizontal = 14.dp, vertical = 8.dp),
419:         style = if (narrowChip) {
420:           androidx.compose.material3.MaterialTheme.typography.labelMedium
421:         } else {
422:           androidx.compose.material3.MaterialTheme.typography.labelLarge
423:         },
424:         color = chipText,
425:         fontWeight = FontWeight.Bold,
426:         textAlign = TextAlign.Center,
427:         maxLines = if (narrowChip || compact) 2 else 1,
428:         overflow = TextOverflow.Ellipsis
429:       )
430:     }
431:   }
432: }
433: 
434: @Composable
435: private fun StatusConnectionIndicator(connected: Boolean, compact: Boolean) {
436:   Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
437:     Box(
438:       modifier = Modifier
439:         .size(8.dp)
440:         .background(if (connected) BreatheAccentStrong else BreatheBorder, CircleShape)
441:     )
442:     Text(
443:       text = if (compact) {
444:         if (connected) "Linked" else "Offline"
445:       } else {
446:         if (connected) "Realtime linked" else "Offline-first mode"
447:       },
448:       style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
449:       color = BreatheAccentStrong.copy(alpha = 0.72f),
450:       textAlign = TextAlign.End,
451:       maxLines = 1,
452:       overflow = TextOverflow.Ellipsis
453:     )
454:   }
455: }
456: 
457: @Composable
458: private fun ToolGridButton(
459:   title: String,
460:   icon: ImageVector,
461:   onClick: () -> Unit,
462:   modifier: Modifier = Modifier
463: ) {
464:   Column(
465:     modifier = modifier
466:       .background(BreatheOverlay.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
467:       .heightIn(min = 116.dp)
468:       .clickable(onClick = onClick)
469:       .padding(16.dp),
470:     horizontalAlignment = Alignment.CenterHorizontally,
471:     verticalArrangement = Arrangement.Center
472:   ) {
473:     Box(
474:       modifier = Modifier
475:         .size(48.dp)
476:         .background(BreatheCardSurface, CircleShape),
477:       contentAlignment = Alignment.Center
478:     ) {
479:       Icon(imageVector = icon, contentDescription = title, tint = BreatheAccentStrong)
480:     }
481:     Spacer(modifier = Modifier.height(12.dp))
482:     Text(
483:       text = title,
484:       color = BreatheInk,
485:       style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
486:       textAlign = TextAlign.Center,
487:       maxLines = 2,
488:       overflow = TextOverflow.Ellipsis
489:     )
490:   }
491: }
492: 
493: private data class HomeUpdateRow(
494:   val label: String,
495:   val time: String,
496:   val dotColor: Color
497: )
498: 
499: private fun buildDisplayUpdates(updates: List<QuickUpdate>): List<HomeUpdateRow> {
500:   val rows = updates.map { update ->
501:     val sender = if (update.isOwn) "You" else "Partner"
502:     HomeUpdateRow(
503:       label = buildString {
504:         append(sender)
505:         append(": ")
506:         append(update.message)
507:         if (!update.note.isNullOrBlank()) {
508:           append(" - ")
509:           append(update.note)
510:         }
511:       },
512:       time = relativeTime(update.createdAt),
513:       dotColor = if (update.isOwn) BreatheAccentStrong else BreatheYellow
514:     )
515:   }.toMutableList()
516: 
517:   while (rows.size < 2) {
518:     rows += HomeUpdateRow(
519:       label = if (rows.isEmpty()) {
520:         "You: No quick update yet"
521:       } else {
522:         "Partner: No reply yet"
523:       },
524:       time = "now",
525:       dotColor = BreatheBorder
526:     )
527:   }
528: 
529:   return rows.take(2)
530: }
531: 
532: private fun homeStatusLabel(status: StatusLevel?): String = when (status) {
533:   StatusLevel.GREEN -> "Open & Steady"
534:   StatusLevel.YELLOW -> "Tension Rising"
535:   StatusLevel.RED -> "Pause Needed"
536:   null -> "Unset"
537: }
538: 
539: private fun relativeTime(iso: String): String {
540:   val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return "now"
541:   val minutes = Duration.between(instant, Instant.now()).toMinutes().coerceAtLeast(0)
542:   return when {
543:     minutes < 1 -> "now"
544:     minutes < 60 -> "${minutes}m"
545:     else -> "${minutes / 60}h"
546:   }
547: }
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
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.border
5: import androidx.compose.foundation.layout.Arrangement
6: import androidx.compose.foundation.layout.Box
7: import androidx.compose.foundation.layout.BoxWithConstraints
8: import androidx.compose.foundation.layout.Column
9: import androidx.compose.foundation.layout.Row
10: import androidx.compose.foundation.layout.fillMaxWidth
11: import androidx.compose.foundation.layout.heightIn
12: import androidx.compose.foundation.layout.padding
13: import androidx.compose.foundation.layout.size
14: import androidx.compose.foundation.shape.CircleShape
15: import androidx.compose.foundation.shape.RoundedCornerShape
16: import androidx.compose.material.icons.Icons
17: import androidx.compose.material.icons.rounded.Favorite
18: import androidx.compose.material.icons.rounded.Mic
19: import androidx.compose.material.icons.rounded.Timer
20: import androidx.compose.material3.Icon
21: import androidx.compose.material3.Text
22: import androidx.compose.runtime.Composable
23: import androidx.compose.runtime.getValue
24: import androidx.compose.ui.Alignment
25: import androidx.compose.ui.Modifier
26: import androidx.compose.ui.graphics.Brush
27: import androidx.compose.ui.graphics.vector.ImageVector
28: import androidx.compose.ui.text.style.TextOverflow
29: import androidx.compose.ui.unit.dp
30: import androidx.hilt.navigation.compose.hiltViewModel
31: import androidx.lifecycle.ViewModel
32: import androidx.lifecycle.compose.collectAsStateWithLifecycle
33: import androidx.lifecycle.viewModelScope
34: import com.breathe.domain.model.CalmSession
35: import com.breathe.domain.model.SessionFeature
36: import com.breathe.domain.repository.SessionRepository
37: import com.breathe.domain.usecase.EndSessionUseCase
38: import com.breathe.domain.usecase.SendPeaceUseCase
39: import com.breathe.domain.usecase.StartSessionUseCase
40: import com.breathe.presentation.theme.BreatheAccent
41: import com.breathe.presentation.theme.BreatheAccentStrong
42: import com.breathe.presentation.theme.BreatheBorder
43: import com.breathe.presentation.theme.BreatheCardSurface
44: import com.breathe.presentation.theme.BreatheGreen
45: import com.breathe.presentation.theme.BreatheInk
46: import com.breathe.presentation.theme.BreatheMutedInk
47: import com.breathe.presentation.theme.BreatheOverlay
48: import com.breathe.presentation.theme.BreatheYellow
49: import com.breathe.presentation.navigation.Screen
50: import com.breathe.presentation.ui.common.AppScreen
51: import com.breathe.presentation.ui.common.BreatheCard
52: import com.breathe.presentation.ui.common.PrimaryActionButton
53: import com.breathe.presentation.ui.common.SecondaryActionButton
54: import dagger.hilt.android.lifecycle.HiltViewModel
55: import javax.inject.Inject
56: import kotlinx.coroutines.Job
57: import kotlinx.coroutines.delay
58: import kotlinx.coroutines.flow.MutableStateFlow
59: import kotlinx.coroutines.flow.StateFlow
60: import kotlinx.coroutines.flow.asStateFlow
61: import kotlinx.coroutines.flow.collect
62: import kotlinx.coroutines.flow.update
63: import kotlinx.coroutines.launch
64: 
65: data class CalmUiState(
66:   val secondsRemaining: Int = 0,
67:   val voiceTrack: String? = null,
68:   val sessionId: Long? = null,
69:   val isActive: Boolean = false,
70:   val lastSignalMessage: String? = null
71: )
72: 
73: sealed interface CalmUiEvent {
74:   data object StartSession : CalmUiEvent
75:   data object CompleteSession : CalmUiEvent
76:   data object SendPeace : CalmUiEvent
77: }
78: 
79: @HiltViewModel
80: class CalmViewModel @Inject constructor(
81:   sessionRepository: SessionRepository,
82:   private val startSessionUseCase: StartSessionUseCase,
83:   private val endSessionUseCase: EndSessionUseCase,
84:   private val sendPeaceUseCase: SendPeaceUseCase
85: ) : ViewModel() {
86:   private val _uiState = MutableStateFlow(CalmUiState())
87:   val uiState: StateFlow<CalmUiState> = _uiState.asStateFlow()
88:   private var countdownJob: Job? = null
89: 
90:   init {
91:     viewModelScope.launch {
92:       sessionRepository.observeActiveCalmSession().collect { session ->
93:         countdownJob?.cancel()
94:         if (session == null) {
95:           _uiState.value = CalmUiState(lastSignalMessage = _uiState.value.lastSignalMessage)
96:         } else {
97:           _uiState.value = session.toUiState(lastSignalMessage = _uiState.value.lastSignalMessage)
98:           startCountdown(session.sessionId, session.secondsRemaining)
99:         }
100:       }
101:     }
102:   }
103: 
104:   fun onEvent(event: CalmUiEvent) {
105:     when (event) {
106:       CalmUiEvent.StartSession -> viewModelScope.launch {
107:         _uiState.update { it.copy(lastSignalMessage = null) }
108:         startSessionUseCase(SessionFeature.CALM)
109:       }
110: 
111:       CalmUiEvent.CompleteSession -> viewModelScope.launch {
112:         _uiState.value.sessionId?.let { endSessionUseCase(it) }
113:       }
114: 
115:       CalmUiEvent.SendPeace -> viewModelScope.launch {
116:         sendPeaceUseCase()
117:         _uiState.update { it.copy(lastSignalMessage = "A gentle peace signal was sent to your partner.") }
118:       }
119:     }
120:   }
121: 
122:   private fun startCountdown(sessionId: Long?, initialSeconds: Int) {
123:     if (sessionId == null) return
124: 
125:     countdownJob = viewModelScope.launch {
126:       var remaining = initialSeconds
127:       while (remaining > 0) {
128:         delay(1_000)
129:         remaining -= 1
130:         _uiState.update {
131:           if (it.sessionId == sessionId) it.copy(secondsRemaining = remaining.coerceAtLeast(0)) else it
132:         }
133:       }
134:       endSessionUseCase(sessionId)
135:     }
136:   }
137: 
138:   private fun CalmSession?.toUiState(lastSignalMessage: String?): CalmUiState = CalmUiState(
139:     secondsRemaining = this?.secondsRemaining ?: 0,
140:     voiceTrack = this?.voiceTrack,
141:     sessionId = this?.sessionId,
142:     isActive = this?.sessionId != null && this.secondsRemaining > 0,
143:     lastSignalMessage = lastSignalMessage
144:   )
145: }
146: 
147: @Composable
148: fun CalmScreen(
149:   onNavigate: (String) -> Unit = {},
150:   viewModel: CalmViewModel = hiltViewModel()
151: ) {
152:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
153: 
154:   AppScreen(
155:     title = "Calm session",
156:     subtitle = "A soft reset before words get faster than your nervous system can handle.",
157:     showBottomNav = true,
158:     selectedBottomRoute = null,
159:     onNavigate = onNavigate
160:   ) {
161:     BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.48f)) {
162:       Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
163:         CalmOrb(secondsRemaining = uiState.secondsRemaining)
164: 
165:         Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
166:           Text(
167:             text = "SESSION DETAILS",
168:             style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
169:             color = BreatheMutedInk
170:           )
171:           SessionInfoRow(icon = Icons.Rounded.Mic, label = "Voice track", value = uiState.voiceTrack ?: "Not selected")
172:           SessionInfoRow(icon = Icons.Rounded.Timer, label = "Seconds remaining", value = uiState.secondsRemaining.toString())
173:         }
174:       }
175:     }
176: 
177:     Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
178:       PrimaryActionButton(
179:         text = if (uiState.isActive) "Calm session active" else "Start calm session",
180:         onClick = { viewModel.onEvent(CalmUiEvent.StartSession) },
181:         enabled = !uiState.isActive
182:       )
183: 
184:       if (uiState.isActive) {
185:         SecondaryActionButton(
186:           text = "Complete calm session",
187:           onClick = { viewModel.onEvent(CalmUiEvent.CompleteSession) }
188:         )
189:       }
190: 
191:       SecondaryActionButton(
192:         text = "Send peace",
193:         onClick = { viewModel.onEvent(CalmUiEvent.SendPeace) },
194:         icon = Icons.Rounded.Favorite
195:       )
196:     }
197: 
198:     uiState.lastSignalMessage?.let { message ->
199:       BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.16f)) {
200:         Text(message, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = BreatheInk)
201:       }
202:     }
203:   }
204: }
205: 
206: @Composable
207: private fun CalmOrb(secondsRemaining: Int) {
208:   BoxWithConstraints(
209:     modifier = Modifier.fillMaxWidth(),
210:     contentAlignment = Alignment.Center
211:   ) {
212:     val outerSize = maxWidth.coerceAtMost(268.dp)
213:     val middleSize = outerSize * 0.88f
214:     val innerSize = outerSize * 0.78f
215: 
216:     Box(
217:       modifier = Modifier
218:         .size(outerSize)
219:         .background(BreatheAccent.copy(alpha = 0.08f), CircleShape),
220:       contentAlignment = Alignment.Center
221:     ) {
222:       Box(
223:         modifier = Modifier
224:           .size(middleSize)
225:           .background(BreatheAccent.copy(alpha = 0.12f), CircleShape),
226:         contentAlignment = Alignment.Center
227:       ) {
228:         Box(
229:           modifier = Modifier
230:             .size(innerSize)
231:             .background(
232:               brush = Brush.linearGradient(
233:                 colors = listOf(BreatheCardSurface, BreatheGreen.copy(alpha = 0.18f), BreatheYellow.copy(alpha = 0.18f))
234:               ),
235:               shape = CircleShape
236:             )
237:             .border(1.dp, BreatheBorder.copy(alpha = 0.4f), CircleShape),
238:           contentAlignment = Alignment.Center
239:         ) {
240:           Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
241:             Text(
242:               text = formatMinutes(secondsRemaining),
243:               style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
244:               color = BreatheAccentStrong
245:             )
246:             Text(
247:               text = "MINUTES",
248:               style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
249:               color = BreatheAccentStrong.copy(alpha = 0.6f)
250:             )
251:           }
252:         }
253:       }
254:     }
255:   }
256: }
257: 
258: @Composable
259: private fun SessionInfoRow(icon: ImageVector, label: String, value: String) {
260:   Row(
261:     modifier = Modifier
262:       .fillMaxWidth()
263:       .heightIn(min = 72.dp)
264:       .background(BreatheCardSurface, RoundedCornerShape(18.dp))
265:       .padding(horizontal = 16.dp, vertical = 14.dp),
266:     horizontalArrangement = Arrangement.spacedBy(12.dp),
267:     verticalAlignment = Alignment.CenterVertically
268:   ) {
269:     Box(
270:       modifier = Modifier
271:         .size(40.dp)
272:         .background(BreatheAccent.copy(alpha = 0.14f), CircleShape),
273:       contentAlignment = Alignment.Center
274:     ) {
275:       Icon(imageVector = icon, contentDescription = null, tint = BreatheAccentStrong)
276:     }
277: 
278:     Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
279:       Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
280:       Text(
281:         text = value,
282:         style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
283:         color = BreatheInk,
284:         maxLines = 2,
285:         overflow = TextOverflow.Ellipsis
286:       )
287:     }
288:   }
289: }
290: 
291: private fun formatMinutes(seconds: Int): String {
292:   val safeSeconds = seconds.coerceAtLeast(0)
293:   val minutes = safeSeconds / 60
294:   val remainder = safeSeconds % 60
295:   return "%02d:%02d".format(minutes, remainder)
296: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/timeout/TimeoutScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.timeout
2: 
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.layout.Arrangement
5: import androidx.compose.foundation.layout.Box
6: import androidx.compose.foundation.layout.BoxWithConstraints
7: import androidx.compose.foundation.layout.Column
8: import androidx.compose.foundation.layout.Row
9: import androidx.compose.foundation.layout.fillMaxWidth
10: import androidx.compose.foundation.layout.padding
11: import androidx.compose.foundation.layout.size
12: import androidx.compose.foundation.shape.CircleShape
13: import androidx.compose.foundation.shape.RoundedCornerShape
14: import androidx.compose.material.icons.Icons
15: import androidx.compose.material.icons.rounded.CheckCircle
16: import androidx.compose.material.icons.rounded.DoorFront
17: import androidx.compose.material.icons.rounded.Lock
18: import androidx.compose.material.icons.rounded.Security
19: import androidx.compose.material.icons.rounded.SelfImprovement
20: import androidx.compose.material3.CircularProgressIndicator
21: import androidx.compose.material3.Icon
22: import androidx.compose.material3.Text
23: import androidx.compose.runtime.Composable
24: import androidx.compose.runtime.getValue
25: import androidx.compose.ui.Alignment
26: import androidx.compose.ui.Modifier
27: import androidx.compose.ui.graphics.Color
28: import androidx.compose.ui.unit.dp
29: import androidx.hilt.navigation.compose.hiltViewModel
30: import androidx.lifecycle.ViewModel
31: import androidx.lifecycle.compose.collectAsStateWithLifecycle
32: import androidx.lifecycle.viewModelScope
33: import com.breathe.domain.model.SessionFeature
34: import com.breathe.domain.model.TimeoutLock
35: import com.breathe.domain.repository.SessionRepository
36: import com.breathe.domain.usecase.EndSessionUseCase
37: import com.breathe.domain.usecase.StartSessionUseCase
38: import com.breathe.presentation.navigation.Screen
39: import com.breathe.presentation.theme.BreatheAccentStrong
40: import com.breathe.presentation.theme.BreatheBorder
41: import com.breathe.presentation.theme.BreatheCanvas
42: import com.breathe.presentation.theme.BreatheCardSurface
43: import com.breathe.presentation.theme.BreatheInk
44: import com.breathe.presentation.theme.BreatheMutedInk
45: import com.breathe.presentation.theme.BreatheOverlay
46: import com.breathe.presentation.theme.BreatheRed
47: import com.breathe.presentation.theme.BreatheYellow
48: import com.breathe.presentation.ui.common.AdaptiveTwoPane
49: import com.breathe.presentation.ui.common.AppScreen
50: import com.breathe.presentation.ui.common.BreatheCard
51: import com.breathe.presentation.ui.common.PrimaryActionButton
52: import dagger.hilt.android.lifecycle.HiltViewModel
53: import javax.inject.Inject
54: import kotlinx.coroutines.Job
55: import kotlinx.coroutines.delay
56: import kotlinx.coroutines.flow.MutableStateFlow
57: import kotlinx.coroutines.flow.StateFlow
58: import kotlinx.coroutines.flow.asStateFlow
59: import kotlinx.coroutines.flow.collect
60: import kotlinx.coroutines.flow.update
61: import kotlinx.coroutines.launch
62: 
63: data class TimeoutUiState(
64:   val sessionId: Long? = null,
65:   val secondsRemaining: Int = 0,
66:   val isLocked: Boolean = false,
67:   val unlocksAt: String? = null
68: )
69: 
70: sealed interface TimeoutUiEvent {
71:   data object StartTimeout : TimeoutUiEvent
72: }
73: 
74: @HiltViewModel
75: class TimeoutViewModel @Inject constructor(
76:   sessionRepository: SessionRepository,
77:   private val startSessionUseCase: StartSessionUseCase,
78:   private val endSessionUseCase: EndSessionUseCase
79: ) : ViewModel() {
80:   private val _uiState = MutableStateFlow(TimeoutUiState())
81:   val uiState: StateFlow<TimeoutUiState> = _uiState.asStateFlow()
82:   private var countdownJob: Job? = null
83: 
84:   init {
85:     viewModelScope.launch {
86:       sessionRepository.observeTimeoutLock().collect { lock ->
87:         countdownJob?.cancel()
88:         _uiState.value = lock.toUiState()
89:         if (lock.isLocked) {
90:           startCountdown(lock.sessionId, lock.secondsRemaining)
91:         }
92:       }
93:     }
94:   }
95: 
96:   fun onEvent(event: TimeoutUiEvent) {
97:     when (event) {
98:       TimeoutUiEvent.StartTimeout -> viewModelScope.launch { startSessionUseCase(SessionFeature.TIMEOUT) }
99:     }
100:   }
101: 
102:   private fun startCountdown(sessionId: Long?, initialSeconds: Int) {
103:     countdownJob = viewModelScope.launch {
104:       var remaining = initialSeconds
105:       while (remaining > 0) {
106:         delay(1_000)
107:         remaining -= 1
108:         _uiState.update {
109:           if (sessionId == null || it.sessionId == sessionId || it.sessionId == null) {
110:             it.copy(secondsRemaining = remaining.coerceAtLeast(0), isLocked = remaining > 0)
111:           } else {
112:             it
113:           }
114:         }
115:       }
116:       sessionId?.let { endSessionUseCase(it) }
117:     }
118:   }
119: 
120:   private fun TimeoutLock.toUiState(): TimeoutUiState = TimeoutUiState(
121:     sessionId = sessionId,
122:     secondsRemaining = secondsRemaining,
123:     isLocked = isLocked,
124:     unlocksAt = unlocksAt
125:   )
126: }
127: 
128: @Composable
129: fun TimeoutScreen(
130:   onNavigate: (String) -> Unit = {},
131:   viewModel: TimeoutViewModel = hiltViewModel()
132: ) {
133:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
134: 
135:   AppScreen(
136:     title = "Structured timeout",
137:     subtitle = "A hard pause for your body first, problem-solving later.",
138:     showBottomNav = true,
139:     selectedBottomRoute = null,
140:     onNavigate = onNavigate
141:   ) {
142:     BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.52f)) {
143:       Column(
144:         modifier = Modifier.fillMaxWidth(),
145:         horizontalAlignment = Alignment.CenterHorizontally,
146:         verticalArrangement = Arrangement.spacedBy(16.dp)
147:       ) {
148:         Box(
149:           modifier = Modifier
150:             .size(82.dp)
151:             .background(BreatheRed.copy(alpha = 0.16f), CircleShape),
152:           contentAlignment = Alignment.Center
153:         ) {
154:           Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, tint = BreatheRed, modifier = Modifier.size(38.dp))
155:         }
156:         Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
157:           Text("CURRENT STATE", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheRed)
158:           Text("Locked: ${if (uiState.isLocked) "Yes" else "No"}", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheInk)
159:         }
160: 
161:         TimeoutRing(secondsRemaining = uiState.secondsRemaining)
162:       }
163:     }
164: 
165:     AdaptiveTwoPane(
166:       first = { paneModifier ->
167:         BreatheCard(modifier = paneModifier, containerColor = BreatheCardSurface) {
168:           Icon(imageVector = Icons.Rounded.DoorFront, contentDescription = null, tint = BreatheAccentStrong)
169:           Text("Re-entry window opens at ${uiState.unlocksAt ?: "--:--"}.", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
170:           Text("Move slowly. There is no rush to return.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = BreatheMutedInk)
171:         }
172:       },
173:       second = { paneModifier ->
174:         BreatheCard(modifier = paneModifier, containerColor = BreatheYellow.copy(alpha = 0.18f)) {
175:           Icon(imageVector = Icons.Rounded.SelfImprovement, contentDescription = null, tint = Color(0xFF775A00))
176:           Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
177:             Text("Focus on the exhale. Let your heart rate find its floor.", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
178:             Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
179:               Text(
180:                 text = "Breathing pace",
181:                 style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
182:                 color = Color(0xFF775A00)
183:               )
184:               Box(
185:                 modifier = Modifier
186:                   .fillMaxWidth()
187:                   .background(BreatheCanvas, RoundedCornerShape(999.dp))
188:                   .padding(1.dp)
189:               ) {
190:                 Box(
191:                   modifier = Modifier
192:                     .fillMaxWidth(0.34f)
193:                     .background(Color(0xFF775A00), RoundedCornerShape(999.dp))
194:                     .padding(vertical = 3.dp)
195:                 )
196:               }
197:             }
198:           }
199:         }
200:       }
201:     )
202: 
203:     BreatheCard(containerColor = BreatheCardSurface) {
204:       Icon(imageVector = Icons.Rounded.Security, contentDescription = null, tint = BreatheAccentStrong)
205:       Text("Why we pause", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
206:       Text(
207:         "In high-arousal moments, the part of our brain responsible for connection and logic goes offline. This structured timeout is not a punishment, but a sanctuary for your nervous system to regulate.",
208:         style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
209:         color = BreatheMutedInk
210:       )
211:       Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
212:         Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = BreatheAccentStrong)
213:         Text(
214:           text = "Safe space for both partners",
215:           modifier = Modifier.weight(1f),
216:           style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
217:           color = BreatheAccentStrong
218:         )
219:       }
220:     }
221: 
222:     Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
223:       PrimaryActionButton(
224:         text = "Start timeout",
225:         onClick = { viewModel.onEvent(TimeoutUiEvent.StartTimeout) },
226:         enabled = !uiState.isLocked
227:       )
228:       Text(
229:         text = if (uiState.isLocked) "Timeout already in progress" else "The re-entry window is open.",
230:         style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
231:         color = BreatheMutedInk
232:       )
233:     }
234:   }
235: }
236: 
237: @Composable
238: private fun TimeoutRing(secondsRemaining: Int) {
239:   val progress = (secondsRemaining.coerceAtLeast(0) / 1_200f).coerceIn(0f, 1f)
240: 
241:   BoxWithConstraints(contentAlignment = Alignment.Center) {
242:     val ringSize = maxWidth.coerceAtMost(184.dp)
243: 
244:     CircularProgressIndicator(
245:       progress = { 1f },
246:       modifier = Modifier.size(ringSize),
247:       color = BreatheBorder.copy(alpha = 0.38f),
248:       strokeWidth = 10.dp
249:     )
250:     CircularProgressIndicator(
251:       progress = { progress },
252:       modifier = Modifier.size(ringSize),
253:       color = BreatheRed,
254:       strokeWidth = 10.dp
255:     )
256:     Column(horizontalAlignment = Alignment.CenterHorizontally) {
257:       Text(
258:         text = formatTimeout(secondsRemaining),
259:         style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
260:         color = BreatheInk
261:       )
262:       Text(
263:         text = "REMAINING",
264:         style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
265:         color = BreatheMutedInk
266:       )
267:     }
268:   }
269: }
270: 
271: private fun formatTimeout(seconds: Int): String {
272:   val safeSeconds = seconds.coerceAtLeast(0)
273:   val minutes = safeSeconds / 60
274:   val remainder = safeSeconds % 60
275:   return "%02d:%02d".format(minutes, remainder)
276: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/status/StatusScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.status
2: 
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.border
5: import androidx.compose.foundation.clickable
6: import androidx.compose.foundation.layout.Arrangement
7: import androidx.compose.foundation.layout.Box
8: import androidx.compose.foundation.layout.Column
9: import androidx.compose.foundation.layout.Row
10: import androidx.compose.foundation.layout.fillMaxWidth
11: import androidx.compose.foundation.layout.heightIn
12: import androidx.compose.foundation.layout.padding
13: import androidx.compose.foundation.layout.size
14: import androidx.compose.foundation.shape.CircleShape
15: import androidx.compose.foundation.shape.RoundedCornerShape
16: import androidx.compose.material.icons.Icons
17: import androidx.compose.material.icons.rounded.Favorite
18: import androidx.compose.material.icons.rounded.Lightbulb
19: import androidx.compose.material.icons.rounded.Spa
20: import androidx.compose.material.icons.rounded.Timer
21: import androidx.compose.material3.Icon
22: import androidx.compose.material3.Text
23: import androidx.compose.runtime.Composable
24: import androidx.compose.runtime.LaunchedEffect
25: import androidx.compose.runtime.getValue
26: import androidx.compose.runtime.mutableStateOf
27: import androidx.compose.runtime.remember
28: import androidx.compose.runtime.setValue
29: import androidx.compose.ui.Alignment
30: import androidx.compose.ui.Modifier
31: import androidx.compose.ui.graphics.Color
32: import androidx.compose.ui.graphics.vector.ImageVector
33: import androidx.compose.ui.text.font.FontWeight
34: import androidx.compose.ui.text.style.TextAlign
35: import androidx.compose.ui.text.style.TextOverflow
36: import androidx.compose.ui.unit.dp
37: import androidx.hilt.navigation.compose.hiltViewModel
38: import androidx.lifecycle.ViewModel
39: import androidx.lifecycle.compose.collectAsStateWithLifecycle
40: import androidx.lifecycle.viewModelScope
41: import com.breathe.domain.model.StatusLevel
42: import com.breathe.domain.repository.StatusRepository
43: import com.breathe.domain.usecase.SetStatusUseCase
44: import com.breathe.presentation.navigation.Screen
45: import com.breathe.presentation.theme.BreatheAccentStrong
46: import com.breathe.presentation.theme.BreatheBorder
47: import com.breathe.presentation.theme.BreatheCanvas
48: import com.breathe.presentation.theme.BreatheCardSurface
49: import com.breathe.presentation.theme.BreatheGreen
50: import com.breathe.presentation.theme.BreatheInk
51: import com.breathe.presentation.theme.BreatheMutedInk
52: import com.breathe.presentation.theme.BreatheOverlay
53: import com.breathe.presentation.theme.BreatheRed
54: import com.breathe.presentation.theme.BreatheYellow
55: import com.breathe.presentation.ui.common.AdaptiveTwoPane
56: import com.breathe.presentation.ui.common.AppScreen
57: import com.breathe.presentation.ui.common.BreatheCard
58: import com.breathe.presentation.ui.common.PrimaryActionButton
59: import dagger.hilt.android.lifecycle.HiltViewModel
60: import javax.inject.Inject
61: import kotlinx.coroutines.flow.SharingStarted
62: import kotlinx.coroutines.flow.StateFlow
63: import kotlinx.coroutines.flow.combine
64: import kotlinx.coroutines.flow.stateIn
65: import kotlinx.coroutines.launch
66: 
67: data class StatusUiState(
68:   val selectedStatus: StatusLevel? = null,
69:   val partnerStatus: StatusLevel? = null,
70:   val wsConnected: Boolean = false
71: )
72: 
73: sealed interface StatusUiEvent {
74:   data class SelectStatus(val status: StatusLevel) : StatusUiEvent
75: }
76: 
77: @HiltViewModel
78: class StatusViewModel @Inject constructor(
79:   statusRepository: StatusRepository,
80:   private val setStatusUseCase: SetStatusUseCase
81: ) : ViewModel() {
82:   val uiState: StateFlow<StatusUiState> = combine(
83:     statusRepository.observeOwnStatus(),
84:     statusRepository.observePartnerStatus(),
85:     statusRepository.observeWsConnection()
86:   ) { own, partner, connected ->
87:     StatusUiState(selectedStatus = own, partnerStatus = partner, wsConnected = connected)
88:   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatusUiState())
89: 
90:   fun onEvent(event: StatusUiEvent) {
91:     when (event) {
92:       is StatusUiEvent.SelectStatus -> viewModelScope.launch { setStatusUseCase(event.status) }
93:     }
94:   }
95: }
96: 
97: @Composable
98: fun StatusScreen(
99:   onNavigate: (String) -> Unit = {},
100:   viewModel: StatusViewModel = hiltViewModel()
101: ) {
102:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
103:   var draftStatus by remember { mutableStateOf(uiState.selectedStatus) }
104: 
105:   LaunchedEffect(uiState.selectedStatus) {
106:     if (draftStatus == null || draftStatus == uiState.selectedStatus) {
107:       draftStatus = uiState.selectedStatus
108:     }
109:   }
110: 
111:   AppScreen(
112:     title = "Status check-in",
113:     subtitle = "Signal your nervous-system state early so the next step can fit the moment.",
114:     showBottomNav = true,
115:     selectedBottomRoute = Screen.Status.route,
116:     onNavigate = onNavigate
117:   ) {
118:     AdaptiveTwoPane(
119:       first = { paneModifier ->
120:         StatusAvatarCard(
121:           modifier = paneModifier,
122:           heading = "You",
123:           label = statusPersonaLabel(uiState.selectedStatus),
124:           accent = statusAccent(uiState.selectedStatus),
125:           icon = statusIcon(uiState.selectedStatus)
126:         )
127:       },
128:       second = { paneModifier ->
129:         StatusAvatarCard(
130:           modifier = paneModifier,
131:           heading = "Partner",
132:           label = statusPersonaLabel(uiState.partnerStatus),
133:           accent = statusAccent(uiState.partnerStatus),
134:           icon = statusIcon(uiState.partnerStatus)
135:         )
136:       }
137:     )
138: 
139:     Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
140:       Text(
141:         text = "How are you feeling right now?",
142:         style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
143:         color = BreatheInk
144:       )
145: 
146:       StatusOptionCard(
147:         title = "Green: Safe & Open",
148:         body = "I feel regulated and ready for connection.",
149:         accent = BreatheGreen,
150:         selected = draftStatus == StatusLevel.GREEN,
151:         onClick = { draftStatus = StatusLevel.GREEN },
152:         icon = Icons.Rounded.Spa
153:       )
154:       StatusOptionCard(
155:         title = "Yellow: Feeling Stretched",
156:         body = "I'm managing, but approaching my limit.",
157:         accent = BreatheYellow,
158:         selected = draftStatus == StatusLevel.YELLOW,
159:         onClick = { draftStatus = StatusLevel.YELLOW },
160:         icon = Icons.Rounded.Favorite
161:       )
162:       StatusOptionCard(
163:         title = "Red: Dysregulated / Shutting Down",
164:         body = "I need to stop and regulate before continuing.",
165:         accent = BreatheRed,
166:         selected = draftStatus == StatusLevel.RED,
167:         onClick = { draftStatus = StatusLevel.RED },
168:         icon = Icons.Rounded.Timer
169:       )
170:     }
171: 
172:     SelectionNudgeCard(status = draftStatus)
173: 
174:     PrimaryActionButton(
175:       text = "Update Status",
176:       onClick = { draftStatus?.let { viewModel.onEvent(StatusUiEvent.SelectStatus(it)) } },
177:       enabled = draftStatus != null && draftStatus != uiState.selectedStatus
178:     )
179:   }
180: }
181: 
182: @Composable
183: private fun StatusAvatarCard(
184:   heading: String,
185:   label: String,
186:   accent: Color,
187:   icon: ImageVector,
188:   modifier: Modifier = Modifier
189: ) {
190:   BreatheCard(modifier = modifier.heightIn(min = 188.dp), containerColor = BreatheOverlay.copy(alpha = 0.52f)) {
191:     Column(
192:       modifier = Modifier.fillMaxWidth(),
193:       horizontalAlignment = Alignment.CenterHorizontally,
194:       verticalArrangement = Arrangement.spacedBy(16.dp)
195:     ) {
196:       Box(
197:         modifier = Modifier
198:           .size(64.dp)
199:           .background(BreatheCardSurface, CircleShape)
200:           .border(4.dp, accent.copy(alpha = 0.22f), CircleShape),
201:         contentAlignment = Alignment.Center
202:       ) {
203:         Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
204:       }
205:       Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
206:         Text(
207:           text = heading.uppercase(),
208:           style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
209:           color = accent,
210:           fontWeight = FontWeight.SemiBold
211:         )
212:         Text(
213:           text = label,
214:           style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
215:           color = BreatheInk,
216:           textAlign = TextAlign.Center,
217:           maxLines = 2,
218:           overflow = TextOverflow.Ellipsis
219:         )
220:       }
221:     }
222:   }
223: }
224: 
225: @Composable
226: private fun StatusOptionCard(
227:   title: String,
228:   body: String,
229:   accent: Color,
230:   selected: Boolean,
231:   onClick: () -> Unit,
232:   icon: ImageVector
233: ) {
234:   Row(
235:     modifier = Modifier
236:       .fillMaxWidth()
237:       .background(
238:         if (selected) accent.copy(alpha = 0.16f) else BreatheOverlay.copy(alpha = 0.45f),
239:         RoundedCornerShape(24.dp)
240:       )
241:       .border(
242:         if (selected) 2.dp else 1.dp,
243:         if (selected) accent else BreatheBorder.copy(alpha = 0.18f),
244:         RoundedCornerShape(24.dp)
245:       )
246:       .heightIn(min = 112.dp)
247:       .clickable(onClick = onClick)
248:       .padding(horizontal = 16.dp, vertical = 16.dp),
249:     horizontalArrangement = Arrangement.spacedBy(16.dp),
250:     verticalAlignment = Alignment.Top
251:   ) {
252:     Row(
253:       modifier = Modifier.weight(1f),
254:       horizontalArrangement = Arrangement.spacedBy(16.dp),
255:       verticalAlignment = Alignment.Top
256:     ) {
257:       Box(
258:         modifier = Modifier
259:           .size(48.dp)
260:           .background(accent.copy(alpha = 0.18f), CircleShape),
261:         contentAlignment = Alignment.Center
262:       ) {
263:         Icon(imageVector = icon, contentDescription = null, tint = accent)
264:       }
265:       Column(
266:         modifier = Modifier.weight(1f),
267:         verticalArrangement = Arrangement.spacedBy(4.dp)
268:       ) {
269:         Text(
270:           title,
271:           style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
272:           color = accent,
273:           maxLines = 2,
274:           overflow = TextOverflow.Ellipsis
275:         )
276:         Text(
277:           text = body,
278:           style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
279:           color = BreatheMutedInk,
280:           maxLines = 3,
281:           overflow = TextOverflow.Ellipsis
282:         )
283:       }
284:     }
285: 
286:     Icon(
287:       imageVector = Icons.Rounded.Favorite,
288:       contentDescription = null,
289:       tint = if (selected) accent else BreatheBorder,
290:       modifier = Modifier.padding(top = 2.dp)
291:     )
292:   }
293: }
294: 
295: @Composable
296: private fun SelectionNudgeCard(status: StatusLevel?) {
297:   val (accent, title, body) = when (status) {
298:     StatusLevel.GREEN -> Triple(
299:       BreatheGreen,
300:       "Choosing Green...",
301:       "You feel open enough to stay in contact. This is a good moment for warmth, simplicity, and clear repair."
302:     )
303:     StatusLevel.YELLOW -> Triple(
304:       BreatheYellow,
305:       "Choosing Yellow...",
306:       "This is a signal to slow down. Keep interactions gentle and avoid heavier topics until both bodies settle."
307:     )
308:     StatusLevel.RED -> Triple(
309:       BreatheRed,
310:       "Choosing Red...",
311:       "This means stop pushing content. Shift into Calm or Timeout so the next interaction can be safer and softer."
312:     )
313:     null -> Triple(
314:       BreatheAccentStrong,
315:       "Choosing a state...",
316:       "A status is information, not failure. Pick the closest one so the next step can fit the moment."
317:     )
318:   }
319: 
320:   BreatheCard(containerColor = accent.copy(alpha = 0.12f)) {
321:     Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
322:       Box(
323:         modifier = Modifier
324:           .padding(top = 2.dp)
325:           .size(40.dp)
326:           .background(accent.copy(alpha = 0.14f), CircleShape),
327:         contentAlignment = Alignment.Center
328:       ) {
329:         Icon(imageVector = Icons.Rounded.Lightbulb, contentDescription = null, tint = accent)
330:       }
331:       Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
332:         Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheInk)
333:         Text(body, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
334:       }
335:     }
336:   }
337: }
338: 
339: private fun statusPersonaLabel(status: StatusLevel?): String = when (status) {
340:   StatusLevel.GREEN -> "Grounded"
341:   StatusLevel.YELLOW -> "Stretched"
342:   StatusLevel.RED -> "Flooded"
343:   null -> "Checking in"
344: }
345: 
346: private fun statusAccent(status: StatusLevel?): Color = when (status) {
347:   StatusLevel.GREEN -> BreatheGreen
348:   StatusLevel.YELLOW -> BreatheYellow
349:   StatusLevel.RED -> BreatheRed
350:   null -> BreatheAccentStrong
351: }
352: 
353: private fun statusIcon(status: StatusLevel?): ImageVector = when (status) {
354:   StatusLevel.GREEN -> Icons.Rounded.Spa
355:   StatusLevel.YELLOW -> Icons.Rounded.Favorite
356:   StatusLevel.RED -> Icons.Rounded.Timer
357:   null -> Icons.Rounded.Favorite
358: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/updates/QuickUpdatesScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.updates
2: 
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.border
5: import androidx.compose.foundation.clickable
6: import androidx.compose.foundation.layout.Arrangement
7: import androidx.compose.foundation.layout.Box
8: import androidx.compose.foundation.layout.Column
9: import androidx.compose.foundation.layout.Row
10: import androidx.compose.foundation.layout.fillMaxWidth
11: import androidx.compose.foundation.layout.heightIn
12: import androidx.compose.foundation.layout.padding
13: import androidx.compose.foundation.layout.size
14: import androidx.compose.foundation.shape.CircleShape
15: import androidx.compose.foundation.shape.RoundedCornerShape
16: import androidx.compose.material.icons.Icons
17: import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
18: import androidx.compose.material.icons.automirrored.rounded.Send
19: import androidx.compose.material.icons.rounded.Bedtime
20: import androidx.compose.material.icons.rounded.Favorite
21: import androidx.compose.material.icons.rounded.Home
22: import androidx.compose.material.icons.rounded.LaptopMac
23: import androidx.compose.material.icons.rounded.Restaurant
24: import androidx.compose.material.icons.rounded.SelfImprovement
25: import androidx.compose.material3.Icon
26: import androidx.compose.material3.OutlinedTextField
27: import androidx.compose.material3.Text
28: import androidx.compose.runtime.Composable
29: import androidx.compose.runtime.getValue
30: import androidx.compose.ui.Alignment
31: import androidx.compose.ui.Modifier
32: import androidx.compose.ui.graphics.Color
33: import androidx.compose.ui.graphics.vector.ImageVector
34: import androidx.compose.ui.text.font.FontStyle
35: import androidx.compose.ui.text.style.TextOverflow
36: import androidx.compose.ui.unit.dp
37: import androidx.hilt.navigation.compose.hiltViewModel
38: import androidx.lifecycle.ViewModel
39: import androidx.lifecycle.compose.collectAsStateWithLifecycle
40: import androidx.lifecycle.viewModelScope
41: import com.breathe.domain.model.QuickUpdate
42: import com.breathe.domain.usecase.GetQuickUpdatesUseCase
43: import com.breathe.domain.usecase.SendQuickUpdateUseCase
44: import com.breathe.presentation.navigation.Screen
45: import com.breathe.presentation.theme.BreatheAccentStrong
46: import com.breathe.presentation.theme.BreatheBorder
47: import com.breathe.presentation.theme.BreatheCanvas
48: import com.breathe.presentation.theme.BreatheCardSurface
49: import com.breathe.presentation.theme.BreatheInk
50: import com.breathe.presentation.theme.BreatheMutedInk
51: import com.breathe.presentation.theme.BreatheOverlay
52: import com.breathe.presentation.theme.BreatheYellow
53: import com.breathe.presentation.ui.common.AdaptiveTwoPane
54: import com.breathe.presentation.ui.common.AppScreen
55: import com.breathe.presentation.ui.common.BreatheCard
56: import com.breathe.presentation.ui.common.PrimaryActionButton
57: import dagger.hilt.android.lifecycle.HiltViewModel
58: import java.time.Duration
59: import java.time.Instant
60: import javax.inject.Inject
61: import kotlinx.coroutines.flow.MutableStateFlow
62: import kotlinx.coroutines.flow.SharingStarted
63: import kotlinx.coroutines.flow.StateFlow
64: import kotlinx.coroutines.flow.asStateFlow
65: import kotlinx.coroutines.flow.combine
66: import kotlinx.coroutines.flow.stateIn
67: import kotlinx.coroutines.flow.update
68: import kotlinx.coroutines.launch
69: 
70: data class QuickUpdatePreset(
71:   val key: String,
72:   val label: String,
73:   val icon: ImageVector
74: )
75: 
76: private val quickUpdatePresets = listOf(
77:   QuickUpdatePreset("on_my_way", "On my way", Icons.AutoMirrored.Rounded.DirectionsRun),
78:   QuickUpdatePreset("eating_now", "Eating now", Icons.Rounded.Restaurant),
79:   QuickUpdatePreset("at_work", "At work", Icons.Rounded.LaptopMac),
80:   QuickUpdatePreset("home_safe", "Home safe", Icons.Rounded.Home),
81:   QuickUpdatePreset("quiet_time", "Quiet time", Icons.Rounded.SelfImprovement),
82:   QuickUpdatePreset("resting", "Resting", Icons.Rounded.Bedtime)
83: )
84: 
85: data class QuickUpdatesUiState(
86:   val selectedPresetKey: String = quickUpdatePresets.first().key,
87:   val note: String = "",
88:   val entries: List<QuickUpdate> = emptyList(),
89:   val isSending: Boolean = false
90: ) {
91:   val selectedPreset: QuickUpdatePreset
92:     get() = quickUpdatePresets.firstOrNull { it.key == selectedPresetKey } ?: quickUpdatePresets.first()
93: }
94: 
95: sealed interface QuickUpdatesUiEvent {
96:   data class SelectPreset(val presetKey: String) : QuickUpdatesUiEvent
97:   data class NoteChanged(val value: String) : QuickUpdatesUiEvent
98:   data object Send : QuickUpdatesUiEvent
99:   data object Refresh : QuickUpdatesUiEvent
100: }
101: 
102: @HiltViewModel
103: class QuickUpdatesViewModel @Inject constructor(
104:   private val getQuickUpdatesUseCase: GetQuickUpdatesUseCase,
105:   private val sendQuickUpdateUseCase: SendQuickUpdateUseCase
106: ) : ViewModel() {
107:   private val localState = MutableStateFlow(QuickUpdatesUiState())
108:   val uiState: StateFlow<QuickUpdatesUiState> = combine(
109:     localState,
110:     getQuickUpdatesUseCase()
111:   ) { local, entries ->
112:     local.copy(entries = entries)
113:   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuickUpdatesUiState())
114: 
115:   init {
116:     onEvent(QuickUpdatesUiEvent.Refresh)
117:   }
118: 
119:   fun onEvent(event: QuickUpdatesUiEvent) {
120:     when (event) {
121:       is QuickUpdatesUiEvent.SelectPreset -> localState.update { it.copy(selectedPresetKey = event.presetKey) }
122:       is QuickUpdatesUiEvent.NoteChanged -> localState.update { it.copy(note = event.value.take(160)) }
123:       QuickUpdatesUiEvent.Refresh -> viewModelScope.launch { getQuickUpdatesUseCase.refresh() }
124:       QuickUpdatesUiEvent.Send -> sendSelectedUpdate()
125:     }
126:   }
127: 
128:   private fun sendSelectedUpdate() {
129:     val snapshot = localState.value
130:     val preset = snapshot.selectedPreset
131: 
132:     viewModelScope.launch {
133:       localState.update { it.copy(isSending = true) }
134:       runCatching {
135:         sendQuickUpdateUseCase(
136:           presetKey = preset.key,
137:           message = preset.label,
138:           note = snapshot.note.trim().takeIf { it.isNotEmpty() }
139:         )
140:       }
141:       localState.update { it.copy(isSending = false, note = "") }
142:     }
143:   }
144: }
145: 
146: @Composable
147: fun QuickUpdatesScreen(
148:   onNavigate: (String) -> Unit = {},
149:   viewModel: QuickUpdatesViewModel = hiltViewModel()
150: ) {
151:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
152:   val latest = uiState.entries.firstOrNull()
153: 
154:   AppScreen(
155:     title = "Quick Updates",
156:     subtitle = "Let your partner know where you are or how you're feeling with a gentle touch.",
157:     showBottomNav = true,
158:     selectedBottomRoute = null,
159:     onNavigate = onNavigate
160:   ) {
161:     AdaptiveTwoPane(
162:       first = { paneModifier ->
163:         BreatheCard(modifier = paneModifier, containerColor = BreatheOverlay.copy(alpha = 0.56f)) {
164:           Text("LAST SHARED", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheAccentStrong.copy(alpha = 0.6f))
165:           Text(
166:             text = latest?.let {
167:               buildString {
168:                 append('"')
169:                 append(if (!it.note.isNullOrBlank()) it.note else it.message)
170:                 append('"')
171:               }
172:             } ?: "\"No gentle update yet.\"",
173:             style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
174:             color = BreatheInk,
175:             fontStyle = FontStyle.Italic
176:           )
177:           Text(
178:             text = latest?.createdAt?.let(::relativeTime) ?: "just now",
179:             style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
180:             color = BreatheMutedInk
181:           )
182:         }
183:       },
184:       second = { paneModifier ->
185:         BreatheCard(modifier = paneModifier, containerColor = BreatheAccentStrong.copy(alpha = 0.12f)) {
186:           Column(
187:             modifier = Modifier.fillMaxWidth(),
188:             horizontalAlignment = Alignment.CenterHorizontally,
189:             verticalArrangement = Arrangement.spacedBy(10.dp)
190:           ) {
191:             Box(
192:               modifier = Modifier
193:                 .size(54.dp)
194:                 .background(BreatheCardSurface, CircleShape),
195:               contentAlignment = Alignment.Center
196:             ) {
197:               Icon(imageVector = Icons.Rounded.Favorite, contentDescription = null, tint = BreatheAccentStrong, modifier = Modifier.size(28.dp))
198:             }
199:             Text("Connected", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheAccentStrong)
200:           }
201:         }
202:       }
203:     )
204: 
205:     Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
206:       Text(
207:         text = "CHOOSE A RITUAL UPDATE",
208:         style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
209:         color = BreatheAccentStrong.copy(alpha = 0.6f)
210:       )
211: 
212:       quickUpdatePresets.chunked(2).forEach { rowPresets ->
213:         AdaptiveTwoPane(
214:           first = { paneModifier ->
215:             val preset = rowPresets[0]
216:             PresetCard(
217:               preset = preset,
218:               selected = uiState.selectedPresetKey == preset.key,
219:               onClick = { viewModel.onEvent(QuickUpdatesUiEvent.SelectPreset(preset.key)) },
220:               modifier = paneModifier
221:             )
222:           },
223:           second = { paneModifier ->
224:             val preset = rowPresets[1]
225:             PresetCard(
226:               preset = preset,
227:               selected = uiState.selectedPresetKey == preset.key,
228:               onClick = { viewModel.onEvent(QuickUpdatesUiEvent.SelectPreset(preset.key)) },
229:               modifier = paneModifier
230:             )
231:           }
232:         )
233:       }
234:     }
235: 
236:     BreatheCard(containerColor = BreatheCardSurface.copy(alpha = 0.88f)) {
237:       Text("OR SHARE SOMETHING UNIQUE", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheAccentStrong.copy(alpha = 0.6f))
238:       OutlinedTextField(
239:         modifier = Modifier.fillMaxWidth(),
240:         value = uiState.note,
241:         onValueChange = { viewModel.onEvent(QuickUpdatesUiEvent.NoteChanged(it)) },
242:         label = { Text("Write a short note") },
243:         placeholder = { Text("Taking a deep breath before the meeting.") },
244:         shape = RoundedCornerShape(999.dp)
245:       )
246:       PrimaryActionButton(
247:         text = if (uiState.isSending) "Sending..." else "Send Update",
248:         onClick = { viewModel.onEvent(QuickUpdatesUiEvent.Send) },
249:         enabled = !uiState.isSending,
250:         icon = Icons.AutoMirrored.Rounded.Send
251:       )
252:     }
253: 
254:     BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.18f)) {
255:       Text(
256:         text = "\"Communication is the bridge between two hearts.\"",
257:         style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
258:         color = BreatheInk,
259:         fontStyle = FontStyle.Italic
260:       )
261:     }
262:   }
263: }
264: 
265: @Composable
266: private fun PresetCard(
267:   preset: QuickUpdatePreset,
268:   selected: Boolean,
269:   onClick: () -> Unit,
270:   modifier: Modifier = Modifier
271: ) {
272:   Column(
273:     modifier = modifier
274:       .background(
275:         if (selected) BreatheAccentStrong.copy(alpha = 0.14f) else BreatheOverlay.copy(alpha = 0.56f),
276:         RoundedCornerShape(22.dp)
277:       )
278:       .border(1.dp, if (selected) BreatheAccentStrong else BreatheBorder.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
279:       .heightIn(min = 116.dp)
280:       .clickable(onClick = onClick)
281:       .padding(16.dp),
282:     verticalArrangement = Arrangement.Center
283:   ) {
284:     Icon(imageVector = preset.icon, contentDescription = null, tint = BreatheAccentStrong)
285:     androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
286:     Text(
287:       text = preset.label,
288:       style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
289:       color = BreatheInk,
290:       maxLines = 2,
291:       overflow = TextOverflow.Ellipsis
292:     )
293:   }
294: }
295: 
296: private fun relativeTime(iso: String): String {
297:   val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return "just now"
298:   val minutes = Duration.between(instant, Instant.now()).toMinutes().coerceAtLeast(0)
299:   return when {
300:     minutes < 1 -> "just now"
301:     minutes < 60 -> "$minutes minutes ago"
302:     else -> "${minutes / 60} hours ago"
303:   }
304: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/voice/VoiceStudioScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.voice
2: 
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.border
5: import androidx.compose.foundation.clickable
6: import androidx.compose.foundation.layout.Arrangement
7: import androidx.compose.foundation.layout.Box
8: import androidx.compose.foundation.layout.BoxWithConstraints
9: import androidx.compose.foundation.layout.Column
10: import androidx.compose.foundation.layout.Row
11: import androidx.compose.foundation.layout.fillMaxWidth
12: import androidx.compose.foundation.layout.heightIn
13: import androidx.compose.foundation.layout.padding
14: import androidx.compose.foundation.layout.size
15: import androidx.compose.foundation.shape.CircleShape
16: import androidx.compose.foundation.shape.RoundedCornerShape
17: import androidx.compose.material.icons.Icons
18: import androidx.compose.material.icons.rounded.GraphicEq
19: import androidx.compose.material.icons.rounded.Mic
20: import androidx.compose.material.icons.rounded.RadioButtonChecked
21: import androidx.compose.material.icons.rounded.Stop
22: import androidx.compose.material3.Icon
23: import androidx.compose.material3.Text
24: import androidx.compose.runtime.Composable
25: import androidx.compose.runtime.getValue
26: import androidx.compose.ui.Alignment
27: import androidx.compose.ui.Modifier
28: import androidx.compose.ui.graphics.Brush
29: import androidx.compose.ui.text.style.TextOverflow
30: import androidx.compose.ui.unit.dp
31: import androidx.hilt.navigation.compose.hiltViewModel
32: import androidx.lifecycle.ViewModel
33: import androidx.lifecycle.compose.collectAsStateWithLifecycle
34: import androidx.lifecycle.viewModelScope
35: import com.breathe.domain.model.VoicePrompt
36: import com.breathe.domain.repository.VoiceRepository
37: import com.breathe.presentation.navigation.Screen
38: import com.breathe.presentation.theme.BreatheAccent
39: import com.breathe.presentation.theme.BreatheAccentStrong
40: import com.breathe.presentation.theme.BreatheBorder
41: import com.breathe.presentation.theme.BreatheCardSurface
42: import com.breathe.presentation.theme.BreatheInk
43: import com.breathe.presentation.theme.BreatheMutedInk
44: import com.breathe.presentation.theme.BreatheOverlay
45: import com.breathe.presentation.theme.BreatheRed
46: import com.breathe.presentation.theme.BreatheYellow
47: import com.breathe.presentation.ui.common.AdaptiveTwoPane
48: import com.breathe.presentation.ui.common.AppScreen
49: import com.breathe.presentation.ui.common.BreatheCard
50: import com.breathe.presentation.ui.common.PrimaryActionButton
51: import dagger.hilt.android.lifecycle.HiltViewModel
52: import javax.inject.Inject
53: import kotlinx.coroutines.flow.MutableStateFlow
54: import kotlinx.coroutines.flow.SharingStarted
55: import kotlinx.coroutines.flow.StateFlow
56: import kotlinx.coroutines.flow.combine
57: import kotlinx.coroutines.flow.stateIn
58: import kotlinx.coroutines.flow.update
59: 
60: data class VoiceUiState(
61:   val prompts: List<VoicePrompt> = emptyList(),
62:   val isRecording: Boolean = false,
63:   val currentSlot: Int? = null,
64:   val currentPromptLabel: String? = null
65: )
66: 
67: sealed interface VoiceUiEvent {
68:   data class SelectSlot(val slot: Int) : VoiceUiEvent
69:   data object ToggleRecording : VoiceUiEvent
70: }
71: 
72: @HiltViewModel
73: class VoiceViewModel @Inject constructor(
74:   voiceRepository: VoiceRepository
75: ) : ViewModel() {
76:   private val selectedSlot = MutableStateFlow<Int?>(null)
77:   private val recording = MutableStateFlow(false)
78: 
79:   val uiState: StateFlow<VoiceUiState> = combine(
80:     voiceRepository.observePrompts(),
81:     selectedSlot,
82:     recording
83:   ) { prompts, slot, isRecording ->
84:     val selectedPrompt = prompts.firstOrNull { it.slot == slot }
85:     VoiceUiState(
86:       prompts = prompts,
87:       isRecording = isRecording,
88:       currentSlot = slot,
89:       currentPromptLabel = selectedPrompt?.label
90:     )
91:   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VoiceUiState())
92: 
93:   fun onEvent(event: VoiceUiEvent) {
94:     when (event) {
95:       is VoiceUiEvent.SelectSlot -> {
96:         selectedSlot.update { event.slot }
97:         recording.update { false }
98:       }
99: 
100:       VoiceUiEvent.ToggleRecording -> {
101:         if (uiState.value.currentSlot != null) {
102:           recording.update { !it }
103:         }
104:       }
105:     }
106:   }
107: }
108: 
109: @Composable
110: fun VoiceStudioScreen(
111:   onNavigate: (String) -> Unit = {},
112:   viewModel: VoiceViewModel = hiltViewModel()
113: ) {
114:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
115: 
116:   AppScreen(
117:     title = "Voice Studio",
118:     subtitle = "Prepare warm, regulating phrases while calm so your future self does not have to invent them in the middle of stress.",
119:     showBottomNav = true,
120:     selectedBottomRoute = null,
121:     onNavigate = onNavigate
122:   ) {
123:     BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.52f)) {
124:       Column(
125:         modifier = Modifier.fillMaxWidth(),
126:         horizontalAlignment = Alignment.CenterHorizontally,
127:         verticalArrangement = Arrangement.spacedBy(18.dp)
128:       ) {
129:         BoxWithConstraints(
130:           modifier = Modifier.fillMaxWidth(),
131:           contentAlignment = Alignment.Center
132:         ) {
133:           val outerSize = maxWidth.coerceAtMost(188.dp)
134:           val innerSize = outerSize * 0.82f
135: 
136:           Box(
137:             modifier = Modifier
138:               .size(outerSize)
139:               .background(BreatheAccent.copy(alpha = 0.08f), CircleShape),
140:             contentAlignment = Alignment.Center
141:           ) {
142:             Box(
143:               modifier = Modifier
144:                 .size(innerSize)
145:                 .background(
146:                   brush = Brush.linearGradient(
147:                     colors = if (uiState.isRecording) {
148:                       listOf(BreatheRed.copy(alpha = 0.25f), BreatheCardSurface, BreatheYellow.copy(alpha = 0.18f))
149:                     } else {
150:                       listOf(BreatheCardSurface, BreatheAccent.copy(alpha = 0.18f), BreatheOverlay)
151:                     }
152:                   ),
153:                   shape = CircleShape
154:                 )
155:                 .border(1.dp, BreatheBorder.copy(alpha = 0.4f), CircleShape),
156:               contentAlignment = Alignment.Center
157:             ) {
158:               Icon(
159:                 imageVector = if (uiState.isRecording) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
160:                 contentDescription = null,
161:                 tint = if (uiState.isRecording) BreatheRed else BreatheAccentStrong,
162:                 modifier = Modifier.size(56.dp)
163:               )
164:             }
165:           }
166:         }
167: 
168:         Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
169:           Text(
170:             text = if (uiState.isRecording) "Recording a care phrase" else "Current take",
171:             style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
172:             color = BreatheInk
173:           )
174:           Text(
175:             text = uiState.currentPromptLabel ?: "Choose a slot below to preview the prompt you want to record.",
176:             style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
177:             color = BreatheMutedInk,
178:             maxLines = 3,
179:             overflow = TextOverflow.Ellipsis
180:           )
181:         }
182:       }
183:     }
184: 
185:     AdaptiveTwoPane(
186:       first = { paneModifier ->
187:         VoiceInfoCard(
188:           modifier = paneModifier,
189:           label = "Recording",
190:           value = if (uiState.isRecording) "In progress" else "Idle",
191:           icon = if (uiState.isRecording) Icons.Rounded.GraphicEq else Icons.Rounded.RadioButtonChecked
192:         )
193:       },
194:       second = { paneModifier ->
195:         VoiceInfoCard(
196:           modifier = paneModifier,
197:           label = "Slot",
198:           value = uiState.currentSlot?.toString() ?: "None",
199:           icon = Icons.Rounded.Mic
200:         )
201:       }
202:     )
203: 
204:     Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
205:       Text(
206:         text = "Prompt slots",
207:         style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
208:         color = BreatheAccentStrong
209:       )
210:       uiState.prompts.forEach { prompt ->
211:         VoicePromptCard(
212:           prompt = prompt,
213:           selected = uiState.currentSlot == prompt.slot,
214:           onClick = { viewModel.onEvent(VoiceUiEvent.SelectSlot(prompt.slot)) }
215:         )
216:       }
217:     }
218: 
219:     PrimaryActionButton(
220:       text = if (uiState.isRecording) "Stop recording" else "Start recording",
221:       onClick = { viewModel.onEvent(VoiceUiEvent.ToggleRecording) },
222:       enabled = uiState.currentSlot != null,
223:       icon = if (uiState.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic
224:     )
225:   }
226: }
227: 
228: @Composable
229: private fun VoiceInfoCard(
230:   label: String,
231:   value: String,
232:   icon: androidx.compose.ui.graphics.vector.ImageVector,
233:   modifier: Modifier = Modifier
234: ) {
235:   BreatheCard(modifier = modifier.heightIn(min = 96.dp), containerColor = BreatheCardSurface) {
236:     Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
237:       Box(
238:         modifier = Modifier
239:           .size(44.dp)
240:           .background(BreatheOverlay.copy(alpha = 0.72f), CircleShape),
241:         contentAlignment = Alignment.Center
242:       ) {
243:         Icon(imageVector = icon, contentDescription = null, tint = BreatheAccentStrong)
244:       }
245:       Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
246:         Text(label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
247:         Text(
248:           text = value,
249:           style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
250:           color = BreatheInk,
251:           maxLines = 2,
252:           overflow = TextOverflow.Ellipsis
253:         )
254:       }
255:     }
256:   }
257: }
258: 
259: @Composable
260: private fun VoicePromptCard(prompt: VoicePrompt, selected: Boolean, onClick: () -> Unit) {
261:   Row(
262:     modifier = Modifier
263:       .fillMaxWidth()
264:       .background(
265:         if (selected) BreatheAccentStrong.copy(alpha = 0.12f) else BreatheOverlay.copy(alpha = 0.48f),
266:         RoundedCornerShape(22.dp)
267:       )
268:       .border(1.dp, if (selected) BreatheAccentStrong else BreatheBorder.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
269:       .heightIn(min = 88.dp)
270:       .clickable(onClick = onClick)
271:       .padding(16.dp),
272:     horizontalArrangement = Arrangement.spacedBy(16.dp),
273:     verticalAlignment = Alignment.CenterVertically
274:   ) {
275:     Column(
276:       modifier = Modifier.weight(1f),
277:       verticalArrangement = Arrangement.spacedBy(4.dp)
278:     ) {
279:       Text("Slot ${prompt.slot}", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
280:       Text(
281:         text = prompt.label,
282:         style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
283:         color = BreatheInk,
284:         maxLines = 2,
285:         overflow = TextOverflow.Ellipsis
286:       )
287:     }
288: 
289:     Box(
290:       modifier = Modifier
291:         .size(38.dp)
292:         .background(if (selected) BreatheAccentStrong.copy(alpha = 0.18f) else BreatheYellow.copy(alpha = 0.12f), CircleShape),
293:       contentAlignment = Alignment.Center
294:     ) {
295:       Icon(imageVector = Icons.Rounded.Mic, contentDescription = null, tint = if (selected) BreatheAccentStrong else BreatheMutedInk)
296:     }
297:   }
298: }
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/log/ConflictLogScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.log
2: 
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.border
5: import androidx.compose.foundation.clickable
6: import androidx.compose.foundation.layout.Arrangement
7: import androidx.compose.foundation.layout.Column
8: import androidx.compose.foundation.layout.Row
9: import androidx.compose.foundation.layout.fillMaxWidth
10: import androidx.compose.foundation.layout.heightIn
11: import androidx.compose.foundation.layout.padding
12: import androidx.compose.foundation.shape.RoundedCornerShape
13: import androidx.compose.material.icons.Icons
14: import androidx.compose.material.icons.rounded.EditNote
15: import androidx.compose.material.icons.rounded.Lock
16: import androidx.compose.material.icons.rounded.SelfImprovement
17: import androidx.compose.material3.Icon
18: import androidx.compose.material3.Text
19: import androidx.compose.runtime.Composable
20: import androidx.compose.runtime.getValue
21: import androidx.compose.ui.Alignment
22: import androidx.compose.ui.Modifier
23: import androidx.compose.ui.graphics.Color
24: import androidx.compose.ui.text.style.TextAlign
25: import androidx.compose.ui.text.style.TextOverflow
26: import androidx.compose.ui.unit.dp
27: import androidx.hilt.navigation.compose.hiltViewModel
28: import androidx.lifecycle.ViewModel
29: import androidx.lifecycle.compose.collectAsStateWithLifecycle
30: import androidx.lifecycle.viewModelScope
31: import com.breathe.domain.model.ConflictLogEntry
32: import com.breathe.domain.model.SessionFeature
33: import com.breathe.domain.repository.SessionRepository
34: import com.breathe.presentation.navigation.Screen
35: import com.breathe.presentation.theme.BreatheAccentStrong
36: import com.breathe.presentation.theme.BreatheBorder
37: import com.breathe.presentation.theme.BreatheCanvas
38: import com.breathe.presentation.theme.BreatheCardSurface
39: import com.breathe.presentation.theme.BreatheInk
40: import com.breathe.presentation.theme.BreatheMutedInk
41: import com.breathe.presentation.theme.BreatheOverlay
42: import com.breathe.presentation.theme.BreatheRed
43: import com.breathe.presentation.theme.BreatheYellow
44: import com.breathe.presentation.ui.common.AdaptiveThreePane
45: import com.breathe.presentation.ui.common.AdaptiveTwoPane
46: import com.breathe.presentation.ui.common.AppScreen
47: import com.breathe.presentation.ui.common.BreatheCard
48: import dagger.hilt.android.lifecycle.HiltViewModel
49: import javax.inject.Inject
50: import kotlinx.coroutines.flow.MutableStateFlow
51: import kotlinx.coroutines.flow.SharingStarted
52: import kotlinx.coroutines.flow.StateFlow
53: import kotlinx.coroutines.flow.combine
54: import kotlinx.coroutines.flow.stateIn
55: import kotlinx.coroutines.flow.update
56: 
57: data class LogUiState(
58:   val entries: List<ConflictLogEntry> = emptyList(),
59:   val selectedEntry: ConflictLogEntry? = null,
60:   val isShared: Boolean = false
61: )
62: 
63: sealed interface LogUiEvent {
64:   data class SelectEntry(val sessionId: Long) : LogUiEvent
65: }
66: 
67: @HiltViewModel
68: class LogViewModel @Inject constructor(
69:   sessionRepository: SessionRepository
70: ) : ViewModel() {
71:   private val selectedSessionId = MutableStateFlow<Long?>(null)
72: 
73:   val uiState: StateFlow<LogUiState> = combine(
74:     sessionRepository.observeConflictLogs(),
75:     selectedSessionId
76:   ) { entries, selectedId ->
77:     val selectedEntry = entries.firstOrNull { it.sessionId == selectedId } ?: entries.firstOrNull()
78:     LogUiState(
79:       entries = entries,
80:       selectedEntry = selectedEntry,
81:       isShared = selectedEntry?.isShared == true
82:     )
83:   }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogUiState())
84: 
85:   fun onEvent(event: LogUiEvent) {
86:     when (event) {
87:       is LogUiEvent.SelectEntry -> selectedSessionId.update { event.sessionId }
88:     }
89:   }
90: }
91: 
92: @Composable
93: fun ConflictLogScreen(
94:   onNavigate: (String) -> Unit = {},
95:   viewModel: LogViewModel = hiltViewModel()
96: ) {
97:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
98: 
99:   AppScreen(
100:     title = "Conflict log",
101:     subtitle = "A private record of repair attempts and the shape each difficult moment took.",
102:     showBottomNav = true,
103:     selectedBottomRoute = Screen.Log.route,
104:     onNavigate = onNavigate
105:   ) {
106:     AdaptiveThreePane(
107:       first = { paneModifier ->
108:         LogSummaryCard(modifier = paneModifier, label = "Entries", value = uiState.entries.size.toString())
109:       },
110:       second = { paneModifier ->
111:         LogSummaryCard(modifier = paneModifier, label = "Selected", value = uiState.selectedEntry?.feature?.let(::featureSummaryLabel) ?: "None")
112:       },
113:       third = { paneModifier ->
114:         LogSummaryCard(modifier = paneModifier, label = "Shared", value = if (uiState.isShared) "Yes" else "No")
115:       }
116:     )
117: 
118:     BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.54f)) {
119:       Text("Selected entry", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
120:       val entry = uiState.selectedEntry
121:       if (entry == null) {
122:         Text("No sessions have been logged yet.", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = BreatheMutedInk)
123:       } else {
124:         Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
125:           Icon(
126:             imageVector = if (entry.feature == SessionFeature.CALM) Icons.Rounded.SelfImprovement else Icons.Rounded.Lock,
127:             contentDescription = null,
128:             tint = featureAccent(entry.feature)
129:           )
130:           Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
131:             Text(featureTitle(entry.feature), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
132:             Text(prettyMoment(entry.startedAt), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = BreatheMutedInk)
133:           }
134:         }
135:         Text(
136:           text = entry.privateNote ?: "No private note saved for this session.",
137:           style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
138:           color = BreatheMutedInk
139:         )
140:         AdaptiveTwoPane(
141:           first = { paneModifier ->
142:             LogMetricChip(modifier = paneModifier, label = "Duration", value = entry.durationSeconds?.let { "$it sec" } ?: "Open")
143:           },
144:           second = { paneModifier ->
145:             LogMetricChip(modifier = paneModifier, label = "Mood shift", value = buildMoodShift(entry))
146:           }
147:         )
148:       }
149:     }
150: 
151:     Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
152:       Text("Entries", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
153:       if (uiState.entries.isEmpty()) {
154:         BreatheCard {
155:           Text(
156:             text = "Your local log is still empty. Calm and Timeout sessions will appear here.",
157:             style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
158:             color = BreatheMutedInk
159:           )
160:         }
161:       } else {
162:         uiState.entries.forEach { entry ->
163:           LogEntryCard(
164:             entry = entry,
165:             selected = uiState.selectedEntry?.sessionId == entry.sessionId,
166:             onClick = { viewModel.onEvent(LogUiEvent.SelectEntry(entry.sessionId)) }
167:           )
168:         }
169:       }
170:     }
171:   }
172: }
173: 
174: @Composable
175: private fun LogSummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
176:   BreatheCard(modifier = modifier.heightIn(min = 104.dp), containerColor = BreatheCardSurface) {
177:     Text(label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
178:     Text(
179:       text = value,
180:       style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
181:       color = BreatheInk,
182:       maxLines = 2,
183:       overflow = TextOverflow.Ellipsis
184:     )
185:   }
186: }
187: 
188: @Composable
189: private fun LogMetricChip(label: String, value: String, modifier: Modifier = Modifier) {
190:   Column(
191:     modifier = modifier
192:       .background(BreatheCardSurface, RoundedCornerShape(18.dp))
193:       .padding(horizontal = 14.dp, vertical = 12.dp)
194:   ) {
195:     Text(label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
196:     Text(
197:       text = value,
198:       style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
199:       color = BreatheInk,
200:       maxLines = 2,
201:       overflow = TextOverflow.Ellipsis
202:     )
203:   }
204: }
205: 
206: @Composable
207: private fun LogEntryCard(entry: ConflictLogEntry, selected: Boolean, onClick: () -> Unit) {
208:   Row(
209:     modifier = Modifier
210:       .fillMaxWidth()
211:       .background(
212:         if (selected) featureAccent(entry.feature).copy(alpha = 0.12f) else BreatheOverlay.copy(alpha = 0.48f),
213:         RoundedCornerShape(22.dp)
214:       )
215:       .border(1.dp, if (selected) featureAccent(entry.feature) else BreatheBorder.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
216:       .heightIn(min = 80.dp)
217:       .clickable(onClick = onClick)
218:       .padding(horizontal = 16.dp, vertical = 16.dp),
219:     horizontalArrangement = Arrangement.spacedBy(16.dp),
220:     verticalAlignment = Alignment.CenterVertically
221:   ) {
222:     Column(
223:       modifier = Modifier.weight(1f),
224:       verticalArrangement = Arrangement.spacedBy(4.dp)
225:     ) {
226:       Text(featureTitle(entry.feature), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheInk)
227:       Text(prettyMoment(entry.startedAt), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = BreatheMutedInk)
228:     }
229:     Text(
230:       text = buildMoodShift(entry),
231:       style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
232:       color = featureAccent(entry.feature),
233:       textAlign = TextAlign.End,
234:       maxLines = 1,
235:       overflow = TextOverflow.Ellipsis
236:     )
237:   }
238: }
239: 
240: private fun buildMoodShift(entry: ConflictLogEntry): String {
241:   val before = entry.moodBefore
242:   val after = entry.moodAfter
243:   return if (before != null && after != null) {
244:     val delta = after - before
245:     if (delta >= 0) "+$delta" else delta.toString()
246:   } else {
247:     "n/a"
248:   }
249: }
250: 
251: private fun featureAccent(feature: SessionFeature): Color = when (feature) {
252:   SessionFeature.CALM -> BreatheAccentStrong
253:   SessionFeature.TIMEOUT -> BreatheRed
254: }
255: 
256: private fun featureTitle(feature: SessionFeature): String = when (feature) {
257:   SessionFeature.CALM -> "Calm session"
258:   SessionFeature.TIMEOUT -> "Structured timeout"
259: }
260: 
261: private fun featureSummaryLabel(feature: SessionFeature): String = when (feature) {
262:   SessionFeature.CALM -> "Calm"
263:   SessionFeature.TIMEOUT -> "Timeout"
264: }
265: 
266: private fun prettyMoment(raw: String): String = raw.replace('T', ' ').removeSuffix("Z")
```

## File: `android/app/src/main/java/com/breathe/presentation/ui/insights/InsightsScreen.kt`

```kotlin
1: package com.breathe.presentation.ui.insights
2: 
3: import androidx.compose.foundation.background
4: import androidx.compose.foundation.layout.Arrangement
5: import androidx.compose.foundation.layout.Column
6: import androidx.compose.foundation.layout.Row
7: import androidx.compose.foundation.layout.fillMaxWidth
8: import androidx.compose.foundation.layout.heightIn
9: import androidx.compose.foundation.layout.padding
10: import androidx.compose.foundation.shape.RoundedCornerShape
11: import androidx.compose.material.icons.Icons
12: import androidx.compose.material.icons.rounded.AutoGraph
13: import androidx.compose.material.icons.rounded.Refresh
14: import androidx.compose.material.icons.rounded.TipsAndUpdates
15: import androidx.compose.material3.Icon
16: import androidx.compose.material3.Text
17: import androidx.compose.runtime.Composable
18: import androidx.compose.runtime.getValue
19: import androidx.compose.ui.Alignment
20: import androidx.compose.ui.Modifier
21: import androidx.compose.ui.text.style.TextAlign
22: import androidx.compose.ui.text.style.TextOverflow
23: import androidx.compose.ui.unit.dp
24: import androidx.hilt.navigation.compose.hiltViewModel
25: import androidx.lifecycle.ViewModel
26: import androidx.lifecycle.compose.collectAsStateWithLifecycle
27: import androidx.lifecycle.viewModelScope
28: import com.breathe.domain.model.MoodTrend
29: import com.breathe.domain.model.WeeklySummary
30: import com.breathe.domain.usecase.GetInsightsUseCase
31: import com.breathe.presentation.navigation.Screen
32: import com.breathe.presentation.theme.BreatheAccentStrong
33: import com.breathe.presentation.theme.BreatheBorder
34: import com.breathe.presentation.theme.BreatheCardSurface
35: import com.breathe.presentation.theme.BreatheInk
36: import com.breathe.presentation.theme.BreatheMutedInk
37: import com.breathe.presentation.theme.BreatheOverlay
38: import com.breathe.presentation.theme.BreatheYellow
39: import com.breathe.presentation.ui.common.AdaptiveTwoPane
40: import com.breathe.presentation.ui.common.AppScreen
41: import com.breathe.presentation.ui.common.BreatheCard
42: import com.breathe.presentation.ui.common.PrimaryActionButton
43: import dagger.hilt.android.lifecycle.HiltViewModel
44: import javax.inject.Inject
45: import kotlinx.coroutines.flow.SharingStarted
46: import kotlinx.coroutines.flow.StateFlow
47: import kotlinx.coroutines.flow.map
48: import kotlinx.coroutines.flow.stateIn
49: import kotlinx.coroutines.launch
50: 
51: data class InsightsUiState(
52:   val weeklySummary: WeeklySummary = WeeklySummary(),
53:   val moodTrend: MoodTrend = MoodTrend(),
54:   val topFeature: String? = null,
55:   val headline: String = "",
56:   val recommendations: List<String> = emptyList()
57: )
58: 
59: sealed interface InsightsUiEvent {
60:   data object Refresh : InsightsUiEvent
61: }
62: 
63: @HiltViewModel
64: class InsightsViewModel @Inject constructor(
65:   private val getInsightsUseCase: GetInsightsUseCase
66: ) : ViewModel() {
67:   val uiState: StateFlow<InsightsUiState> = getInsightsUseCase()
68:     .map {
69:       InsightsUiState(
70:         weeklySummary = it.weeklySummary,
71:         moodTrend = it.moodTrend,
72:         topFeature = it.topFeature,
73:         headline = it.headline,
74:         recommendations = it.recommendations
75:       )
76:     }
77:     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())
78: 
79:   init {
80:     onEvent(InsightsUiEvent.Refresh)
81:   }
82: 
83:   fun onEvent(event: InsightsUiEvent) {
84:     when (event) {
85:       InsightsUiEvent.Refresh -> viewModelScope.launch { getInsightsUseCase.refresh() }
86:     }
87:   }
88: }
89: 
90: @Composable
91: fun InsightsScreen(
92:   onNavigate: (String) -> Unit = {},
93:   viewModel: InsightsViewModel = hiltViewModel()
94: ) {
95:   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
96: 
97:   AppScreen(
98:     title = "Weekly insights",
99:     subtitle = "A softer review of patterns, not a scoreboard.",
100:     showBottomNav = true,
101:     selectedBottomRoute = Screen.Insights.route,
102:     onNavigate = onNavigate
103:   ) {
104:     BreatheCard(containerColor = BreatheOverlay.copy(alpha = 0.54f)) {
105:       if (uiState.headline.isBlank() && uiState.recommendations.isEmpty()) {
106:         Column(
107:           modifier = Modifier.fillMaxWidth(),
108:           horizontalAlignment = Alignment.CenterHorizontally,
109:           verticalArrangement = Arrangement.spacedBy(12.dp)
110:         ) {
111:           Icon(imageVector = Icons.Rounded.AutoGraph, contentDescription = null, tint = BreatheBorder)
112:           Text(
113:             text = "No weekly insight yet",
114:             style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
115:             color = BreatheInk,
116:             textAlign = TextAlign.Center
117:           )
118:           Text(
119:             text = "As you log calm, status, and timeout moments, a softer reflection will gather here.",
120:             style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
121:             color = BreatheMutedInk,
122:             textAlign = TextAlign.Center
123:           )
124:         }
125:       } else {
126:         Icon(imageVector = Icons.Rounded.AutoGraph, contentDescription = null, tint = BreatheAccentStrong)
127:         Text(
128:           text = uiState.headline,
129:           style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
130:           color = BreatheInk
131:         )
132:         Text(
133:           text = "This is a weekly reflection on rhythm, not a measure of relationship performance.",
134:           style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
135:           color = BreatheMutedInk
136:         )
137:       }
138:     }
139: 
140:     AdaptiveTwoPane(
141:       first = { paneModifier ->
142:         InsightStatCard(modifier = paneModifier, label = "Top feature", value = uiState.topFeature ?: "none")
143:       },
144:       second = { paneModifier ->
145:         InsightStatCard(modifier = paneModifier, label = "Average mood", value = uiState.weeklySummary.averageMood?.formatOneDecimal() ?: "n/a")
146:       }
147:     )
148: 
149:     AdaptiveTwoPane(
150:       first = { paneModifier ->
151:         InsightStatCard(modifier = paneModifier, label = "Mood direction", value = uiState.moodTrend.direction)
152:       },
153:       second = { paneModifier ->
154:         InsightStatCard(modifier = paneModifier, label = "Shared reflections", value = uiState.weeklySummary.sharedReflections.toString())
155:       }
156:     )
157: 
158:     BreatheCard(containerColor = BreatheCardSurface) {
159:       Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
160:         Icon(imageVector = Icons.Rounded.TipsAndUpdates, contentDescription = null, tint = BreatheAccentStrong)
161:         Text("Recommendations", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = BreatheAccentStrong)
162:       }
163: 
164:       if (uiState.recommendations.isEmpty()) {
165:         Column(
166:           modifier = Modifier.fillMaxWidth(),
167:           horizontalAlignment = Alignment.CenterHorizontally,
168:           verticalArrangement = Arrangement.spacedBy(8.dp)
169:         ) {
170:           Text(
171:             text = "No recommendations yet.",
172:             style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
173:             color = BreatheMutedInk,
174:             textAlign = TextAlign.Center
175:           )
176:           Text(
177:             text = "A few more check-ins will give this space something gentle to offer back.",
178:             style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
179:             color = BreatheMutedInk,
180:             textAlign = TextAlign.Center
181:           )
182:         }
183:       } else {
184:         uiState.recommendations.forEach { recommendation ->
185:           Row(
186:             modifier = Modifier
187:               .fillMaxWidth()
188:               .background(BreatheOverlay.copy(alpha = 0.52f), RoundedCornerShape(18.dp))
189:               .padding(horizontal = 16.dp, vertical = 12.dp),
190:             horizontalArrangement = Arrangement.spacedBy(10.dp),
191:             verticalAlignment = Alignment.Top
192:           ) {
193:             Text("•", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = BreatheAccentStrong)
194:             Text(
195:               text = recommendation,
196:               modifier = Modifier.weight(1f),
197:               style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
198:               color = BreatheInk
199:             )
200:           }
201:         }
202:       }
203:     }
204: 
205:     BreatheCard(containerColor = BreatheYellow.copy(alpha = 0.18f)) {
206:       Text(
207:         text = "This summary is here to steady your view of the week, not to judge it.",
208:         style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
209:         color = BreatheInk
210:       )
211:     }
212: 
213:     PrimaryActionButton(
214:       text = "Refresh",
215:       onClick = { viewModel.onEvent(InsightsUiEvent.Refresh) },
216:       icon = Icons.Rounded.Refresh
217:     )
218:   }
219: }
220: 
221: @Composable
222: private fun InsightStatCard(label: String, value: String, modifier: Modifier = Modifier) {
223:   BreatheCard(modifier = modifier.heightIn(min = 104.dp), containerColor = BreatheCardSurface) {
224:     Text(label.uppercase(), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = BreatheMutedInk)
225:     Text(
226:       text = value,
227:       style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
228:       color = BreatheInk,
229:       maxLines = 2,
230:       overflow = TextOverflow.Ellipsis
231:     )
232:   }
233: }
234: 
235: private fun Double.formatOneDecimal(): String = String.format("%.1f", this)
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
