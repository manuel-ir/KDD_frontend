package com.kdd.kdd_frontend.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kdd.kdd_frontend.R
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.AuthState
import com.kdd.kdd_frontend.viewmodel.AuthViewModel

private const val WEB_CLIENT_ID = "1045671394809-32or29g8gilr4gs2rm29j2o58u9mkujh.apps.googleusercontent.com"

/**
 * Pantalla de inicio de sesion.
 *
 * Muestra un formulario de email y contrasena directamente en el card.
 * Tambien permite autenticarse con Google como alternativa.
 * Enlace a registro para nuevos usuarios.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.mensaje

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            onLoginSuccess()
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val googleIdToken = account?.idToken
                if (googleIdToken != null) {
                    val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                authTask.result?.user?.getIdToken(true)
                                    ?.addOnSuccessListener { tokenResult ->
                                        val firebaseToken = tokenResult.token
                                        android.util.Log.d("KDD_AUTH", "Firebase token OK: ${firebaseToken?.take(20)}...")
                                        if (firebaseToken != null) {
                                            authViewModel.loginConGoogle(context, firebaseToken)
                                        } else {
                                            android.util.Log.e("KDD_AUTH", "Firebase token null")
                                            authViewModel.resetState()
                                        }
                                    }
                                    ?.addOnFailureListener { e ->
                                        android.util.Log.e("KDD_AUTH", "getIdToken fallo: ${e.message}")
                                        authViewModel.resetState()
                                    }
                            } else {
                                android.util.Log.e("KDD_AUTH", "signInWithCredential fallo: ${authTask.exception?.message}")
                                authViewModel.resetState()
                            }
                        }
                } else {
                    android.util.Log.e("KDD_AUTH", "googleIdToken null")
                    authViewModel.resetState()
                }
            } catch (e: ApiException) {
                android.util.Log.e("KDD_AUTH", "ApiException: ${e.statusCode} - ${e.message}")
                authViewModel.resetState()
            }
        } else {
            android.util.Log.e("KDD_AUTH", "resultCode no OK: ${result.resultCode}")
            authViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(KddPurpleDark, KddPurple, Color(0xFF9B6BF2))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_kdd),
                    contentDescription = "Logo KDD",
                    modifier = Modifier.size(70.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "KDD",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tarjeta principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Bienvenido a KDD",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = KddTextPrimary
                    )
                    Text(
                        text = "Inicia sesion para descubrir planes y conectar con personas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KddTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Campo email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (authState is AuthState.Error) authViewModel.resetState()
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
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Campo contrasena
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (authState is AuthState.Error) authViewModel.resetState()
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
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank() && !isLoading) {
                                authViewModel.loginConEmail(context, email.trim(), password)
                            }
                        })
                    )

                    // Error
                    if (!errorMessage.isNullOrBlank()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFEBEE)) {
                            Text(
                                text = errorMessage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB71C1C),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Boton Entrar
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            authViewModel.loginConEmail(context, email.trim(), password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (email.isNotBlank() && password.isNotBlank()) KddPurple else KddDivider
                        ),
                        enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Entrar", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
                                authViewModel.resetState()
                                onNavigateToRegister()
                            }
                        )
                    }

                    // Divisor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = KddDivider)
                        Text(
                            " o ",
                            style = MaterialTheme.typography.labelSmall,
                            color = KddTextSecondary
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = KddDivider)
                    }

                    // Boton Google
                    OutlinedButton(
                        onClick = {
                            authViewModel.resetState()
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(WEB_CLIENT_ID)
                                .requestEmail()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KddTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KddDivider),
                        enabled = !isLoading
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFF4285F4), RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Continuar con Google", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
