package com.example.rampasmezaapp.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.rampasmezaapp.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verificationKey by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var verificationKeyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        Image(
            painter = painterResource(id = R.drawable.logo_rampas_meza),
            contentDescription = "Logo Rampas Meza",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        
        Spacer(modifier = Modifier.height(32.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF049749)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Crear nueva cuenta",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF049749)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Ingrese su usuario",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color(0xFFE0E0E0),
                unfocusedContainerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF049749)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Crea una contraseña segura",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color(0xFFE0E0E0),
                unfocusedContainerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = Color.Black
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF049749)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Ingrese la clave de Verificación",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        OutlinedTextField(
            value = verificationKey,
            onValueChange = { verificationKey = it },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color(0xFFE0E0E0),
                unfocusedContainerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (verificationKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { verificationKeyVisible = !verificationKeyVisible }) {
                    Icon(
                        imageVector = if (verificationKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (verificationKeyVisible) "Ocultar clave" else "Mostrar clave",
                        tint = Color.Black
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))


        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
            ) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }


        val isVerificationKeyValid = verificationKey == "19852025"
        val isFormValid = email.isNotBlank() && password.isNotBlank() && verificationKey.isNotBlank() && isVerificationKeyValid

        if (verificationKey.isNotBlank() && !isVerificationKeyValid) {
            Text(
                text = "Clave de verificación incorrecta",
                color = Color.Red,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }


        Button(
            onClick = { onRegisterClick(email, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && isFormValid,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black
                )
            } else {
                Text("Registrarse", fontSize = 16.sp, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Ir a Login", fontSize = 14.sp, color = Color.Black)
        }
    }
}
