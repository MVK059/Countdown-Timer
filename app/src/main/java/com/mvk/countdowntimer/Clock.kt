package com.mvk.countdowntimer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun Number(value: Int, isActive: Boolean, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(
        if (isActive) MaterialTheme.colors.primary else MaterialTheme.colors.primaryVariant,
    )

    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            color = Color.White,
        )
    }
}

@Composable
@Preview
fun NumberPreview() {
    Column {
        Number(value = 3, isActive = true, modifier = Modifier.size(40.dp))
        Number(value = 7, isActive = true, modifier = Modifier.size(40.dp))
    }
}

@Composable
fun NumberColumn(
    current: Int,
    range: IntRange,
    modifier: Modifier = Modifier,
) {
    // Size of each item
    val size = 40.dp
    // Align active numbers to center of the screen
    // Distance of the current digit from the midpoint, times the height of each individual digit.
    val mid = (range.last - range.first) / 2f
    val reset = current == range.first
    val offset by animateDpAsState(
        targetValue = size * (mid - current),
        animationSpec = if (reset) {
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        }
    )

    Column(
        modifier = modifier
            .offset(y = offset)
            .clip(RoundedCornerShape(percent = 25))
    ) {
        range.forEach { num ->
            Number(num, num == current, Modifier.size(size))
        }
    }
}

@Composable
@Preview
fun NumberColumnPreview() {
    NumberColumn(range = 0..9, current = 5)
}

@Composable
fun Clock(time: Time) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val padding = Modifier.padding(horizontal = 4.dp)

        NumberColumn(time.hours / 10, 0..2, padding)
        NumberColumn(time.hours % 10, 0..9, padding)

        Spacer(Modifier.size(16.dp))

        NumberColumn(time.minutes / 10, 0..5, padding)
        NumberColumn(time.minutes % 10, 0..9, padding)

        Spacer(Modifier.size(16.dp))

        NumberColumn(time.seconds / 10, 0..5, padding)
        NumberColumn(time.seconds % 10, 0..9, padding)
    }
}

@Composable
@Preview
fun ClockPreview() {
    Clock(Time(14, 45, 50))
}

@Composable
fun ClockScreen() {
    fun currentTime(): Time {
        val cal = Calendar.getInstance()
        return Time(
            hours = cal.get(Calendar.HOUR_OF_DAY),
            minutes = cal.get(Calendar.MINUTE),
            seconds = cal.get(Calendar.SECOND),
        )
    }

    var time by remember { mutableStateOf(currentTime()) }
    /*
    LaunchedEffect runs the suspending lambda passed to it, which will update time every second.
    LaunchedEffect will only recompose if its key parameter changes: the hardcoded dummy 0 value
    here ensures that it only starts this loop once.
    When the Composable that contains it leaves the composition, the coroutine will be cancelled.
     */
    LaunchedEffect(0) {
        while (true) {
            time = currentTime()
            delay(1000)
        }
    }

    Clock(time)
}