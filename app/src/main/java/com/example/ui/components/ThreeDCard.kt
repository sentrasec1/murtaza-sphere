package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun ThreeDCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }

    val animateRotationX by animateFloatAsState(targetValue = rotationX, label = "x")
    val animateRotationY by animateFloatAsState(targetValue = rotationY, label = "y")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        rotationY = (change.position.x - size.width / 2) / size.width * 30f
                        rotationX = (change.position.y - size.height / 2) / size.height * -30f
                    },
                    onDragEnd = { rotationX = 0f; rotationY = 0f },
                    onDragCancel = { rotationX = 0f; rotationY = 0f }
                )
            }
            .graphicsLayer {
                this.rotationX = animateRotationX
                this.rotationY = animateRotationY
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        tonalElevation = 4.dp
    ) {
        Box(content = content)
    }
}
