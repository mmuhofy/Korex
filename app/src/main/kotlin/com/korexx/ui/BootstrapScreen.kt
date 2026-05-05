package com.korexx.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BootstrapScreen(
    message: String,
    percent: Int,
) {
    val animatedProgress by animateFloatAsState(
        targetValue    = percent / 100f,
        animationSpec  = tween(durationMillis = 300),
        label          = "bootstrap_progress",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.padding(horizontal = 40.dp),
        ) {
            Icon(
                imageVector        = Icons.Rounded.Terminal,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text       = "Korex",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text     = "Setting up environment…",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            LinearProgressIndicator(
                progress          = { animatedProgress },
                modifier          = Modifier.fillMaxWidth(),
                color             = MaterialTheme.colorScheme.primary,
                trackColor        = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text     = message,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                fontSize = 11.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = "$percent%",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}