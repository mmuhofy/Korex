package com.muhofy.korex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val RAIL_WIDTH = 44.dp
private val ICON_SIZE  = 22.dp

@Composable
fun LeftBar(
    onHamburgerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(RAIL_WIDTH)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Icon(
            imageVector        = Icons.Rounded.Menu,
            contentDescription = "Toggle session bar",
            tint               = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            modifier           = Modifier
                .padding(top = 14.dp)
                .size(ICON_SIZE)
                .clickable { onHamburgerClick() },
        )
    }
}