package com.breathe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.breathe.presentation.navigation.BreatheNavGraph
import com.breathe.presentation.theme.BreatheTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      BreatheTheme {
        Surface {
          Box {
            BreatheNavGraph()
            BuildBadge(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BuildBadge(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
    tonalElevation = 4.dp,
    shadowElevation = 2.dp,
    shape = MaterialTheme.shapes.small
  ) {
    Text(
      text = "Breathe ${BuildConfig.VERSION_NAME}",
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
