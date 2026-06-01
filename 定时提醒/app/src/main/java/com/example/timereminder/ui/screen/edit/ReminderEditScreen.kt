package com.example.timereminder.ui.screen.edit

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.timereminder.domain.model.ReminderType
import com.example.timereminder.domain.model.Tag
import com.example.timereminder.ui.component.RepeatSelector
import com.example.timereminder.ui.theme.TagColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditScreen(
    viewModel: ReminderEditViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val saveCompleted by viewModel.saveCompleted.collectAsState()

    // 保存成功后返回
    LaunchedEffect(saveCompleted) {
        if (saveCompleted) {
            onNavigateBack()
        }
    }

    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 系统铃声选择器
    val systemRingtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.getParcelableExtra<Uri>(
            RingtoneManager.EXTRA_RINGTONE_PICKED_URI
        )
        if (uri != null) {
            val name = viewModel.resolveRingtoneName(context.contentResolver, uri.toString())
            viewModel.updateRingtone(uri.toString(), name)
        }
    }

    // 本地文件选择器（选择音频文件）
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // 获取持久化读取权限
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            val name = viewModel.resolveRingtoneName(context.contentResolver, uri.toString())
            viewModel.updateRingtone(uri.toString(), name)
        }
    }

    fun showSystemRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择铃声")
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, state.ringtoneUri)
        }
        systemRingtoneLauncher.launch(intent)
    }

    fun showFilePicker() {
        filePickerLauncher.launch(arrayOf("audio/*"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isLoaded) "编辑提醒" else "新建提醒",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题输入
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("提醒标题") },
                placeholder = { Text("例如：喝水、吃药、开会...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 备注输入
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("备注（可选）") },
                placeholder = { Text("添加一些备注信息...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // 时间选择
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⏰ ${state.hour.toString().padStart(2, '0')}:${state.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // 重复类型
            RepeatSelector(
                selectedType = state.type,
                onTypeSelected = viewModel::updateType,
                intervalMinutes = state.intervalMinutes,
                onIntervalChange = viewModel::updateIntervalMinutes,
                selectedDays = state.selectedDays,
                onDayToggle = viewModel::toggleDay,
                selectedDayOfMonth = state.dayOfMonth,
                onDayOfMonthChange = viewModel::updateDayOfMonth
            )

            // 提醒方式
            Text(
                text = "提醒方式",
                style = MaterialTheme.typography.titleMedium
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingSwitch(
                    label = "通知",
                    checked = state.isNotificationEnabled,
                    onCheckedChange = { viewModel.toggleNotification() }
                )
                SettingSwitch(
                    label = "响铃",
                    checked = state.isRingEnabled,
                    onCheckedChange = { viewModel.toggleRing() }
                )
                SettingSwitch(
                    label = "震动",
                    checked = state.isVibrationEnabled,
                    onCheckedChange = { viewModel.toggleVibration() }
                )
            }

            // 铃声选择
            RingtoneSelector(
                ringtoneDisplayName = state.ringtoneDisplayName,
                onSelectDefault = { viewModel.resetRingtoneToDefault() },
                onSelectFromSystem = { showSystemRingtonePicker() },
                onSelectFromFile = { showFilePicker() }
            )

            // 标签选择
            TagSelector(
                tags = tags,
                selectedTagId = state.selectedTagId,
                onTagSelected = viewModel::updateTagId
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 保存按钮
            Button(
                onClick = viewModel::saveReminder,
                enabled = state.title.isNotBlank() && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("保存提醒", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 时间选择器对话框
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.hour,
            initialMinute = state.minute,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        viewModel.updateTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("确认")
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * 铃声选择器
 */
@Composable
private fun RingtoneSelector(
    ringtoneDisplayName: String,
    onSelectDefault: () -> Unit,
    onSelectFromSystem: () -> Unit,
    onSelectFromFile: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "铃声",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = ringtoneDisplayName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "更换",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("选择铃声") },
            text = {
                Column {
                    TextButton(
                        onClick = { onSelectDefault(); showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("默认系统闹钟铃声", modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = { onSelectFromSystem(); showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("从系统铃声库选择", modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = { onSelectFromFile(); showDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("从本地文件选择", modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagSelector(
    tags: List<Tag>,
    selectedTagId: Long?,
    onTagSelected: (Long?) -> Unit
) {
    val selectedTag = tags.find { it.id == selectedTagId }
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "标签",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedTag?.name ?: "无标签",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("无标签") },
                    onClick = {
                        onTagSelected(null)
                        expanded = false
                    }
                )
                tags.forEach { tag ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 颜色圆点
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .padding(0.dp)
                                ) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(Color(tag.color))
                                    }
                                }
                                Text(tag.name)
                            }
                        },
                        onClick = {
                            onTagSelected(tag.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
