package com.ritmo.app.ui.today

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritmo.app.data.TodayHabit
import com.ritmo.app.ui.components.EmptyState
import com.ritmo.app.ui.components.HabitColorDot
import com.ritmo.app.ui.components.RitmoScreen
import com.ritmo.app.ui.components.SectionHeader
import com.ritmo.app.ui.components.SyncStatusChip

@Composable
fun TodayRoute(
    contentPadding: PaddingValues,
    onAddHabit: () -> Unit,
    onHabitClick: (String) -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onAddHabit = onAddHabit,
        onHabitClick = onHabitClick,
        onToggleHabit = viewModel::toggleHabit,
        onSync = viewModel::requestSync,
        onMessageShown = viewModel::messageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScreen(
    contentPadding: PaddingValues,
    uiState: TodayUiState,
    onAddHabit: () -> Unit,
    onHabitClick: (String) -> Unit,
    onToggleHabit: (String) -> Unit,
    onSync: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        val message = uiState.userMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ritmo", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Hábitos de hoje",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    SyncStatusChip(
                        status = uiState.syncStatus,
                        onClick = onSync,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddHabit,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo hábito") },
            )
        },
        modifier = Modifier.padding(contentPadding),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        RitmoScreen(
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                end = 20.dp,
                bottom = 0.dp,
            ),
        ) {
            TodayHero(
                completedCount = uiState.completedCount,
                totalCount = uiState.totalCount,
                progress = uiState.progress,
            )
            Spacer(Modifier.height(22.dp))
            SectionHeader(
                title = "Rotina",
                subtitle = if (uiState.totalCount == 0) {
                    "Monte uma rotina simples para acompanhar todos os dias."
                } else {
                    "Toque no círculo para registrar o check-in de hoje."
                },
            )
            Spacer(Modifier.height(12.dp))
            if (uiState.habits.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    title = "Comece com um hábito",
                    body = "Adicione algo pequeno e repetível, como beber água ou estudar por 20 minutos.",
                    icon = Icons.Filled.Flag,
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 112.dp),
                ) {
                    items(uiState.habits, key = { it.habit.id }) { item ->
                        HabitCard(
                            item = item,
                            onClick = { onHabitClick(item.habit.id) },
                            onToggle = { onToggleHabit(item.habit.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayHero(
    completedCount: Int,
    totalCount: Int,
    progress: Float,
) {
    val percent = (progress * 100).toInt()
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Progresso do dia",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$completedCount de $totalCount concluídos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(18.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
private fun HabitCard(
    item: TodayHabit,
    onClick: () -> Unit,
    onToggle: () -> Unit,
) {
    val containerColor = if (item.checkedInToday) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HabitColorDot(color = item.habit.color)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 10.dp),
            ) {
                Text(
                    text = item.habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (item.habit.description.isBlank()) {
                        if (item.checkedInToday) "Check-in registrado" else "Pendente hoje"
                    } else {
                        item.habit.description
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (item.checkedInToday) {
                FilledIconButton(onClick = onToggle) {
                    Icon(Icons.Filled.Check, contentDescription = "Concluído")
                }
            } else {
                OutlinedIconButton(onClick = onToggle) {
                    Icon(Icons.Filled.RadioButtonUnchecked, contentDescription = "Marcar")
                }
            }
        }
    }
}
