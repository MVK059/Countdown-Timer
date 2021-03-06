package com.mvk.countdowntimer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class TickWheelState(val scope: CoroutineScope) {
    var secondsLeft by mutableStateOf<Int>(0)
    var isDragging by mutableStateOf(false)
    var endPosition by mutableStateOf<Offset?>(null)
//    var isCountingDown by mutableStateOf(false)
    private var job: Job? = null
    /**
     * Calculate the time in seconds
     */
    val time: String
        get() {
            return "${secondsLeft}s"
        }

    fun startDrag(startPosition: Offset) {
        isDragging = true
        endPosition = startPosition
        stop()
    }

    fun onDrag(delta: Offset) {
        val current = endPosition
        val next = current?.let { it + delta } ?: delta
        secondsLeft = ((next.theta + 180f) / 360f * 60f).roundToInt()
        endPosition = next
    }

    fun endDrag() {
        val current = endPosition
        if (current != null) {
            secondsLeft = ((current.theta + 180f) / 360f * 60f).roundToInt()
            isDragging = false
        } else {
            error("Position was null when it shouldn't have been")
        }
    }

    fun toggle() {
        if (job == null) {
            job = scope.launch {
                while (secondsLeft > 0) {
                    delay(1000)
//                    secondsLeft--
                    val next = secondsLeft - 1
                    secondsLeft = next
                    val theta = (((next % 60) * 6 - 180) * PI / 180).toFloat()
                    val radius = 100f
                    endPosition = Offset(
                        cos(theta) * radius,
                        sin(theta) * radius
                    )
                }
            }
        } else {
            stop()
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}