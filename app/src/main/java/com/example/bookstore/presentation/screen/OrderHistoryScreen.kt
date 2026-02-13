package com.example.bookstore.presentation.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookstore.domain.model.Order
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

private fun canCancel(order: Order): Boolean =
    !order.status.equals("Отменён", ignoreCase = true) &&
        !order.status.equals("Выполнен", ignoreCase = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val ui = viewModel.state.collectAsState().value
    val orders = viewModel.orders.collectAsState(initial = emptyList()).value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История заказов") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        if (orders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(Dimens.screenPadding),
                verticalArrangement = Arrangement.Center
            ) {
                Text("У вас пока нет заказов", style = MaterialTheme.typography.titleMedium)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = orders, key = { it.id }) { order ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(Dimens.cardRadius)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Заказ #${order.id.take(8)}", style = MaterialTheme.typography.titleMedium)
                    Text("Дата: ${order.createdAt}")
                    Text("Статус: ${order.status}")
                    Text("Оплата: ${order.paymentStatus}")
                    Text("Сумма: ${order.totalRub} ₽")

                    if (order.items.isNotEmpty()) {
                        Text(
                            text = order.items.joinToString { "${it.title} ×${it.quantity}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (canCancel(order)) {
                        Button(
                            onClick = { viewModel.cancelOrder(order.id) },
                            enabled = ui.cancellingOrderId != order.id
                        ) {
                            Text(if (ui.cancellingOrderId == order.id) "Отмена..." else "Отменить заказ")
                        }
                    }
                }
            }
        }
    }
}
