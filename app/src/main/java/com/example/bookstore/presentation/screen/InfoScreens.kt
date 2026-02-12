package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bookstore.presentation.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit, modifier: Modifier = Modifier) = InfoScreenTemplate("О нас", onBack, modifier) {
    Text("BookStore — это не просто магазин книг, а пространство для тех, кто по-настоящему любит читать.")
    Text("Мы понимаем это чувство, когда хороший роман заканчивается слишком быстро, в дороге не хватает времени дочитать главу, а от истории не хочется отрываться. С книгами любимых авторов время летит незаметно — и мы хотим, чтобы такие моменты случались у вас как можно чаще.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(onBack: () -> Unit, modifier: Modifier = Modifier) = InfoScreenTemplate("FAQ", onBack, modifier) {
    Text("По всем вопросам обращайтесь на почту: support@bookstore.app")
    Text("Оплата заказов сейчас доступна только наличными при получении.")
    Text("Заказы принимаются ежедневно с 09:00 до 21:00.")
    Text("Обычно доставка занимает 1–3 дня в зависимости от адреса.")
    Text("Отменить заказ можно до передачи в доставку через экран Профиль → Заказы.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoScreenTemplate(title: String, onBack: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
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
