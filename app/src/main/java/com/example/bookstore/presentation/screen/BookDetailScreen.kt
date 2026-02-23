package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.ui.BookCover
import com.example.bookstore.presentation.ui.formatPrice
import com.example.bookstore.presentation.ui.formatRating
import com.example.bookstore.presentation.viewmodel.BookDetailViewModel
import com.example.bookstore.domain.model.BookReview
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: BookDetailViewModel = koinViewModel(parameters = { parametersOf(bookId) })
    val book by viewModel.book.collectAsState()
    val description by viewModel.description.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val canReview by viewModel.canReview.collectAsState()
    var myReview by remember { mutableStateOf("") }
    var myRating by remember { mutableFloatStateOf(5f) }
    var buyDialog by remember { mutableStateOf(false) }

    val reviewsCount = reviews.size
    val reviewsAverageRating = if (reviewsCount == 0) 0.0 else reviews.map { it.rating }.average()

    if (buyDialog) {
        AlertDialog(
            onDismissRequest = { buyDialog = false },
            title = { Text("Онлайн-оплата временно недоступна") },
            text = { Text("Книга добавится в корзину, затем можно оформить заказ.") },
            confirmButton = {
                Button(onClick = {
                    buyDialog = false
                    viewModel.addToCart()
                    onCheckout()
                }) { Text("В корзину и оформить") }
            },
            dismissButton = { OutlinedButton(onClick = { buyDialog = false }) { Text("Отмена") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Книга", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        modifier = modifier
    ) { padding ->
        val b = book
        if (b == null) return@Scaffold

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.blockSpacing)
        ) {
            BookCover(imageUrl = b.imageUrl, modifier = Modifier.fillMaxWidth().height(260.dp))
            SelectionContainer {
                Text(b.title, style = MaterialTheme.typography.headlineSmall)
            }
            SelectionContainer {
                Text(b.author, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (reviewsCount == 0) {
                Text("Нет оценок (0)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("${formatRating(reviewsAverageRating)} • $reviewsCount оценок", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatPrice(b.priceRub), style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { viewModel.addToCart() }, modifier = Modifier.weight(1f)) { Text("В корзину") }
                Button(onClick = { buyDialog = true }, modifier = Modifier.weight(1f)) { Text("Купить") }
            }

            DescriptionSection(description = description)
            ReviewsSection(
                canReview = canReview,
                reviews = reviews,
                myReview = myReview,
                myRating = myRating,
                onReviewChange = { myReview = it },
                onRatingChange = { myRating = it },
                onSubmitReview = {
                    viewModel.addReview(myReview, myRating.toInt())
                    myReview = ""
                }
            )

        }
    }
}

@Composable
private fun DescriptionSection(description: String?) {
    val descriptionText = description?.trim().orEmpty()

    Text("Описание", style = MaterialTheme.typography.titleLarge)
    if (descriptionText.isBlank()) {
        Text("Описание отсутствует", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        SelectionContainer {
            Text(descriptionText)
        }
    }
}

@Composable
private fun ReviewsSection(
    canReview: Boolean,
    reviews: List<BookReview>,
    myReview: String,
    myRating: Float,
    onReviewChange: (String) -> Unit,
    onRatingChange: (Float) -> Unit,
    onSubmitReview: () -> Unit
) {
    Text("Отзывы", style = MaterialTheme.typography.titleLarge)

    if (canReview) {
        OutlinedTextField(
            value = myReview,
            onValueChange = onReviewChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Оставить отзыв") }
        )
        Text("Ваша оценка: ${myRating.toInt()} / 5")
        StarRatingInput(
            rating = myRating.toInt(),
            onRatingChange = { onRatingChange(it.toFloat()) }
        )
        Button(onClick = onSubmitReview) { Text("Отправить") }
    } else {
        Text("Вы уже оставили отзыв на эту книгу.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    reviews.forEach { review ->
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = review.userName,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = ratingStars(review.rating),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = review.reviewText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun ratingStars(rating: Int): String {
    val safeRating = rating.coerceIn(0, 5)
    val filled = "★".repeat(safeRating)
    val empty = "☆".repeat(5 - safeRating)
    return "$filled$empty"
}

@Composable
private fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        (1..5).forEach { star ->
            IconButton(onClick = { onRatingChange(star) }) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Оценка $star",
                    tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
