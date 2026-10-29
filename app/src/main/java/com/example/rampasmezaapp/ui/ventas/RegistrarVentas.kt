package com.example.rampasmezaapp.ui.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rampasmezaapp.R
import com.example.rampasmezaapp.ui.inventario.DialogoSeleccionProducto
import com.example.rampasmezaapp.ui.inventario.ViewModelInventarioGlobal
import com.example.rampasmezaapp.data.Producto
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarVentas(
    inventarioGlobalViewModel: ViewModelInventarioGlobal,
    onBackToMainClick: () -> Unit
) {
    val estadoGlobal by inventarioGlobalViewModel.estadoGlobal.collectAsState()
    val viewModelVentas = remember { ViewModelVentas() }
    val estadoVentas by viewModelVentas.estado.collectAsState()

    var mostrarSelector by remember { mutableStateOf(false) }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidadVendida by remember { mutableStateOf("") }
    var itemsVenta by remember { mutableStateOf(listOf<Pair<Producto, Int>>()) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }

    // Limpiar UI cuando el ViewModel reporte éxito
    LaunchedEffect(estadoVentas.mensajeExito) {
        if (estadoVentas.mensajeExito != null) {
            itemsVenta = emptyList()
            productoSeleccionado = null
            cantidadVendida = ""
            mensajeExito = estadoVentas.mensajeExito
        }
    }

    // Mostrar errores provenientes del ViewModel
    LaunchedEffect(estadoVentas.mensajeError) {
        if (estadoVentas.mensajeError != null) {
            mensajeError = estadoVentas.mensajeError
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Registrar Ventas", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.rampasmezaapp.R.drawable.logo_rampas_meza),
                contentDescription = "Logo Rampas Meza",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            // Se eliminó el cuadro verde de título solicitado
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { mostrarSelector = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF049749)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = productoSeleccionado?.name ?: "Seleccionar producto", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF049749)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ingresa la cantidad vendida",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cantidadVendida,
                onValueChange = { cantidadVendida = it },
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
                placeholder = { Text("Cantidad vendida") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    mensajeError = null
                    mensajeExito = null
                    val qty = cantidadVendida.toIntOrNull()
                    val prod = productoSeleccionado
                    if (prod == null) {
                        mensajeError = "Selecciona un producto"
                    } else if (qty == null || qty <= 0) {
                        mensajeError = "Ingresa una cantidad válida"
                    } else if (qty > prod.currentStock) {
                        mensajeError = "Stock insuficiente. Disponible: ${prod.currentStock}"
                    } else {
                        itemsVenta = itemsVenta + (prod to qty)
                        viewModelVentas.agregarItem(prod, qty)
                        // reset selección para añadir otro fácilmente
                        productoSeleccionado = null
                        cantidadVendida = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Agregar producto", color = Color.Black)
            }

            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                    Text(mensajeError!!, color = Color.Red, modifier = Modifier.padding(12.dp))
                }
            }

            if (mensajeExito != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF049749).copy(alpha = 0.12f)), modifier = Modifier.fillMaxWidth()) {
                    Text(mensajeExito!!, color = Color(0xFF049749), modifier = Modifier.padding(12.dp))
                }
            }

            if (itemsVenta.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Productos añadidos a la venta:", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                            items(itemsVenta) { (p, q) ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(p.name)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("x$q")
                                        Spacer(Modifier.width(12.dp))
                                        IconButton(onClick = {
                                            itemsVenta = itemsVenta.filterNot { it.first.id == p.id }
                                            // Además, hay que limpiarlo del viewModelVentas
                                            viewModelVentas.limpiar()
                                            itemsVenta.forEach { (prodRestante, cant) ->
                                                viewModelVentas.agregarItem(prodRestante, cant)
                                            }
                                        }) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    mensajeError = null
                    mensajeExito = null
                    // Si hay una selección pendiente válida, agréguela al carrito antes de registrar
                    val qtyPend = cantidadVendida.toIntOrNull()
                    val prodPend = productoSeleccionado
                    if (prodPend != null && qtyPend != null && qtyPend > 0) {
                        if (qtyPend > prodPend.currentStock) {
                            mensajeError = "Stock insuficiente. Disponible: ${prodPend.currentStock}"
                            return@Button
                        }
                        itemsVenta = itemsVenta + (prodPend to qtyPend)
                        viewModelVentas.agregarItem(prodPend, qtyPend)
                        productoSeleccionado = null
                        cantidadVendida = ""
                    }

                    if (itemsVenta.isEmpty()) {
                        mensajeError = "Agrega al menos un producto"
                        return@Button
                    }
                    viewModelVentas.registrarVenta { cambios ->
                        // Actualizar stock global en memoria para reflejar al instante
                        cambios.forEach { (id, nuevo) ->
                            inventarioGlobalViewModel.actualizarStockProducto(id, nuevo)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (estadoVentas.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                } else {
                    Text("Registrar venta", color = Color.Black)
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
                Text("Ir a Menú Principal", color = Color.Black)
            }
        }
    }

    if (mostrarSelector) {
        DialogoSeleccionProducto(
            productos = estadoGlobal.productos,
            onProductSelected = { productoSeleccionado = it; mostrarSelector = false },
            onDismiss = { mostrarSelector = false }
        )
    }
}


