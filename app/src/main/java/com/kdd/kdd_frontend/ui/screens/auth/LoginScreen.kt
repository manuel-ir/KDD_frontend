package com.kdd.kdd_frontend.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.AuthState
import com.kdd.kdd_frontend.viewmodel.AuthViewModel

private const val WEB_CLIENT_ID = "1045671394809-32or29g8gilr4gs2rm29j2o58u9mkujh.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val authState by authViewModel.authState.collectAsState()

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.mensaje

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
                                        android.util.Log.e("KDD_AUTH", "getIdToken falló: ${e.message}")
                                        authViewModel.resetState()
                                    }
                            } else {
                                android.util.Log.e("KDD_AUTH", "signInWithCredential falló: ${authTask.exception?.message}")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4A90D9),
                            Color(0xFFE05252),
                            Color(0xFF3A3A3A)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = KddYellow
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "KDD",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "KDD",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; authViewModel.resetState() },
                    placeholder = { Text("Correo electrónico", color = Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KddPurple,
                        unfocusedBorderColor = Color(0xFF9E9E9E)
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; authViewModel.resetState() },
                    placeholder = { Text("Contraseña", color = Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KddPurple,
                        unfocusedBorderColor = Color(0xFF9E9E9E)
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = KddError,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KddYellow),
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Iniciar sesión", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = KddDivider)
                    Text("  o  ", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = KddDivider)
                }

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
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KddTextPrimary),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF4285F4), RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuar con Google", fontWeight = FontWeight.Medium)
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = KddTextSecondary)) { append("¿No tienes cuenta? ") }
                                withStyle(SpanStyle(color = KddPurple, fontWeight = FontWeight.SemiBold)) { append("Regístrate") }
                            }
                        )
                    }
                }
            }
        }
    }
}
