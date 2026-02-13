package com.example.bookstore.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

private data class NavBarDestination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

private val bottomDestinations = listOf(
    NavBarDestination(
        route = Routes.Search,
        label = "Главная",
        icon = { Icon(Icons.Default.Home, contentDescription = "Главная") }
    ),
    NavBarDestination(
        route = Routes.Checkout,
        label = "Корзина",
        icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Корзина") }
    ),
    NavBarDestination(
        route = Routes.Settings,
        label = "Настройки",
        icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") }
    ),
    NavBarDestination(
        route = Routes.Profile,
        label = "Профиль",
        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Профиль") }
    )
)

@Composable
internal fun BookStoreBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = destination.icon,
                label = { Text(destination.label) }
            )
        }
    }
}
