package com.example.timereminder.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 星期名称（周一=0 ~ 周日=6） */
val WEEK_DAY_SHORT_NAMES = listOf("一", "二", "三", "四", "五", "六", "日")

/**
 * 星期选择器
 * @param selectedDays 已选中的星期索引集合（0=周一 ~ 6=周日）
 * @param onDayToggle 点击切换回调
 */
@Composable
fun WeekdaySelector(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "选择星期",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WEEK_DAY_SHORT_NAMES.forEachIndexed { index, dayName ->
                val isSelected = index in selectedDays
                FilterChip(
                    selected = isSelected,
                    onClick = { onDayToggle(index) },
                    label = {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
