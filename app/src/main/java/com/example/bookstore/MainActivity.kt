package com.example.bookstore

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.bookstore.domain.model.ThemeMode
import com.example.bookstore.presentation.navigation.Root
import com.example.bookstore.presentation.theme.BookStoreTheme
import com.example.bookstore.presentation.viewmodel.SessionViewModel
import com.example.bookstore.presentation.viewmodel.ThemeLoadState
import com.example.bookstore.presentation.viewmodel.ThemeViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val notificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val askedCount = prefs.getInt(KEY_NOTIFICATIONS_PERMISSION_ASKED_COUNT, 0)
            prefs.edit()
                .putInt(KEY_NOTIFICATIONS_PERMISSION_ASKED_COUNT, askedCount + 1)
                .putLong(KEY_NOTIFICATIONS_PERMISSION_LAST_ASKED_AT, SystemClock.elapsedRealtime())
                .apply()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermissionOnFirstLaunchIfNeeded()
        setContent {
            val themeViewModel: ThemeViewModel = koinViewModel()
            val themeLoadState by themeViewModel.themeLoadState.collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()

            when (val state = themeLoadState) {
                ThemeLoadState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ThemeLoadState.Ready -> {
                    val darkTheme = when (state.themeMode) {
                        ThemeMode.SYSTEM -> systemDarkTheme
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                    }
                    BookStoreTheme(darkTheme = darkTheme) {
                        val sessionViewModel: SessionViewModel = koinViewModel()
                        val sessionState by sessionViewModel.sessionState.collectAsState()
                        Root(sessionState = sessionState)
                    }
                }
            }
        }
    }

    private fun requestNotificationsPermissionOnFirstLaunchIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) return

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val askedCount = prefs.getInt(KEY_NOTIFICATIONS_PERMISSION_ASKED_COUNT, 0)
        val lastAskedAt = prefs.getLong(KEY_NOTIFICATIONS_PERMISSION_LAST_ASKED_AT, 0L)
        val shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        val reaskCooldownPassed = (SystemClock.elapsedRealtime() - lastAskedAt) >= NOTIFICATIONS_REASK_COOLDOWN_MS

        val canAskNow = askedCount == 0 || (shouldShowRationale && reaskCooldownPassed)
        if (canAskNow) {
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private companion object {
        const val PREFS_NAME = "bookstore_prefs"
        const val KEY_NOTIFICATIONS_PERMISSION_ASKED_COUNT = "notifications_permission_asked_count"
        const val KEY_NOTIFICATIONS_PERMISSION_LAST_ASKED_AT = "notifications_permission_last_asked_at"

        const val NOTIFICATIONS_REASK_COOLDOWN_MS = 7L * 24 * 60 * 60 * 1000
    }
}
