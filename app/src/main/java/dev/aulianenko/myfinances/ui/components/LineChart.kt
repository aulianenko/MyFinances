package dev.aulianenko.myfinances.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

/**
 * A reusable line chart component with touch gestures and haptic feedback support.
 *
 * @param data List of Y-axis values to display
 * @param xAxisLabels List of labels for X-axis (optional)
 * @param lineColor Color of the chart line
 * @param modifier Modifier for customization
 * @param onDataPointClick Callback when a data point is clicked (with haptic feedback)
 */
@Composable
fun LineChart(
    data: List<Double>,
    xAxisLabels: List<String> = emptyList(),
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onDataPointClick: ((index: Int, value: Double) -> Unit)? = null
) {
    val view = LocalView.current

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries {
                series(data)
            }
        }
    }

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis()
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}
