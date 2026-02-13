package com.example.bookstore

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.bookstore.presentation.navigation.Root
import com.example.bookstore.presentation.theme.BookStoreTheme
import com.example.bookstore.presentation.viewmodel.SessionViewModel
import com.example.bookstore.presentation.viewmodel.ThemeViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val notificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationsPermissionOnFirstLaunchIfNeeded()
        setContent {
            val themeViewModel: ThemeViewModel = koinViewModel()
            val darkTheme by themeViewModel.darkTheme.collectAsState(initial = isSystemInDarkTheme())
            BookStoreTheme(darkTheme = darkTheme) {
                val sessionViewModel: SessionViewModel = koinViewModel()
                val session by sessionViewModel.session.collectAsState()
                Root(session = session)
            }
        }
    }

    private fun requestNotificationsPermissionOnFirstLaunchIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val alreadyRequested = prefs.getBoolean(KEY_NOTIFICATIONS_PERMISSION_REQUESTED, false)
        if (alreadyRequested) return

        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_PERMISSION_REQUESTED, true).apply()
            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private companion object {
        const val PREFS_NAME = "bookstore_prefs"
        const val KEY_NOTIFICATIONS_PERMISSION_REQUESTED = "notifications_permission_requested"
    }
}
