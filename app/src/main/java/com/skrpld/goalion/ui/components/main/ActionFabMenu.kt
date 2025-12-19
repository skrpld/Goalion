package com.skrpld.goalion.ui.components.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skrpld.goalion.ui.screens.main.MainViewModel

@Composable
fun ActionFabMenu(
    selectedTarget: MainViewModel.ActionTarget?,
    pinnedGoalIds: Set<Int>,
    onAddGoal: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPriority: () -> Unit,
    onPin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = selectedTarget != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallFabWithStyle(onClick = onDelete, icon = Icons.Default.Delete, isError = true)
                SmallFabWithStyle(onClick = onEdit, icon = Icons.Default.Edit)
                SmallFabWithStyle(onClick = onPriority, icon = Icons.Default.PriorityHigh)

                if (selectedTarget is MainViewModel.ActionTarget.GoalTarget) {
                    val isPinned = pinnedGoalIds.contains(selectedTarget.goal.id)
                    SmallFabWithStyle(
                        onClick = onPin,
                        icon = if (isPinned) Icons.Default.PushPin else Icons.Default.PushPin, // Можно менять иконку
                        isError = false,
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        GoalionCard(
            onClick = onAddGoal,
            modifier = Modifier.size(56.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}

@Composable
fun SmallFabWithStyle(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false,
    tint: androidx.compose.ui.graphics.Color? = null
) {
    GoalionCard(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = tint ?: (if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}