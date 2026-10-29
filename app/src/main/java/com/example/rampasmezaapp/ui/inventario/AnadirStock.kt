package com.example.rampasmezaapp.ui.inventario

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rampasmezaapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnadirStock(
    inventarioGlobalViewModel: ViewModelInventarioGlobal,
    onBackToMainClick: () -> Unit
) {
    val viewModel = remember { ViewModelInventario(inventarioGlobalViewModel) }
    val state by viewModel.estadoInventario.collectAsState()
    val estadoGlobal by inventarioGlobalViewModel.estadoGlobal.collectAsState()
    var showProductList by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Añadir Stock",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_rampas_meza),
                contentDescription = "Logo Rampas Meza",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showProductList = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF049749)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = state.productoSeleccionado?.name ?: "Seleccionar producto",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF049749)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ingresa la cantidad a añadir al stock actual",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.cantidadStock,
                onValueChange = { viewModel.actualizarCantidadStock(it) },
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
                placeholder = { Text("Cantidad a añadir") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            state.mensajeError?.let { mensajeError ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = mensajeError,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            state.mensajeExito?.let { mensajeExito ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF049749).copy(alpha = 0.1f))
                ) {
                    Text(
                        text = mensajeExito,
                        color = Color(0xFF049749),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { viewModel.anadirStock() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.cargando && state.productoSeleccionado != null && state.cantidadStock.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = "Añadir Stock del\nproducto",
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBackToMainClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Ir a Menú Principal", fontSize = 14.sp, color = Color.Black)
            }
        }
    }

    if (showProductList) {
        DialogoSeleccionProducto(
            productos = estadoGlobal.productos,
            onProductSelected = { producto ->
                viewModel.seleccionarProducto(producto)
            },
            onDismiss = { showProductList = false }
        )
    }
}
