package com.ritmo.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritmo.app.data.HabitCheckIn
import com.ritmo.app.ui.components.EmptyState
import com.ritmo.app.ui.components.HabitColorDot
import com.ritmo.app.ui.components.RitmoScreen
import com.ritmo.app.ui.components.SectionHeader
import com.ritmo.app.ui.components.StatCard

@Composable
fun HabitDetailRoute(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onArchived: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is HabitDetailEvent.Archived) onArchived()
        }
    }

    HabitDetailScreen(
        habitId = viewModel.habitId,
        uiState = uiState,
        onBack = onBack,
        onEdit = onEdit,
        onArchive = viewModel::archiveHabit,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDetailScreen(
    habitId: String,
    uiState: HabitDetailUiState,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onArchive: () -> Unit,
) {
    var showArchiveDialog by remember { mutableStateOf(false) }
    val detail = uiState.detail

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(habitId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showArchiveDialog = true }) {
                        Icon(Icons.Filled.Archive, contentDescription = "Arquivar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        if (detail == null && !uiState.isLoading) {
            EmptyState(
                title = "Hábito não encontrado",
                body = "Esse hábito pode ter sido arquivado ou removido.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        } else if (detail != null) {
            RitmoScreen(contentPadding = paddingValues) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                ) {
                    item {
                        HabitHeader(
                            title = detail.habit.title,
                            description = detail.habit.description,
                            color = detail.habit.color,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                label = "Sequência atual",
                                value = "${detail.currentStreak} dias",
                                icon = Icons.Filled.LocalFireDepartment,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                label = "Check-ins",
                                value = detail.checkIns.size.toString(),
                                icon = Icons.Filled.EventAvailable,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        SectionHeader(
                            title = "Histórico recente",
                            subtitle = "Últimos registros salvos localmente.",
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    if (detail.checkIns.isEmpty()) {
                        item {
                            EmptyState(
                                title = "Sem check-ins",
                                body = "Marque esse hábito na tela Hoje para criar histórico.",
                                icon = Icons.Filled.CalendarMonth,
                            )
                        }
                    } else {
                        items(detail.checkIns.take(14), key = { it.id }) { checkIn ->
                            CheckInRow(checkIn = checkIn)
                        }
                    }
                }
            }
        }
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Arquivar hábito?") },
            text = { Text("Ele sairá da tela Hoje, mas o histórico continuará preservado localmente.") },
            confirmButton = {
                Button(onClick = onArchive) {
                    Text("Arquivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun HabitHeader(
    title: String,
    description: String,
    color: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            HabitColorDot(
                color = color,
                modifier = Modifier.padding(top = 6.dp),
            )
            Column(Modifier.padding(start = 14.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckInRow(checkIn: HabitCheckIn) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.EventAvailable,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "${checkIn.date.dayOfMonth}/${checkIn.date.monthNumber}/${checkIn.date.year}",
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Text(
                text = checkIn.syncState.name.lowercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
