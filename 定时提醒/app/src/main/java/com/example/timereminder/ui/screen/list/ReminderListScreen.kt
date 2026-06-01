package com.example.timereminder.ui.screen.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.timereminder.domain.model.Reminder
import com.example.timereminder.ui.component.ReminderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    viewModel: ReminderListViewModel,
    onAddReminder: () -> Unit,
    onEditReminder: (Long) -> Unit,
    onManageTags: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    val tags by viewModel.tags.collectAsState()

    // 创建标签ID → 颜色的映射
    val tagColorMap = remember(tags) {
        tags.associate { it.id to Color(it.color) }
    }

    var deleteConfirmReminder by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "定时提醒",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onManageTags) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "管理标签"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminder,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加提醒",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (reminders.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "还没有提醒\n点击 + 添加一个",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = reminders,
                    key = { it.id }
                ) { reminder ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                deleteConfirmReminder = reminder
                                false // 我们不直接删除，而是弹确认对话框
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true
                    ) {
                        ReminderCard(
                            reminder = reminder,
                            tagColor = reminder.tagId?.let { tagColorMap[it] },
                            onToggle = { viewModel.toggleEnabled(reminder) },
                            onClick = { onEditReminder(reminder.id) }
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (deleteConfirmReminder != null) {
        val reminderToDelete = deleteConfirmReminder!!
        AlertDialog(
            onDismissRequest = { deleteConfirmReminder = null },
            title = { Text("删除提醒") },
            text = { Text("确定要删除「${reminderToDelete.title}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReminder(reminderToDelete.id)
                        deleteConfirmReminder = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmReminder = null }) {
                    Text("取消")
                }
            }
        )
    }
}
