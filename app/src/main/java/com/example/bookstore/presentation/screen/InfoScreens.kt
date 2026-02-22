package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.bookstore.domain.model.FaqItem
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.viewmodel.FaqViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) = InfoScreenTemplate("О нас", onBack, modifier) {
    Text("BookStore — это не просто магазин книг, а пространство для тех, кто по-настоящему любит читать.")
    Text("Мы понимаем это чувство, когда хороший роман заканчивается слишком быстро, в дороге не хватает времени дочитать главу, а от истории не хочется отрываться. С книгами любимых авторов время летит незаметно — и мы хотим, чтобы такие моменты случались у вас как можно чаще.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FaqViewModel = koinViewModel()
) {
    val items by viewModel.items.collectAsState()
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQ") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockSpacing)
        ) {
            if (state.loading && items.isEmpty()) {
                item { CircularProgressIndicator() }
            }

            if (state.error != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::syncFaqOnce) {
                            Text("Повторить")
                        }
                    }
                }
            }

            items(items, key = { it.id }) { faq ->
                FaqCard(item = faq)
            }
        }
    }
}

@Composable
private fun FaqCard(item: FaqItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)
        ) {
            Text(text = item.question, style = MaterialTheme.typography.titleMedium)
            Text(text = item.answer, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoScreenTemplate(title: String, onBack: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockSpacing)
        ) { content() }
    }
}
