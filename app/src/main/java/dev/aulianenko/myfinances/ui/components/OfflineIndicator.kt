package dev.aulianenko.myfinances.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.aulianenko.myfinances.util.NetworkMonitor

/**
 * Displays a banner at the top of the screen when the device is offline.
 * Automatically shows/hides based on network connectivity.
 *
 * @param modifier Modifier to be applied to the container
 * @param networkMonitor NetworkMonitor instance to observe connectivity
 */
@Composable
fun OfflineIndicator(
    modifier: Modifier = Modifier,
    networkMonitor: NetworkMonitor
) {
    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)

    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Offline",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "No internet connection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Simpler variant that just returns whether the device is offline.
 * Useful for conditional rendering or disabling features.
 */
@Composable
fun isOffline(networkMonitor: NetworkMonitor): Boolean {
    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
    return !isOnline
}
