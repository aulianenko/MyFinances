package dev.aulianenko.myfinances.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Material 3 top app bar with scroll behavior support.
 *
 * @param title The title text to display
 * @param canNavigateBack Whether to show back navigation button
 * @param onNavigateBack Callback when back button is clicked
 * @param actions Optional action buttons (e.g., menu, search)
 * @param scrollBehavior Defines scroll behavior for collapsing/expanding
 * @param useLargeVariant Use large top app bar for main screens
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    actions: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    useLargeVariant: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val navigationIcon: @Composable () -> Unit = {
        if (canNavigateBack) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back"
                )
            }
        }
    }

    if (useLargeVariant && scrollBehavior != null) {
        LargeTopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = colors,
            scrollBehavior = scrollBehavior
        )
    } else {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = colors,
            scrollBehavior = scrollBehavior
        )
    }
}
