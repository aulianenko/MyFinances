package dev.aulianenko.myfinances.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Enhanced card elevation values for better depth perception.
 */
object CardElevation {
    val low = 2.dp
    val medium = 4.dp
    val high = 8.dp
}

/**
 * Standard card shapes used throughout the app.
 */
object CardShapes {
    val small = RoundedCornerShape(12.dp)
    val medium = RoundedCornerShape(16.dp)
    val large = RoundedCornerShape(20.dp)
    val extraLarge = RoundedCornerShape(24.dp)
}

/**
 * Enhanced card colors with better contrast.
 */
@Composable
fun enhancedCardColors(
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) = CardDefaults.cardColors(
    containerColor = containerColor,
    contentColor = contentColor
)

/**
 * Primary card elevation for dashboard cards and main content.
 */
@Composable
fun primaryCardElevation() = CardDefaults.cardElevation(
    defaultElevation = CardElevation.medium,
    pressedElevation = CardElevation.high,
    hoveredElevation = CardElevation.high
)

/**
 * Secondary card elevation for list items and secondary content.
 */
@Composable
fun secondaryCardElevation() = CardDefaults.cardElevation(
    defaultElevation = CardElevation.low,
    pressedElevation = CardElevation.medium,
    hoveredElevation = CardElevation.medium
)
