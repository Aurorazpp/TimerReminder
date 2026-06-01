package com.example.timereminder.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.timereminder.domain.model.ReminderType

/**
 * 重复类型选择器 + 对应配置
 */
@Composable
fun RepeatSelector(
    selectedType: ReminderType,
    onTypeSelected: (ReminderType) -> Unit,
    intervalMinutes: String,
    onIntervalChange: (String) -> Unit,
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit,
    selectedDayOfMonth: String,
    onDayOfMonthChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "重复",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 类型选择 Chip 行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                ReminderType.ONCE to "一次性",
                ReminderType.DAILY to "每天",
                ReminderType.WEEKLY to "每周",
                ReminderType.MONTHLY to "每月"
            ).forEach { (type, label) ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                ReminderType.INTERVAL to "间隔",
                ReminderType.WEEKDAY to "工作日"
            ).forEach { (type, label) ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // 根据类型显示对应配置
        when (selectedType) {
            ReminderType.WEEKLY, ReminderType.WEEKDAY -> {
                WeekdaySelector(
                    selectedDays = selectedDays,
                    onDayToggle = onDayToggle
                )
            }

            ReminderType.MONTHLY -> {
                OutlinedTextField(
                    value = selectedDayOfMonth,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toIntOrNull() in 1..31) {
                            onDayOfMonthChange(newValue)
                        }
                    },
                    label = { Text("每月第几天") },
                    placeholder = { Text("1-31") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(160.dp)
                )
            }

            ReminderType.INTERVAL -> {
                OutlinedTextField(
                    value = intervalMinutes,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toLongOrNull() != null) {
                            onIntervalChange(newValue)
                        }
                    },
                    label = { Text("间隔（分钟）") },
                    placeholder = { Text("例如: 30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(200.dp)
                )
            }

            else -> { /* ONCE/DAILY 无额外配置 */ }
        }
    }
}
