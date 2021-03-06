package com.mvk.countdowntimer

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2

data class WheelPosition(
    val start: Offset,
    val current: Offset
) {
    val delta: Offset get() = current - start
    val theta: Float get() = delta.theta
}

// atan works for quandrant 1 & 4, atan2 works for all 4 quadrants
// Calculate angle from the center. atan2 gives angle in radians. Then we convert it to degrees
val Offset.theta: Float get() = (atan2(y.toDouble(), x.toDouble()) * 180.0 / PI).toFloat()

operator fun WheelPosition?.plus(rhs: Offset): WheelPosition =
    this?.copy(current = current + rhs) ?: WheelPosition(rhs, rhs)
