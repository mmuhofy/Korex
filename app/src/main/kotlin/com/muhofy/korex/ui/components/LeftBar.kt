package com.muhofy.korex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.List
import com.adamglin.phosphoricons.regular.X
import com.adamglin.phosphoricons.regular.GearSix

private val RAIL_WIDTH = 44.dp
private val ICON_SIZE  = 20.dp

@Composable
fun LeftBar(
    isPanelOpen: Boolean,
    onHamburgerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(RAIL_WIDTH)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        // Hamburger / Close toggle
        Icon(
            imageVector        = if (isPanelOpen) PhosphorIcons.Regular.X else PhosphorIcons.Regular.List,
            contentDescription = if (isPanelOpen) "Close panel" else "Open panel",
            tint               = if (isPanelOpen)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier           = Modifier
                .padding(top = 16.dp)
                .size(ICON_SIZE)
                .clip(RoundedCornerShape(6.dp))
                .clickable { onHamburgerClick() },
        )

        Spacer(modifier = Modifier.weight(1f))

        // Settings — bottom
        Icon(
            imageVector        = PhosphorIcons.Regular.GearSix,
            contentDescription = "Settings",
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier           = Modifier
                .padding(bottom = 16.dp)
                .size(ICON_SIZE)
                .clickable { /* settings — later phase */ },
        )
    }
}