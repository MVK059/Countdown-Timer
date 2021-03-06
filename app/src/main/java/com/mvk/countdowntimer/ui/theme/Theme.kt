package com.mvk.countdowntimer.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun CountdownTimerTheme(
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colors = darkColors(
            primary = darkRed,
            primaryVariant = lightOrange,
            secondary = Teal200,
            background = bgColorEdge
        ),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}