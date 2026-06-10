package com.ritmo.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritmo.app.ui.components.RitmoScreen
import com.ritmo.app.ui.components.SectionHeader
import com.ritmo.app.ui.components.StatCard

@Composable
fun StatsRoute(
    contentPadding: PaddingValues,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    StatsScreen(
        contentPadding = contentPadding,
        totalHabits = stats.totalHabits,
        completedToday = stats.completedToday,
        weeklyCompletionRate = stats.weeklyCompletionRate,
        bestStreak = stats.bestStreak,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsScreen(
    contentPadding: PaddingValues,
    totalHabits: Int,
    completedToday: Int,
    weeklyCompletionRate: Float,
    bestStreak: Int,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        modifier = Modifier.padding(contentPadding),
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        RitmoScreen(contentPadding = paddingValues) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionHeader(
                    title = "Seu ritmo",
                    subtitle = "Resumo calculado a partir dos dados locais.",
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "Hábitos ativos",
                        value = totalHabits.toString(),
                        icon = Icons.Filled.Flag,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Concluídos hoje",
                        value = completedToday.toString(),
                        icon = Icons.Filled.CheckCircle,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "Taxa semanal",
                        value = "${(weeklyCompletionRate * 100).toInt()}%",
                        icon = Icons.Filled.CalendarMonth,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Melhor sequência",
                        value = "$bestStreak dias",
                        icon = Icons.Filled.LocalFireDepartment,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
