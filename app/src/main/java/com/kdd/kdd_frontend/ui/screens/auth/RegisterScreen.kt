package com.kdd.kdd_frontend.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.AuthState
import com.kdd.kdd_frontend.viewmodel.AuthViewModel

/**
 * Pantalla de registro con email y contrasena.
 *
 * La contrasena debe tener al menos 9 caracteres, una mayuscula,
 * una minuscula y un numero. Se valida visualmente en tiempo real.
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // Requisitos de contrasena
    val tieneMinCaracteres = password.length >= 9
    val tieneMayuscula = password.any { it.isUpperCase() }
    val tieneMinuscula = password.any { it.isLowerCase() }
    val tieneNumero = password.any { it.isDigit() }
    val contrasenaValida = tieneMinCaracteres && tieneMayuscula && tieneMinuscula && tieneNumero

    val coinciden = password == confirmPassword
    val formValido = nombre.isNotBlank() && email.isNotBlank() && contrasenaValida && coinciden

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Cabecera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.resetState()
                onNavigateToLogin()
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = KddTextPrimary
                )
            }
            Text(
                "Crear cuenta",
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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Nombre de usuario
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text("Nombre de usuario") },
                placeholder = { Text("Tu nombre o alias", color = KddTextHint) },
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = KddTextSecondary)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KddPurple,
                    unfocusedBorderColor = KddDivider
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text("Correo electronico") },
                placeholder = { Text("ejemplo@correo.com", color = KddTextHint) },
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = KddTextSecondary)
                },
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

            // Contrasena
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text("Contrasena") },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = KddTextSecondary)
                },
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
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            // Indicadores de requisitos (solo cuando hay algo escrito)
            if (password.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RequisitoContrasena("Minimo 9 caracteres", tieneMinCaracteres)
                    RequisitoContrasena("Una letra mayuscula (A-Z)", tieneMayuscula)
                    RequisitoContrasena("Una letra minuscula (a-z)", tieneMinuscula)
                    RequisitoContrasena("Un numero (0-9)", tieneNumero)
                }
            }

            // Confirmar contrasena
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (authState is AuthState.Error) viewModel.resetState()
                },
                label = { Text("Confirmar contrasena") },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = KddTextSecondary)
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = KddTextSecondary
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPassword.isNotBlank() && !coinciden,
                supportingText = if (confirmPassword.isNotBlank() && !coinciden) {
                    { Text("Las contrasenas no coinciden", color = Color.Red, style = MaterialTheme.typography.labelSmall) }
                } else null,
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
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            // Error del backend
            val backendError = (authState as? AuthState.Error)?.mensaje
            if (!backendError.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFEBEE)) {
                    Text(
                        text = backendError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB71C1C),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Boton registrar
        val cargando = authState is AuthState.Loading

        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.registrarConEmail(context, nombre.trim(), email.trim(), password)
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Registrar",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Fila que muestra un requisito de contrasena con icono verde (cumplido) o gris (pendiente).
 */
@Composable
private fun RequisitoContrasena(texto: String, cumplido: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (cumplido) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (cumplido) Color(0xFF4CAF50) else KddTextHint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = if (cumplido) Color(0xFF4CAF50) else KddTextHint
        )
    }
}
