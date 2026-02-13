package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.ui.formatPrice
import com.example.bookstore.presentation.viewmodel.CheckoutViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: CheckoutViewModel = koinViewModel()
    val ui by viewModel.state.collectAsState()
    val items by viewModel.items.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ui.message) {
        val m = ui.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(m)
        viewModel.clearMessage()
    }
    val total = items.sumOf { it.book.priceRub * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Оформление заказа") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)
        ) {
            item { Text("Корзина", style = MaterialTheme.typography.titleLarge) }
            if (items.isEmpty()) {
                item { Text("Корзина пуста", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(items) { item ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.book.title)
                            Text(formatPrice(item.book.priceRub), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.decrease(item.book.id, item.quantity) }) { Text("-") }
                            Text(item.quantity.toString(), modifier = Modifier.padding(top = 12.dp))
                            Button(onClick = { viewModel.increase(item.book.id, item.quantity) }) { Text("+") }
                        }
                    }
                }
                item { Text("Итого: ${formatPrice(total)}", style = MaterialTheme.typography.titleMedium) }
            }

            item { OutlinedTextField(ui.name, viewModel::setName, modifier = Modifier.fillMaxWidth(), label = { Text("ФИО") }, singleLine = true) }
            item { OutlinedTextField(ui.phone, viewModel::setPhone, modifier = Modifier.fillMaxWidth(), label = { Text("Телефон") }, singleLine = true) }
            item { OutlinedTextField(ui.email, viewModel::setEmail, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true) }
            item { OutlinedTextField(ui.address, viewModel::setAddress, modifier = Modifier.fillMaxWidth(), label = { Text("Адрес доставки") }) }

            if (ui.orderId != null) {
                item {
                    Text(
                        text = "Заказ успешно оформлен: ${ui.orderId}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ui.loading && ui.orderId == null
                ) {
                    if (ui.loading) CircularProgressIndicator() else Text("Подтвердить заказ")
                }
            }
            item { Spacer(Modifier.padding(8.dp)) }
        }
    }
}
