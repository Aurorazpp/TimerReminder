package com.example.timereminder.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.timereminder.domain.model.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 提醒卡片组件
 */
@Composable
fun ReminderCard(
    reminder: Reminder,
    tagColor: Color? = null,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 标签颜色标记
            if (tagColor != null) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 48.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(tagColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (reminder.isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 时间 + 重复规则
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${reminder.hour.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )

                    if (reminder.isRecurring()) {
                        Text(
                            text = "· ${reminder.getRepeatDescription()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (reminder.isEnabled) 1f else 0.38f
                            )
                        )
                    }
                }

                // 备注
                if (!reminder.description.isNullOrBlank()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (reminder.isEnabled) 0.7f else 0.3f
                        )
                    )
                }

                // 提醒方式图标
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (reminder.isNotificationEnabled) {
                        IconWithAlpha(
                            icon = Icons.Default.Notifications,
                            contentDescription = "通知",
                            alpha = if (reminder.isEnabled) 0.6f else 0.2f
                        )
                    }
                    if (reminder.isRingEnabled) {
                        IconWithAlpha(
                            icon = Icons.Default.VolumeUp,
                            contentDescription = "响铃",
                            alpha = if (reminder.isEnabled) 0.6f else 0.2f
                        )
                    }
                    if (reminder.isVibrationEnabled) {
                        IconWithAlpha(
                            icon = Icons.Default.Vibration,
                            contentDescription = "震动",
                            alpha = if (reminder.isEnabled) 0.6f else 0.2f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 启用开关
            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun IconWithAlpha(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    alpha: Float
) {
    Box(modifier = Modifier.size(16.dp)) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        )
    }
}
