package com.muhofy.korex.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhofy.korex.data.history.CommandHistoryEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandHistorySheet(
    history: List<CommandHistoryEntity>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onExecute: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    fun dismiss() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text     = "History",
                    style    = MaterialTheme.typography.titleSmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                )
                if (history.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Icon(
                            Icons.Rounded.Delete, null,
                            modifier = Modifier.size(16.dp),
                            tint     = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text     = "Clear all",
                            color    = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = onQueryChange,
                placeholder   = { Text("Search commands...") },
                leadingIcon   = {
                    Icon(Icons.Rounded.Search, null, modifier = Modifier.size(18.dp))
                },
                trailingIcon  = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Rounded.Clear, "Clear search",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onQueryChange("") },
                        )
                    }
                },
                singleLine    = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )

            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                thickness = 0.5.dp,
                modifier  = Modifier.padding(top = 8.dp),
            )

            if (history.isEmpty()) {
                Text(
                    text     = if (searchQuery.isBlank()) "No commands yet." else "No results.",
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn {
                    items(history, key = { it.id }) { entry ->
                        HistoryItem(
                            entry    = entry,
                            onTap    = {
                                onExecute(entry.command)
                                dismiss()
                            },
                            onDelete = { onDelete(entry.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryItem(
    entry: CommandHistoryEntity,
    onTap: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector        = Icons.Rounded.History,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
            modifier           = Modifier.size(16.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = entry.command,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text     = DATE_FORMAT.format(Date(entry.executedAt)),
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                fontSize = 11.sp,
            )
        }

        Icon(
            imageVector        = Icons.Rounded.Close,
            contentDescription = "Delete",
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier           = Modifier
                .size(14.dp)
                .clickable { onDelete() },
        )
    }

    HorizontalDivider(
        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.07f),
        thickness = 0.5.dp,
    )
}