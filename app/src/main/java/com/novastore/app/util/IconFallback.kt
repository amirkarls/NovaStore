package com.novastore.app.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

@Composable
fun AppIcon(name: String, url: String, size: Int = 56) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = name,
        modifier = Modifier.size(size.dp),
        loading = { Box(Modifier.size(size.dp)) },
        error = { FallbackIcon(name, size) },
        success = { SubcomposeAsyncImageContent() }
    )
}

@Composable
fun FallbackIcon(name: String, size: Int) {
    val colors = listOf(
        Color(0xFF6650a4), Color(0xFF00696E), Color(0xFF8B5000),
        Color(0xFF984061), Color(0xFF3F683B), Color(0xFF3D5F90)
    )
    val color = colors[name.hashCode().mod(colors.size)]
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = Color.White,
            fontSize = (size * 0.45).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
