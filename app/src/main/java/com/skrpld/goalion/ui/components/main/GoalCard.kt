package com.skrpld.goalion.ui.components.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.data.models.Goal

@Composable
fun GoalCard(
    goal: Goal,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddSubTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "Priority: ${goal.priority}", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onAddSubTask) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null)
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}