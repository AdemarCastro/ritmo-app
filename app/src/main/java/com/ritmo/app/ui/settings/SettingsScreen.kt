package com.ritmo.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.ritmo.app.data.preferences.ThemeMode
import com.ritmo.app.ui.components.RitmoScreen
import com.ritmo.app.ui.components.SectionHeader

@Composable
fun SettingsRoute(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    SettingsScreen(
        contentPadding = contentPadding,
        themeMode = themeMode,
        onThemeModeChange = viewModel::setThemeMode,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    contentPadding: PaddingValues,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionHeader(
                    title = "Aparência",
                    subtitle = "A preferência é salva localmente com DataStore.",
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeModeChip(
                                label = "Sistema",
                                selected = themeMode == ThemeMode.SYSTEM,
                                icon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null) },
                                onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                            )
                            ThemeModeChip(
                                label = "Claro",
                                selected = themeMode == ThemeMode.LIGHT,
                                icon = { Icon(Icons.Filled.LightMode, contentDescription = null) },
                                onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                            )
                            ThemeModeChip(
                                label = "Escuro",
                                selected = themeMode == ThemeMode.DARK,
                                icon = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
                                onClick = { onThemeModeChange(ThemeMode.DARK) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModeChip(
    label: String,
    selected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon,
    )
}
