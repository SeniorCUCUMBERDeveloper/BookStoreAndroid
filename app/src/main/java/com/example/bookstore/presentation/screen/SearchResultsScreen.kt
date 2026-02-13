package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.ui.BookRowItem
import com.example.bookstore.presentation.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    initialQuery: String,
    onBack: () -> Unit,
    onOpenBook: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel()
) {
    val ui by viewModel.state.collectAsState()
    val results by viewModel.results.collectAsState()

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank() && ui.query != initialQuery) {
            viewModel.setQuery(initialQuery)
            viewModel.search()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ui.query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Поиск по названию или автору") },
                    singleLine = true
                )
                IconButton(onClick = viewModel::search, enabled = !ui.loading) {
                    Icon(Icons.Default.Search, null)
                }
            }

            if (ui.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (ui.error != null) {
                Text(ui.error ?: "", color = MaterialTheme.colorScheme.error)
            }

            if (ui.searched && !ui.loading && results.isEmpty()) {
                Text("Ничего не нашлось", style = MaterialTheme.typography.titleMedium)
            }

            if (results.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(results) { book ->
                        BookRowItem(book = book, onClick = { onOpenBook(book.id) })
                    }
                }
            }
        }
    }
}
