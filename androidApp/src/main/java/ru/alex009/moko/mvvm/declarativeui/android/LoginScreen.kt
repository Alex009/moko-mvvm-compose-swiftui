package ru.alex009.moko.mvvm.declarativeui.android

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.icerock.moko.mvvm.flow.compose.observeAsActions
import ru.alex009.moko.mvvm.declarativeui.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
    val context: Context = LocalContext.current

    val login: String by viewModel.login.collectAsState()
    val password: String by viewModel.password.collectAsState()
    val isLoading: Boolean by viewModel.isLoading.collectAsState()
    val isLoginButtonEnabled: Boolean by viewModel.isButtonEnabled.collectAsState()

    viewModel.actions.observeAsActions { action ->
        when (action) {
            LoginViewModel.Action.LoginSuccess ->
                Toast.makeText(context, "login success!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = login,
            enabled = !isLoading,
            label = { Text(text = "Login") },
            onValueChange = { viewModel.login.value = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            enabled = !isLoading,
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { viewModel.password.value = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = isLoginButtonEnabled,
            onClick = viewModel::onLoginPressed
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text(text = "Login")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen()
}
