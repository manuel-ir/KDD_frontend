package com.kdd.kdd_frontend.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.AuthState
import com.kdd.kdd_frontend.viewmodel.AuthViewModel

/**
 * Pantalla de inicio de sesion con email y contrasena.
 *
 * Alternativa al login con Google, pensada para la defensa del TFG
 * donde los asistentes pueden usar cualquier correo (incluso inventado).
 */
@Composable
fun EmailLoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.resetState()
                onNavigateBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = KddTextPrimary)
            }
            Text(
                "Iniciar sesion",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Introduce tu correo y contrasena",
                style = MaterialTheme.typography.bodyMedium,
                color = KddTextSecondary
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text("Correo electronico") },
                placeholder = { Text("ejemplo@correo.com", color = KddTextHint) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = KddTextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KddPurple,
                    unfocusedBorderColor = KddDivider
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text("Contrasena") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = KddTextSecondary) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = KddTextSecondary
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KddPurple,
                    unfocusedBorderColor = KddDivider
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (email.isNotBlank() && password.isNotBlank()) {
                        viewModel.loginConEmail(context, email.trim(), password)
                    }
                })
            )

            val errorMsg = (authState as? AuthState.Error)?.mensaje
            if (!errorMsg.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFEBEE)) {
                    Text(
                        text = errorMsg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB71C1C),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Enlace a registro
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = buildAnnotatedString {
                        append("No tienes cuenta? ")
                        withStyle(SpanStyle(color = KddPurple, fontWeight = FontWeight.SemiBold)) {
                            append("Registrate")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        onNavigateToRegister()
                    }
                )
            }
        }

        val formValido = email.isNotBlank() && password.isNotBlank()
        val cargando = authState is AuthState.Loading

        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Button(
                onClick = {
                    viewModel.loginConEmail(context, email.trim(), password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (formValido && !cargando) KddPurple else KddDivider
                ),
                enabled = formValido && !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Entrar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}
