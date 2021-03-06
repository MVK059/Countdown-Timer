package com.mvk.countdowntimer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import com.mvk.countdowntimer.ui.theme.bgColorCenter
import com.mvk.countdowntimer.ui.theme.bgColorEdge
import com.mvk.countdowntimer.ui.theme.darkRed
import com.mvk.countdowntimer.ui.theme.lightOrange
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val StartRadiusFraction = 0.5f
const val EndRadiusFraction = 0.75f
const val TickWidth = 9f

@Composable
fun Countdown() {
    val scope = rememberCoroutineScope()
    val state = remember { TickWheelState(scope) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(bgColorCenter, bgColorEdge))),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TickWheel(
            modifier = Modifier.fillMaxWidth(),
            ticks = 60,
            startColor = lightOrange,
            endColor = darkRed,
            state = state
        ) {
            // Click Text
            Text(
                text = state.time,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
        IconButton(onClick = { state.toggle() }) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
        }
    }
}

@Composable
fun TickWheel(
    modifier: Modifier,
    ticks: Int,
    startColor: Color,
    endColor: Color,
    state: TickWheelState,
    content: @Composable () -> Unit
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier
            .aspectRatio(1f)    // Square Box
            .onSizeChanged { origin = it.center.toOffset() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        state.startDrag(offset - origin)
                    },
                    onDragEnd = {
                        state.endDrag()
                    },
                    onDragCancel = {
                        state.endDrag()
                    },
                    onDrag = { change, dragAmount ->
                        state.onDrag(dragAmount)
                        // Consumes all changes associated with the PointerInputChange
                        change.consumeAllChanges()
                    }
                )
            }
            .drawBehind {
                // Tapped angle on the screen. Range: -180 to 180
                val endTheta = state.endPosition?.theta ?: -180f
                // Start and End Radius
                val startRadius = size.width / 2 * StartRadiusFraction
                val endRadius = size.width / 2 * EndRadiusFraction
                val sweep = Brush.sweepGradient(
                    0.0f to startColor,
                    1.0f to endColor,
                    center = center
                )
                val offBrush = SolidColor(Color.White.copy(alpha = 0.1f))
                // Pick an angle with endTheta and go around the circle of 360 degree
                for (i in 0 until ticks) {
                    val angle = i * (360 / ticks) - 180 // -180 to 180
                    // Converts the tick angle back to radians
                    val theta = angle * PI.toFloat() / 180f
                    // Start position
                    val startPos = Offset(
                        cos(theta) * startRadius,
                        sin(theta) * startRadius
                    )
                    // End position
                    val endPos = Offset(
                        cos(theta) * endRadius,
                        sin(theta) * endRadius
                    )
                    // If the current angle is less than where the finger is, it is on
                    val on = angle < endTheta
                    // Draw line relative to the center
                    drawLine(
                        if (on) sweep else offBrush,
                        center + startPos,
                        center + endPos,
                        strokeWidth = TickWidth,
                        StrokeCap.Round
                    )
                }

            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}