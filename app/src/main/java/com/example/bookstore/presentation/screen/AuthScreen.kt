package com.example.bookstore.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bookstore.presentation.theme.Dimens
import com.example.bookstore.presentation.viewmodel.AuthMode
import com.example.bookstore.presentation.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.message) {
        if (state.message != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.blockSpacing)
    ) {
        Spacer(Modifier.height(14.dp))
        Text(
            text = "BookStore",
            style = MaterialTheme.typography.headlineLarge
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.mode == AuthMode.Login) {
                Button(onClick = { viewModel.setMode(AuthMode.Login) }) {
                    Text("Вход")
                }
                OutlinedButton(onClick = { viewModel.setMode(AuthMode.Register) }) {
                    Text("Регистрация")
                }
            } else {
                OutlinedButton(onClick = { viewModel.setMode(AuthMode.Login) }) {
                    Text("Вход")
                }
                Button(onClick = { viewModel.setMode(AuthMode.Register) }) {
                    Text("Регистрация")
                }
            }
        }

        if (state.mode == AuthMode.Register) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Имя") }
            )
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::setEmail,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::setPassword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        if (state.message != null) {
            Text(
                text = state.message ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = viewModel::submit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (state.mode == AuthMode.Login) "Войти" else "Создать аккаунт")
            }
        }

        TextButton(
            onClick = viewModel::resetPassword,
            modifier = Modifier.align(Alignment.Start),
            enabled = !state.loading
        ) {
            Text("Восстановить пароль")
        }
    }
}
