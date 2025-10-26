package dev.aulianenko.myfinances.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A reusable pie chart component with legend for portfolio distribution.
 *
 * @param data List of data points with labels and values
 * @param colors List of colors for each segment (optional)
 * @param modifier Modifier for customization
 * @param showPercentages Whether to show percentages in the legend
 */
@Composable
fun PieChart(
    data: List<PieChartData>,
    colors: List<Color> = defaultPieChartColors,
    modifier: Modifier = Modifier,
    showPercentages: Boolean = true
) {
    if (data.isEmpty()) {
        Text(
            text = "No data to display",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp)
        )
        return
    }

    val total = data.sumOf { it.value }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Placeholder for actual pie chart visualization
        // Vico doesn't have built-in pie charts in version 2.0.0-alpha.28
        // We'll create a simple legend-based visualization

        Text(
            text = "Portfolio Distribution",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Legend
        data.forEachIndexed { index, item ->
            val percentage = if (total > 0) (item.value / total) * 100 else 0.0
            val color = colors[index % colors.size]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Surface(
                    modifier = Modifier.size(16.dp),
                    shape = CircleShape,
                    color = color
                ) {}

                Spacer(modifier = Modifier.width(12.dp))

                // Label and value
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (showPercentages) {
                        Text(
                            text = String.format("%.1f%%", percentage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Value
                Text(
                    text = item.formattedValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

data class PieChartData(
    val label: String,
    val value: Double,
    val formattedValue: String
)

private val defaultPieChartColors = listOf(
    Color(0xFF6200EE),
    Color(0xFF03DAC6),
    Color(0xFFFF5722),
    Color(0xFF4CAF50),
    Color(0xFFFFC107),
    Color(0xFF2196F3),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF009688),
    Color(0xFFFF9800)
)
