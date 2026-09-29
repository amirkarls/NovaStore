package com.novastore.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun CustomBottomNav(
    items: List<NavItem>,
    selectedRoute: String?,
    onItemClick: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val active = selectedRoute == item.route
                NavButton(
                    item = item,
                    active = active,
                    onClick = { onItemClick(item.route) }
                )
            }
        }
    }
}

@Composable
fun NavButton(
    item: NavItem,
    active: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (active)
            MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        animationSpec = tween(250),
        label = "nav_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (active)
            MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "nav_content"
    )
    val horizontalPadding by animateDpAsState(
        targetValue = if (active) 18.dp else 12.dp,
        animationSpec = tween(250),
        label = "nav_pad"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            if (active) {
                Spacer(Modifier.width(6.dp))
                Text(
                    item.label,
                    color = contentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}
