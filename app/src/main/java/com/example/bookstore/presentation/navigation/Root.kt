package com.example.bookstore.presentation.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookstore.domain.model.UserSession
import com.example.bookstore.presentation.screen.AboutScreen
import com.example.bookstore.presentation.screen.AuthScreen
import com.example.bookstore.presentation.screen.BookDetailScreen
import com.example.bookstore.presentation.screen.CheckoutScreen
import com.example.bookstore.presentation.screen.FaqScreen
import com.example.bookstore.presentation.screen.OrderHistoryScreen
import com.example.bookstore.presentation.screen.ProfileScreen
import com.example.bookstore.presentation.screen.SearchResultsScreen
import com.example.bookstore.presentation.screen.SearchScreen
import com.example.bookstore.presentation.screen.SettingsScreen
import com.example.bookstore.presentation.viewmodel.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

private fun NavHostController.navigateSafe(route: String) {
    val entry = currentBackStackEntry ?: return
    if (!entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return

    navigate(route) {
        launchSingleTop = true
    }
}

@Composable
fun Root(
    session: UserSession?,
    modifier: Modifier = Modifier
) {
    if (session == null) {
        AuthScreen(modifier = modifier)
        return
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isOnline = rememberNetworkState(context)

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            snackbarHostState.showSnackbar(
                message = "Нет подключения к интернету. Вы вошли по сохранённой сессии."
            )
        }
    }

    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = koinViewModel()
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Search,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Search) {
                SearchScreen(
                    onOpenSearchResults = { query ->
                        val normalized = query.trim()
                        if (normalized.isNotBlank()) {
                            navController.navigate("${Routes.SearchResults}/${Uri.encode(normalized)}")
                        }
                    },
                    onOpenBook = { id -> navController.navigate("${Routes.Book}/$id") },
                    onOpenProfile = { navController.navigateSafe(Routes.Profile) },
                    onOpenSettings = { navController.navigateSafe(Routes.Settings) },
                    onOpenCheckout = { navController.navigateSafe(Routes.Checkout) }
                )
            }
            composable(
                route = "${Routes.SearchResults}/{query}",
                arguments = listOf(navArgument("query") { type = NavType.StringType })
            ) { backStackEntry ->
                val query = Uri.decode(backStackEntry.arguments?.getString("query").orEmpty())
                SearchResultsScreen(
                    initialQuery = query,
                    onBack = { navController.popBackStack() },
                    onOpenBook = { id -> navController.navigate("${Routes.Book}/$id") }
                )
            }
            composable(Routes.Profile) {
                ProfileScreen(
                    onOpenBook = { id -> navController.navigate("${Routes.Book}/$id") },
                    onOpenOrders = { navController.navigateSafe(Routes.Orders) },
                    viewModel = profileViewModel
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenFaq = { navController.navigateSafe(Routes.Faq) },
                    onOpenAbout = { navController.navigateSafe(Routes.About) }
                )
            }
            composable(Routes.About) { AboutScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.Faq) { FaqScreen(onBack = { navController.popBackStack() }) }
            composable(route = "${Routes.Book}/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id").orEmpty()
                BookDetailScreen(bookId = id, onBack = { navController.popBackStack() }, onCheckout = { navController.navigateSafe(Routes.Checkout) })
            }
            composable(Routes.Checkout) {
                CheckoutScreen(onBack = { navController.popBackStack() }, onDone = { navController.popBackStack(Routes.Search, false) })
            }
            composable(Routes.Orders) {
                OrderHistoryScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = profileViewModel
                )
            }
        }
    }
}

@Composable
private fun rememberNetworkState(context: Context): Boolean {
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var online by remember { mutableStateOf(connectivityManager.isOnline()) }

    LaunchedEffect(connectivityManager, mainHandler) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mainHandler.post { online = true }
            }

            override fun onLost(network: Network) {
                mainHandler.post { online = connectivityManager.isOnline() }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        online = connectivityManager.isOnline()

        awaitCancellationWithUnregister {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }

    return online
}

private fun ConnectivityManager.isOnline(): Boolean {
    val network = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

private suspend fun awaitCancellationWithUnregister(onCancel: () -> Unit) {
    try {
        kotlinx.coroutines.awaitCancellation()
    } finally {
        onCancel()
    }
}
