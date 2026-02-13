package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.viewmodel.ThemeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenFaq: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = koinViewModel()
) {
    val darkTheme by themeViewModel.darkTheme.collectAsState(initial = isSystemInDarkTheme())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockSpacing)
        ) {
            Button(onClick = themeViewModel::toggle, modifier = Modifier.fillMaxWidth()) {
                Text(if (darkTheme) "Переключить на светлую тему" else "Переключить на тёмную тему")
            }
            OutlinedButton(onClick = onOpenFaq, modifier = Modifier.fillMaxWidth()) { Text("FAQ") }
            OutlinedButton(onClick = onOpenAbout, modifier = Modifier.fillMaxWidth()) { Text("О нас") }
        }
    }
}
