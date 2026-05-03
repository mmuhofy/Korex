package com.muhofy.korex.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhofy.korex.data.snippet.SnippetEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetSheet(
    snippets: List<SnippetEntity>,
    onDismiss: () -> Unit,
    onExecute: (String) -> Unit,
    onAdd: (title: String, command: String) -> Unit,
    onEdit: (SnippetEntity, title: String, command: String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text     = "Snippets",
                    style    = MaterialTheme.typography.titleSmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector        = Icons.Rounded.Add,
                    contentDescription = "Add snippet",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier
                        .size(22.dp)
                        .clickable { showAddDialog = true },
                )
            }

            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                thickness = 0.5.dp,
            )

            if (snippets.isEmpty()) {
                Text(
                    text     = "No snippets yet. Tap + to add one.",
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn {
                    items(snippets, key = { it.id }) { snippet ->
                        SnippetItem(
                            snippet  = snippet,
                            onTap    = {
                                onExecute(snippet.command)
                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion { onDismiss() }
                            },
                            onEdit   = { t, c -> onEdit(snippet, t, c) },
                            onDelete = { onDelete(snippet.id) },
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SnippetDialog(
            title      = "",
            command    = "",
            dialogTitle = "New Snippet",
            onConfirm  = { t, c ->
                onAdd(t, c)
                showAddDialog = false
            },
            onDismiss  = { showAddDialog = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SnippetItem(
    snippet: SnippetEntity,
    onTap: () -> Unit,
    onEdit: (String, String) -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded  by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick     = onTap,
                onLongClick = { menuExpanded = true },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector        = Icons.Rounded.Code,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier           = Modifier.size(18.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = snippet.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Text(
                text     = snippet.command,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
            )
        }

        DropdownMenu(
            expanded         = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text        = { Text("Edit") },
                leadingIcon = { Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp)) },
                onClick     = {
                    menuExpanded = false
                    showEditDialog = true
                },
            )
            DropdownMenuItem(
                text        = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Close, null,
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                },
                onClick     = {
                    menuExpanded = false
                    onDelete()
                },
            )
        }
    }

    HorizontalDivider(
        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
        thickness = 0.5.dp,
    )

    if (showEditDialog) {
        SnippetDialog(
            title       = snippet.title,
            command     = snippet.command,
            dialogTitle = "Edit Snippet",
            onConfirm   = { t, c ->
                onEdit(t, c)
                showEditDialog = false
            },
            onDismiss   = { showEditDialog = false },
        )
    }
}

@Composable
private fun SnippetDialog(
    title: String,
    command: String,
    dialogTitle: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var t by remember { mutableStateOf(title) }
    var c by remember { mutableStateOf(command) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text(dialogTitle) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = t,
                    onValueChange = { t = it },
                    label         = { Text("Title") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value         = c,
                    onValueChange = { c = it },
                    label         = { Text("Command") },
                    singleLine    = false,
                    minLines      = 2,
                    modifier      = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton    = {
            TextButton(
                onClick = { if (t.isNotBlank() && c.isNotBlank()) onConfirm(t, c) },
            ) { Text("Save") }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}