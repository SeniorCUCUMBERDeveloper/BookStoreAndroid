package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookstore.BuildConfig
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.ui.BookSmallCard
import com.example.bookstore.presentation.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenBook: (String) -> Unit,
    onOpenOrders: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val ui = viewModel.state.collectAsState().value
    val viewed = viewModel.viewed.collectAsState().value
    val ordersCount = viewModel.orders.collectAsState(initial = emptyList()).value.size
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ui.message) {
        val m = ui.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(m)
        viewModel.clearMessage()
    }

    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = viewModel::logout) { Icon(Icons.Default.ExitToApp, null) }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockSpacing)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Личные данные", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = ui.name,
                        onValueChange = viewModel::setName,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Имя") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ui.phone,
                        onValueChange = viewModel::setPhone,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Телефон") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ui.deliveryAddress,
                        onValueChange = viewModel::setDeliveryAddress,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Адрес доставки") }
                    )
                    if (ui.saving) {
                        Text("Сохранение…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (ui.email.isNotBlank()) Text(ui.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (BuildConfig.DEBUG) Text("Debug build", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                Button(onClick = onOpenOrders) {
                    Text(if (ordersCount > 0) "История заказов ($ordersCount)" else "История заказов")
                }
            }

            if (viewed.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Просмотренные товары", style = MaterialTheme.typography.titleLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(viewed) { book ->
                                BookSmallCard(book = book, onClick = { onOpenBook(book.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}
