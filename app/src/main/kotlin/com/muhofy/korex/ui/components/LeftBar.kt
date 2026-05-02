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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.muhofy.korex.R

private val RAIL_WIDTH = 48.dp
private val ICON_SIZE  = 22.dp

// UNTESTED — verify before use
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
        // Hamburger — top
        Icon(
            painter            = painterResource(R.drawable.ic_menu),
            contentDescription = if (isPanelOpen) "Close panel" else "Open panel",
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier
                .padding(top = 12.dp, bottom = 8.dp)
                .size(ICON_SIZE)
                .clickable { onHamburgerClick() },
        )

        // Sessions icon
        RailIcon(
            iconRes     = R.drawable.ic_terminal,
            description = "Sessions",
            active      = isPanelOpen,
            onClick     = onHamburgerClick,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Settings icon — bottom
        RailIcon(
            iconRes     = R.drawable.ic_settings,
            description = "Settings",
            active      = false,
            onClick     = { /* settings panel — later phase */ },
            modifier    = Modifier.padding(bottom = 12.dp),
        )
    }
}

@Composable
private fun RailIcon(
    iconRes: Int,
    description: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter            = painterResource(iconRes),
        contentDescription = description,
        tint               = if (active)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier           = modifier
            .padding(vertical = 8.dp)
            .size(ICON_SIZE)
            .clickable { onClick() },
    )
}