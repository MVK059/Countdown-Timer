package com.mvk.countdowntimer

import android.graphics.Matrix
import android.graphics.SweepGradient
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat.getSystemService
import com.mvk.countdowntimer.ui.theme.bgColorCenter
import com.mvk.countdowntimer.ui.theme.bgColorEdge
import com.mvk.countdowntimer.ui.theme.darkRed
import com.mvk.countdowntimer.ui.theme.lightOrange
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

const val TickWidth = 9f
const val Epsilon = 9f
const val RadiusA = 0.36f
const val RadiusB = 0.40f
const val RadiusC = 0.48f
const val RadiusD = 0.75f
const val RadiusE = 1.4f

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
    val vibrator = systemService<Vibrator>()
    val secondTransition by animateFloatAsState(targetValue = state.seconds.toFloat())
    val minuteTransition by animateFloatAsState(targetValue = state.minutes.toFloat())
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
            .drawWithCache {
                // Width of the screen
                val unitRadius = size.width / 2
                val a = unitRadius * RadiusA
                val b = unitRadius * RadiusB
                val c = unitRadius * RadiusC
                val d = unitRadius * RadiusD
                val e = unitRadius * RadiusE
                val offBrush = Color.White
                    .copy(alpha = 0.1f)
                    .toBrush()
                // User little over 180deg so that the first tick mark isn't split down the middle
                val matrix = Matrix().also {
                    it.setRotate(-182f, size.width / 2, size.height / 2)
                }
                Brush.sweepGradient()
                val sweep = ShaderBrush(SweepGradient(
                    size.width / 2,
                    size.height / 2,
                    startColor.toArgb(),
                    endColor.toArgb()
                ).also {
                    it.setLocalMatrix(matrix)
                })
                onDrawBehind {
                    // Tapped angle on the screen in deg. Range: -180 to 180
                    val endAngle = state.seconds * 6 - 180
                    val minutes = state.minutes
                    // Go through each tick
                    for (i in 0 until ticks) {
                        // We get an angle for each tick
                        val angle = i * (360 / ticks) - 180 // -180 to 180
                        // Converts the tick angle back to radians
                        val theta = angle * PI.toFloat() / 180f
                        // If the current angle is less than where the finger is, it is on
                        val on = angle < endAngle
                        val up = minutes >= minuteTransition
                        val t = 1 - abs(minutes - minuteTransition)

                        /*
                         LERP - Shorthand for linear interpolation.
                         You give it two points/values, it'll interpolate based on the third
                         value which is expected to go from 0 to 1 between the two
                         */
                        if (up) {
                            // Only needed when going up
                            if (minutes > 1) {
                                drawTick(
                                    sweep,
                                    theta,
                                    lerp(start = b, stop = a, fraction = t),
                                    lerp(start = c, stop = b, fraction = t),
                                    1 - t
                                )
                            }

                            if (minutes > 0) {
                                drawTick(
                                    sweep,
                                    theta,
                                    lerp(start = c, stop = b, fraction = t),
                                    lerp(start = d, stop = c, fraction = t),
                                    1f
                                )
                            }
                            drawTick(
                                if (on) sweep else offBrush,
                                theta,
                                lerp(start = d, stop = c, fraction = t),
                                lerp(start = e, stop = d, fraction = t),
                                t
                            )
                        } else {
                            // Only needed when going up
                            if (minutes > 0) {
                                drawTick(
                                    sweep,
                                    theta,
                                    lerp(start = a, stop = b, fraction = t),
                                    lerp(start = b, stop = c, fraction = t),
                                    t
                                )
                            }
                            drawTick(
                                if (on) sweep else offBrush,
                                theta,
                                lerp(start = b, stop = c, fraction = t),
                                lerp(start = c, stop = d, fraction = t),
                                1f
                            )
                            // Main tick. Draw line relative to the center
                            drawTick(
                                offBrush,
                                theta,
                                lerp(start = c, stop = d, fraction = t),
                                lerp(start = d, stop = e, fraction = t),
                                1 - t
                            )
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun DrawScope.drawTick(
    brush: Brush,
    theta: Float,
    startRadius: Float,
    endRadius: Float,
    alpha: Float
) {
    drawLine(
        brush,
        center + Offset(
            cos(theta) * (startRadius + Epsilon),
            sin(theta) * (startRadius + Epsilon)
        ),
        center + Offset(
            cos(theta) * (endRadius - Epsilon),
            sin(theta) * (endRadius - Epsilon)
        ),
        TickWidth,
        StrokeCap.Round,
        alpha = alpha.coerceIn(0f, 1f)
    )
}

@Composable
inline fun <reified T> systemService(): T? {
    val context = LocalContext.current
    return remember { getSystemService(context, T::class.java) }
}