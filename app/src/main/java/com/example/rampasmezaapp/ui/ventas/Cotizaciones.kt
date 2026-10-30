package com.example.rampasmezaapp.ui.ventas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rampasmezaapp.data.Producto
import com.example.rampasmezaapp.ui.inventario.DialogoSeleccionProducto
import com.example.rampasmezaapp.ui.inventario.ViewModelInventarioGlobal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class LineaCotizacion(
    val productoId: String,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)

private data class CotizacionUi(
    val id: String,
    val fecha: String,
    val estado: String,
    val total: Double,
    val vendedor: String,
    val items: List<Map<String, Any?>>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cotizaciones(
    inventarioGlobalViewModel: ViewModelInventarioGlobal,
    nombreVendedor: String,
    esAdministrador: Boolean,
    onBack: () -> Unit
) {
    val inventario by inventarioGlobalViewModel.estadoGlobal.collectAsState()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val moneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")) }
    var cotizaciones by remember { mutableStateOf<List<CotizacionUi>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var nueva by remember { mutableStateOf(false) }
    var selector by remember { mutableStateOf(false) }
    var seleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf("") }
    val lineas = remember { mutableStateListOf<LineaCotizacion>() }

    suspend fun cargarCotizaciones() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val docs = firestore.collection("cotizaciones").get().await()
        val ahora = System.currentTimeMillis()
        docs.documents.forEach { doc ->
            val expiresAt = doc.getLong("expiresAt") ?: Long.MAX_VALUE
            if (doc.getString("estado") == "Pendiente" && expiresAt <= ahora) {
                doc.reference.update("estado", "Expirada").await()
            }
        }
        cotizaciones = docs.documents.map { doc ->
            CotizacionUi(
                doc.id,
                doc.getString("fecha") ?: "",
                if ((doc.getLong("expiresAt") ?: Long.MAX_VALUE) <= ahora && doc.getString("estado") == "Pendiente") "Expirada"
                else doc.getString("estado") ?: "Pendiente",
                doc.getDouble("total") ?: 0.0,
                doc.getString("vendedor") ?: "",
                @Suppress("UNCHECKED_CAST")
                (doc.get("items") as? List<Map<String, Any?>>) ?: emptyList()
            )
        }.sortedByDescending { it.fecha }
        if (uid.isBlank()) error = "Sesión inválida"
    }

    LaunchedEffect(Unit) {
        cargando = true
        try {
            cargarCotizaciones()
        } catch (e: Exception) {
            error = "No se pudieron cargar las cotizaciones: ${e.message}"
        } finally {
            cargando = false
        }
    }

    fun refrescar() {
        scope.launch {
            try {
                cargarCotizaciones()
            } catch (e: Exception) {
                error = "No se pudo actualizar: ${e.message}"
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Cotizaciones") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Button(
                onClick = {
                    lineas.clear()
                    nueva = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF049749))
            ) { Text("Crear cotización", color = Color.White) }
            error?.let { Text(it, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp)) }
            if (cargando) Text("Cargando...")
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cotizaciones, key = { it.id }) { cotizacion ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Folio: ${cotizacion.id}", fontSize = 14.sp)
                            Text("Fecha: ${cotizacion.fecha}")
                            Text("Estado: ${cotizacion.estado}", color = Color(0xFF049749))
                            cotizacion.items.forEach { item ->
                                Text("${item["nombre"] ?: "Producto"} x${item["cantidad"] ?: 0}")
                            }
                            Text("Total: ${moneda.format(cotizacion.total)}")
                            Button(onClick = {
                                try {
                                    val lineasPdf = cotizacion.items.map { item ->
                                        val cantidad = (item["cantidad"] as? Number)?.toInt() ?: 0
                                        val precio = (item["precioUnitario"] as? Number)?.toDouble() ?: 0.0
                                        ItemRecibo(
                                            productoId = item["productoId"] as? String ?: "",
                                            nombre = item["nombre"] as? String ?: "Producto",
                                            cantidad = cantidad,
                                            precioUnitario = precio,
                                            subtotal = (item["subtotal"] as? Number)?.toDouble() ?: precio * cantidad
                                        )
                                    }
                                    compartirCotizacionPdf(
                                        context,
                                        ReciboVenta(
                                            cotizacion.id,
                                            cotizacion.fecha,
                                            cotizacion.vendedor,
                                            lineasPdf,
                                            cotizacion.total
                                        )
                                    )
                                } catch (e: Exception) {
                                    error = "No se pudo compartir el PDF: ${e.message}"
                                }
                            }) { Text("Compartir cotización PDF") }
                            if (cotizacion.estado == "Pendiente" && esAdministrador) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        scope.launch {
                                            try {
                                                firestore.collection("cotizaciones").document(cotizacion.id)
                                                    .update("estado", "Aprobada").await()
                                                refrescar()
                                            } catch (e: Exception) {
                                                error = "No se pudo aprobar: ${e.message}"
                                            }
                                        }
                                    }) {
                                        Text("Aprobar")
                                    }
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    firestore.collection("cotizaciones").document(cotizacion.id)
                                                        .update("estado", "Rechazada").await()
                                                    refrescar()
                                                } catch (e: Exception) {
                                                    error = "No se pudo rechazar: ${e.message}"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                                    ) { Text("Rechazar") }
                                }
                            }
                            if (cotizacion.estado == "Aprobada") {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                cargando = true
                                                convertirCotizacion(
                                                    cotizacion.id,
                                                    nombreVendedor,
                                                    firestore,
                                                    inventarioGlobalViewModel
                                                )
                                                try {
                                                    cargarCotizaciones()
                                                } catch (e: Exception) {
                                                    error = "Venta registrada; no se pudo actualizar la lista: ${e.message}"
                                                }
                                            } catch (e: Exception) {
                                                error = e.message ?: "No se pudo convertir la cotización"
                                            } finally {
                                                cargando = false
                                            }
                                        }
                                    }
                                ) { Text("Convertir en venta") }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) { Text("Volver al menú", color = Color.Black) }
        }
    }

    if (nueva) {
        AlertDialog(
            onDismissRequest = { nueva = false },
            title = { Text("Nueva cotización") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { selector = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(seleccionado?.name ?: "Seleccionar producto")
                    }
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it.filter(Char::isDigit) },
                        label = { Text("Cantidad") },
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val producto = seleccionado
                            val qty = cantidad.toIntOrNull()
                            if (producto == null || qty == null || qty <= 0) {
                                error = "Selecciona un producto e ingresa una cantidad válida"
                            } else if (producto.price <= 0.0) {
                                error = "Configura primero el precio del producto"
                            } else {
                                lineas.add(
                                    LineaCotizacion(
                                        producto.id,
                                        producto.name,
                                        qty,
                                        producto.price,
                                        redondear(producto.price * qty)
                                    )
                                )
                                seleccionado = null
                                cantidad = ""
                                error = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Agregar producto") }
                    lineas.forEach { Text("${it.nombre} x${it.cantidad} — ${moneda.format(it.subtotal)}") }
                    Text("Total: ${moneda.format(lineas.sumOf { it.subtotal })}")
                    Text("La cotización expira en 30 días.", color = Color.DarkGray)
                }
            },
            confirmButton = {
                Button(
                    enabled = lineas.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            try {
                                val usuario = FirebaseAuth.getInstance().currentUser
                                    ?: throw IllegalStateException("Debes iniciar sesión")
                                val ahora = System.currentTimeMillis()
                                val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ahora))
                                firestore.collection("cotizaciones").add(
                                    mapOf(
                                        "fecha" to fecha,
                                        "createdAt" to FieldValue.serverTimestamp(),
                                        "expiresAt" to ahora + 30L * 24 * 60 * 60 * 1000,
                                        "estado" to "Pendiente",
                                        "items" to lineas.map {
                                            mapOf(
                                                "productoId" to it.productoId,
                                                "nombre" to it.nombre,
                                                "cantidad" to it.cantidad,
                                                "precioUnitario" to it.precioUnitario,
                                                "subtotal" to it.subtotal
                                            )
                                        },
                                        "total" to redondear(lineas.sumOf { it.subtotal }),
                                        "createdBy" to usuario.uid,
                                        "vendedor" to nombreVendedor
                                    )
                                ).await()
                                nueva = false
                                lineas.clear()
                                try {
                                    cargarCotizaciones()
                                } catch (e: Exception) {
                                    error = "Cotización guardada; no se pudo actualizar la lista: ${e.message}"
                                }
                            } catch (e: Exception) {
                                error = "No se pudo guardar la cotización: ${e.message}"
                            }
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = { Button(onClick = { nueva = false }) { Text("Cancelar") } }
        )
    }

    if (selector) {
        DialogoSeleccionProducto(
            productos = inventario.productos,
            onProductSelected = {
                seleccionado = it
                selector = false
            },
            onDismiss = { selector = false }
        )
    }
}

private suspend fun convertirCotizacion(
    cotizacionId: String,
    nombreVendedor: String,
    firestore: FirebaseFirestore,
    inventarioGlobalViewModel: ViewModelInventarioGlobal
) {
    val usuario = FirebaseAuth.getInstance().currentUser
        ?: throw IllegalStateException("Debes iniciar sesión")
    val quoteRef = firestore.collection("cotizaciones").document(cotizacionId)
    val saleRef = firestore.collection("ventas").document()
    val result = firestore.runTransaction { tx ->
        val quote = tx.get(quoteRef)
        if (quote.getString("estado") != "Aprobada") throw IllegalStateException("Solo se pueden convertir cotizaciones aprobadas")
        val expiresAt = quote.getLong("expiresAt") ?: 0L
        if (expiresAt <= System.currentTimeMillis()) throw IllegalStateException("La cotización ya expiró")
        if (quote.getString("ventaId") != null) throw IllegalStateException("Esta cotización ya fue convertida")
        val items = @Suppress("UNCHECKED_CAST")
        (quote.get("items") as? List<Map<String, Any?>>).orEmpty()
        val cantidades = items.groupBy { item ->
            item["productoId"] as? String
                ?: throw IllegalStateException("Producto inválido en la cotización")
        }.mapValues { (_, repetidos) ->
            val total = repetidos.sumOf { (it["cantidad"] as? Number)?.toLong() ?: 0L }
            if (total <= 0L || total > Int.MAX_VALUE) {
                throw IllegalStateException("Cantidad inválida en la cotización")
            }
            total.toInt()
        }
        val lecturas = cantidades.map { (id, cantidad) ->
            val ref = firestore.collection("inventory").document(id)
            Triple(ref, cantidad, tx.get(ref))
        }
        val cambios = lecturas.map { (ref, cantidad, snap) ->
            val stock = snap.getLong("currentStock")?.toInt() ?: 0
            if (stock < cantidad) throw IllegalStateException("Stock insuficiente para ${snap.id}")
            Pair(ref, stock - cantidad)
        }
        val ventaItems = items
        val total = quote.getDouble("total") ?: 0.0
        val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date())
        cambios.forEach { (ref, stock) ->
            tx.update(ref, mapOf("currentStock" to stock, "lastSaleId" to saleRef.id))
        }
        tx.set(
            saleRef,
            mapOf(
                "fecha" to fecha,
                "items" to ventaItems,
                "total" to total,
                "createdBy" to usuario.uid,
                "vendedor" to nombreVendedor,
                "cotizacionId" to cotizacionId,
                "cantidades" to cantidades
            )
        )
        tx.update(quoteRef, mapOf("estado" to "Convertida", "ventaId" to saleRef.id))
        cambios.associate { it.first.id to it.second }
    }.await()
    result.forEach { (id, stock) -> inventarioGlobalViewModel.actualizarStockProducto(id, stock) }
}

private fun redondear(valor: Double): Double = kotlin.math.round(valor * 100.0) / 100.0
