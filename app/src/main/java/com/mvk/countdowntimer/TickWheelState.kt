package com.mvk.countdowntimer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

class TickWheelState(val scope: CoroutineScope) {
    var totalSeconds by mutableStateOf<Int>(0)
    val seconds: Int get() = totalSeconds % 60
    val minutes: Int get() = floor(totalSeconds.toDouble() / 60).toInt()
    var isDragging by mutableStateOf(false)
    var endPosition by mutableStateOf<Offset?>(null)
    private var job: Job? = null
    /**
     * Calculate the time in seconds
     */
    val time: String
        get() {
            return buildString {
                append("$minutes".padStart(2, '0'))
                append(":")
                append("$seconds".padStart(2, '0'))
            }
        }

    fun startDrag(startPosition: Offset) {
        isDragging = true
        endPosition = startPosition
        stop()
    }

    fun onDrag(delta: Offset) {
        val prev = endPosition
        val next = if (prev != null) {
            val prevTheta = prev.theta
            val next = prev + delta
            val nextTheta = next.theta
            // Increment minutes when you draw a circle
            val nextMinutes = when {
                // One circle complete
                prevTheta > 90f && nextTheta < -90f -> minutes + 1
                // One circle in the opposite direction
                prevTheta < -90f && nextTheta > 90f -> max(0, minutes - 1)
                else -> minutes
            }
            totalSeconds = nextMinutes * 60 + ((next.theta + 180f) / 360f * 60f).roundToInt()
            next
        } else {
            delta
        }
        endPosition = next
    }

    fun endDrag() {
        val current = endPosition
        if (current != null) {
//            totalSeconds = ((current.theta + 180f) / 360f * 60f).roundToInt()
            isDragging = false
        } else {
            error("Position was null when it shouldn't have been")
        }
    }

    fun toggle() {
        if (job == null) {
            job = scope.launch {
                while (totalSeconds > 0) {
                    delay(1000)
                    countdown()
                }
                endPosition = null
            }
        } else {
            stop()
        }
    }

    fun countdown() {
        val next = totalSeconds - 1
        val theta = (((next % 60) * 6 - 180) * PI / 180).toFloat()
        val radius = 100f
        totalSeconds = next
        endPosition = Offset(
            cos(theta) * radius,
            sin(theta) * radius
        )
    }

    private fun stop() {
        job?.cancel()
        job = null
    }
}