package com.example.bookstore.presentation.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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

private enum class AppTab(val route: String) {
    Home(Routes.Search),
    Checkout(Routes.Checkout),
    Settings(Routes.Settings),
    Profile(Routes.Profile)
}

private fun appTabFromRoute(route: String): AppTab = when (route) {
    Routes.Search -> AppTab.Home
    Routes.Checkout -> AppTab.Checkout
    Routes.Settings -> AppTab.Settings
    Routes.Profile -> AppTab.Profile
    else -> AppTab.Home
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


    var currentTab by remember { mutableStateOf(AppTab.Home) }

    val homeNavController = rememberNavController()
    val checkoutNavController = rememberNavController()
    val settingsNavController = rememberNavController()
    val profileNavController = rememberNavController()

    val profileViewModel: ProfileViewModel = koinViewModel()

    BackHandler {
        val currentController = when (currentTab) {
            AppTab.Home -> homeNavController
            AppTab.Checkout -> checkoutNavController
            AppTab.Settings -> settingsNavController
            AppTab.Profile -> profileNavController
        }

        val atTabRoot = currentController.currentDestination?.route == currentTab.route
        if (atTabRoot) {
            if (currentTab != AppTab.Home) currentTab = AppTab.Home
            return@BackHandler
        }

        currentController.popBackStack()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BookStoreBottomBar(
                currentRoute = currentTab.route,
                onNavigate = { route -> currentTab = appTabFromRoute(route) }
            )
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.Home -> {
                HomeTabNavHost(
                    navController = homeNavController,
                    modifier = Modifier.padding(innerPadding),
                    onOpenCheckoutTab = { currentTab = AppTab.Checkout }
                )
            }

            AppTab.Checkout -> {
                CheckoutTabNavHost(
                    navController = checkoutNavController,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppTab.Settings -> {
                SettingsTabNavHost(
                    navController = settingsNavController,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppTab.Profile -> {
                ProfileTabNavHost(
                    navController = profileNavController,
                    modifier = Modifier.padding(innerPadding),
                    profileViewModel = profileViewModel,
                    onOpenCheckoutTab = { currentTab = AppTab.Checkout }
                )
            }
        }
    }
}

@Composable
private fun HomeTabNavHost(
    navController: NavHostController,
    modifier: Modifier,
    onOpenCheckoutTab: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Search,
        modifier = modifier
    ) {
        composable(Routes.Search) {
            SearchScreen(
                onOpenSearchResults = { query ->
                    val normalized = query.trim()
                    if (normalized.isNotBlank()) {
                        navController.navigate("${Routes.SearchResults}/${Uri.encode(normalized)}")
                    }
                },
                onOpenBook = { id -> navController.navigate("${Routes.Book}/$id") }
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
        composable(
            route = "${Routes.Book}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            BookDetailScreen(
                bookId = id,
                onBack = { navController.popBackStack() },
                onCheckout = onOpenCheckoutTab
            )
        }
    }
}

@Composable
private fun ProfileTabNavHost(
    navController: NavHostController,
    modifier: Modifier,
    profileViewModel: ProfileViewModel,
    onOpenCheckoutTab: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Profile,
        modifier = modifier
    ) {
        composable(Routes.Profile) {
            ProfileScreen(
                onOpenBook = { id -> navController.navigate("${Routes.Book}/$id") },
                onOpenOrders = { navController.navigate(Routes.Orders) },
                viewModel = profileViewModel
            )
        }
        composable(
            route = "${Routes.Book}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            BookDetailScreen(
                bookId = id,
                onBack = { navController.popBackStack() },
                onCheckout = onOpenCheckoutTab
            )
        }
        composable(Routes.Orders) {
            OrderHistoryScreen(
                onBack = { navController.popBackStack() },
                viewModel = profileViewModel
            )
        }
    }
}

@Composable
private fun SettingsTabNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Settings,
        modifier = modifier
    ) {
        composable(Routes.Settings) {
            SettingsScreen(
                onOpenFaq = { navController.navigate(Routes.Faq) },
                onOpenAbout = { navController.navigate(Routes.About) }
            )
        }
        composable(Routes.About) { AboutScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.Faq) { FaqScreen(onBack = { navController.popBackStack() }) }
    }
}

@Composable
private fun CheckoutTabNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Checkout,
        modifier = modifier
    ) {
        composable(Routes.Checkout) {
            CheckoutScreen()
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
