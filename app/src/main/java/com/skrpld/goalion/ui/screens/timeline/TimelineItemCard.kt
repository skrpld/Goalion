package com.skrpld.goalion.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skrpld.goalion.domain.entities.GoalWithTasks

@Composable
fun TimelineItemCard(
    item: TimelineGoalItem,
    width: Dp,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(width)
            .wrapContentHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (item.data.goal.status) Color(0xFFE8F5E9) else Color(0xFFE3F2FD))
            .border(1.dp, if (item.data.goal.status) Color(0xFF4CAF50) else Color(0xFF2196F3), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(
            text = item.data.goal.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        if (item.isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            if (item.data.tasks.isEmpty()) {
                Text("No tasks", fontSize = 10.sp)
            } else {
                item.data.tasks.forEach { task ->
                    Text("• ${task.title}", fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}