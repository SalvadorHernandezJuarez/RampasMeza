package com.example.rampasmezaapp.ui.inventario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.rampasmezaapp.data.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

private data class MovimientoUi(
    val producto: String,
    val tipo: String,
    val cantidad: Int,
    val motivo: String,
    val usuario: String,
    val fecha: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesInventario(
    inventarioGlobalViewModel: ViewModelInventarioGlobal,
    nombreUsuario: String,
    onBack: () -> Unit
) {
    val productos by inventarioGlobalViewModel.estadoGlobal.collectAsState()
    var producto by remember { mutableStateOf<Producto?>(null) }
    var menuProducto by remember { mutableStateOf(false) }
    var cantidad by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("Devolución") }
    var entrada by remember { mutableStateOf(true) }
    var menuMotivo by remember { mutableStateOf(false) }
    var procesando by remember { mutableStateOf(false) }
    var historial by remember { mutableStateOf<List<MovimientoUi>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    suspend fun cargarHistorial() {
        historial = FirebaseFirestore.getInstance().collection("inventory_adjustments")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(25)
            .get().await().documents.map { doc ->
                val fecha = (doc.get("createdAt") as? Timestamp)?.toDate()
                MovimientoUi(
                    doc.getString("producto") ?: "",
                    doc.getString("tipo") ?: "",
                    doc.getLong("cantidad")?.toInt() ?: 0,
                    doc.getString("motivo") ?: "",
                    doc.getString("usuario") ?: "",
                    fecha?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it) }
                        ?: "Fecha pendiente"
                )
            }
    }

    LaunchedEffect(Unit) {
        try {
            cargarHistorial()
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo cargar el historial: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Devoluciones y ajustes") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { menuProducto = true }, modifier = Modifier.fillMaxWidth()) {
                Text(producto?.name ?: "Seleccionar producto")
            }
            DropdownMenu(expanded = menuProducto, onDismissRequest = { menuProducto = false }) {
                productos.productos.forEach { item ->
                    DropdownMenuItem(text = { Text(item.name) }, onClick = {
                        producto = item
                        menuProducto = false
                    })
                }
            }
            Text("Stock actual: ${producto?.currentStock ?: "-"}")
            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cantidad") },
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { entrada = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (entrada) Color(0xFF049749) else Color.LightGray
                    )
                ) { Text("Entrada") }
                Button(
                    onClick = { entrada = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!entrada) Color(0xFF049749) else Color.LightGray
                    )
                ) { Text("Salida") }
            }
            Button(onClick = { menuMotivo = true }) { Text("Motivo: $motivo") }
            DropdownMenu(expanded = menuMotivo, onDismissRequest = { menuMotivo = false }) {
                listOf("Devolución", "Merma", "Pérdida", "Corrección").forEach { opcion ->
                    DropdownMenuItem(text = { Text(opcion) }, onClick = {
                        motivo = opcion
                        menuMotivo = false
                    })
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                enabled = !procesando && producto != null && cantidad.toIntOrNull()?.let { it > 0 } == true,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val seleccionado = producto ?: return@Button
                    val unidades = cantidad.toIntOrNull() ?: return@Button
                    procesando = true
                    scope.launch {
                        try {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                                ?: error("Debes iniciar sesión")
                            val firestore = FirebaseFirestore.getInstance()
                            val stockRef = firestore.collection("inventory").document(seleccionado.id)
                            val movimientoRef = firestore.collection("inventory_adjustments").document()
                            val actualizado = firestore.runTransaction { transaction ->
                                val stockSnap = transaction.get(stockRef)
                                val stockActual = stockSnap.getLong("currentStock")?.toInt() ?: 0
                                val nuevo = stockActual + if (entrada) unidades else -unidades
                                if (nuevo < 0) error("La salida supera el stock disponible")
                                transaction.update(
                                    stockRef,
                                    mapOf("currentStock" to nuevo, "lastAdjustmentId" to movimientoRef.id)
                                )
                                transaction.set(
                                    movimientoRef,
                                    mapOf(
                                        "productoId" to seleccionado.id,
                                        "producto" to seleccionado.name,
                                        "cantidad" to unidades,
                                        "tipo" to if (entrada) "Entrada" else "Salida",
                                        "motivo" to motivo,
                                        "stockAnterior" to stockActual,
                                        "stockNuevo" to nuevo,
                                        "createdBy" to uid,
                                        "usuario" to nombreUsuario,
                                        "createdAt" to FieldValue.serverTimestamp()
                                    )
                                )
                                nuevo
                            }.await()
                            inventarioGlobalViewModel.actualizarStockProducto(seleccionado.id, actualizado)
                            Toast.makeText(context, "Ajuste registrado", Toast.LENGTH_SHORT).show()
                            cantidad = ""
                            try {
                                cargarHistorial()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Ajuste guardado; no se pudo actualizar el historial: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message ?: "No se pudo registrar el ajuste", Toast.LENGTH_LONG).show()
                        } finally {
                            procesando = false
                        }
                    }
                }
            ) {
                Text(if (procesando) "Guardando..." else "Registrar ajuste")
            }
            Text("Movimientos recientes", fontSize = 20.sp)
            historial.forEach { movimiento ->
                androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${movimiento.tipo}: ${movimiento.producto} x${movimiento.cantidad}")
                        Text("Motivo: ${movimiento.motivo}")
                        Text("${movimiento.usuario} · ${movimiento.fecha}", color = Color.DarkGray)
                    }
                }
            }
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) { Text("Volver", color = Color.Black, fontSize = 16.sp) }
        }
    }
}
