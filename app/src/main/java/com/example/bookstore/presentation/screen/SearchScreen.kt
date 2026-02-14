package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookstore.domain.model.Book
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.ui.BookSmallCard
import com.example.bookstore.presentation.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenSearchResults: (String) -> Unit,
    onOpenBook: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel()
) {
    val ui by viewModel.state.collectAsState()
    val featuredNew by viewModel.featuredNew.collectAsState()
    val featuredHits by viewModel.featuredHits.collectAsState()
    val featuredBasic by viewModel.featuredBasic.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("BookStore") }
                )
                SearchInputRow(
                    query = ui.query,
                    loading = ui.loading,
                    onQueryChange = viewModel::setQuery,
                    onSubmit = { onOpenSearchResults(ui.query.trim()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.screenPadding)
                        .padding(bottom = 8.dp)
                )
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockSpacing)
        ) {
            if (ui.loading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            if (ui.error != null) {
                item {
                    Text(ui.error ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            if (featuredHits.isNotEmpty()) {
                item {
                    FeaturedSection(
                        title = "Хиты недели",
                        books = featuredHits,
                        onOpenBook = onOpenBook
                    )
                }
            }

            if (featuredNew.isNotEmpty()) {
                item {
                    FeaturedSection(
                        title = "Новинки",
                        books = featuredNew,
                        onOpenBook = onOpenBook
                    )
                }
            }

            if (featuredBasic.isNotEmpty()) {
                item {
                    BasicSelectionSection(
                        books = featuredBasic,
                        onOpenBook = onOpenBook
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInputRow(
    query: String,
    loading: Boolean,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Поиск по названию или автору") },
            singleLine = true
        )
        IconButton(onClick = onSubmit, enabled = !loading) {
            Icon(Icons.Default.Search, null)
        }
    }
}

@Composable
private fun FeaturedSection(
    title: String,
    books: List<Book>,
    onOpenBook: (String) -> Unit
) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(books) { book ->
            BookSmallCard(book = book, onClick = { onOpenBook(book.id) }, modifier = Modifier.width(140.dp))
        }
    }
}

@Composable
private fun BasicSelectionSection(
    books: List<Book>,
    onOpenBook: (String) -> Unit
) {
    val rows = books.chunked(2)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { rowBooks ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowBooks.forEach { book ->
                    BookSmallCard(
                        book = book,
                        onClick = { onOpenBook(book.id) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowBooks.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
