package com.alveteg.simon.minutelauncher.home.stats

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEndAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.shape.Shape
import timber.log.Timber
import java.time.LocalDate
import kotlin.math.max

@Composable
fun UsageBarGraph(
  usageStatistics: List<UsageStatistics>
) {
  val sortedStats =
    usageStatistics.sortedBy { it.usageDate }
  val millisInHour = 3600000f
  val maxDuration = sortedStats.maxOf { it.usageDuration }
  val hours = (maxDuration.div(millisInHour)).toInt()
  val tenners = (maxDuration.div(millisInHour.div(6))).toInt()
  val maxY = if (hours > 0) {
    hours.plus(1).times(millisInHour)
  } else {
    tenners.plus(2).coerceAtMost(5).times(millisInHour.div(6))
  }
  Surface(
    modifier = Modifier
      .height(220.dp)
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.large,
    tonalElevation = 8.dp
  ) {
    CartesianChartHost(
      modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 2.dp),
      chart = rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider = ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(
              color = MaterialTheme.colorScheme.primary,
              thickness = 40.dp,
              shape = remember { Shape.rounded(8f) }
            )
          ),
          spacing = 4.dp,
          axisValueOverrider = AxisValueOverrider.fixed(
            minY = 0f,
            maxY = maxY
          ),
        ),
        endAxis = rememberEndAxis(
          valueFormatter = { value, _, _ ->
            value.toLong().toTimeUsed()
          },
          itemPlacer = remember { VerticalPlacer(maxY) },
          guideline = rememberAxisGuidelineComponent(
            color = MaterialTheme.colorScheme.background,
            shape = Shape.Rectangle
          ),
          axis = rememberLineComponent(thickness = 0.dp),
          tick = rememberLineComponent(thickness = 0.dp)
        )
      ),
      model = CartesianChartModel(
        ColumnCartesianLayerModel.build {
          val dates = sortedStats.map { it.usageDate.toEpochDay() - LocalDate.now().toEpochDay() + 7  }
          val durations = sortedStats.map { it.usageDuration }
          Timber.d("$dates, ${durations.map { it.toTimeUsed() }}")
          series(y = durations, x = dates)
        }
      ),
      zoomState = rememberVicoZoomState(zoomEnabled = false),
      scrollState = rememberVicoScrollState(scrollEnabled = false)
    )
  }
}

class VerticalPlacer(
  private val maxY: Float,
  private val shiftTopLines: Boolean = false
) : AxisItemPlacer.Vertical {
  override fun getBottomVerticalAxisInset(
    context: CartesianMeasureContext,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
    maxLabelHeight: Float,
    maxLineThickness: Float
  ): Float = when (verticalLabelPosition) {
    VerticalAxis.VerticalLabelPosition.Top -> maxLineThickness
    VerticalAxis.VerticalLabelPosition.Center ->
      (maxOf(maxLabelHeight, maxLineThickness) + maxLineThickness).half

    else -> maxLabelHeight + maxLineThickness.half
  }

  override fun getHeightMeasurementLabelValues(
    context: CartesianMeasureContext,
    position: AxisPosition.Vertical
  ): List<Float> = axisValues(context, position)

  override fun getLabelValues(
    context: CartesianDrawContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: AxisPosition.Vertical
  ): List<Float> = axisValues(context, position)

  override fun getTopVerticalAxisInset(
    context: CartesianMeasureContext,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
    maxLabelHeight: Float,
    maxLineThickness: Float
  ): Float = when (verticalLabelPosition) {
    VerticalAxis.VerticalLabelPosition.Top ->
      maxLabelHeight + (if (shiftTopLines) maxLineThickness else -maxLineThickness).half

    VerticalAxis.VerticalLabelPosition.Center ->
      (max(maxLabelHeight, maxLineThickness) +
          if (shiftTopLines) maxLineThickness else -maxLineThickness)
        .half

    else -> if (shiftTopLines) maxLineThickness else 0f
  }

  override fun getWidthMeasurementLabelValues(
    context: CartesianMeasureContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: AxisPosition.Vertical
  ): List<Float> = axisValues(context, position)

  private fun axisValues(
    context: CartesianMeasureContext,
    position: AxisPosition.Vertical
  ): List<Float> {
    val values = mutableListOf<Float>()
    val yRange = context.chartValues.getYRange(position)
    val minutes = yRange.maxY.div(60000)
    if (minutes >= 60) {
      val hours = maxY.div(3600000).toInt() + 1
      repeat(hours) { values += 0f.plus(3600000).times(it) }
    } else {
      val tenners = maxY.div(600000).toInt() + 1
      repeat(tenners) { values += 0f.plus(600000).times(it) }
    }
    Timber.d("Y Axis: $values")
    return values
  }
}