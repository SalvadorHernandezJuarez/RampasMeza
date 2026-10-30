package com.example.rampasmezaapp.ui.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

data class ItemVentaUI(
    val id: String,
    val fecha: String,
    val vendedor: String,
    val total: Double?,
    val items: List<Map<String, Any?>>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialVentas(onBackToMainClick: () -> Unit) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var ventas by remember { mutableStateOf<List<ItemVentaUI>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val moneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")) }

    LaunchedEffect(Unit) {
        try {
            val snap = firestore.collection("ventas").orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING).get().await()
            val lista = snap.documents.map { doc ->
                val fecha = doc.getString("fecha") ?: ""
                val total = doc.getDouble("total") ?: doc.getLong("total")?.toDouble()
                val items = (doc.get("items") as? List<*>)?.mapNotNull { rawItem ->
                    val item = rawItem as? Map<*, *> ?: return@mapNotNull null
                    item.entries.mapNotNull { entry ->
                        (entry.key as? String)?.let { key -> key to entry.value }
                    }.toMap()
                } ?: emptyList()
                ItemVentaUI(doc.id, fecha, doc.getString("vendedor") ?: "", total, items)
            }
            ventas = lista
        } catch (e: Exception) {
            error = e.message
        } finally {
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Ventas", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.rampasmezaapp.R.drawable.logo_rampas_meza),
                contentDescription = "Logo Rampas Meza",
                modifier = Modifier
                    .size(180.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))
            if (cargando) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = "Error: $error", color = Color.Red)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(ventas) { venta ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Fecha: ${venta.fecha}")
                                if (venta.vendedor.isNotBlank()) Text("Vendedor: ${venta.vendedor}")
                                Text("Total: ${venta.total?.let(moneda::format) ?: "no disponible (venta anterior a la actualización)"}")
                                Spacer(Modifier.height(8.dp))
                                venta.items.forEach { item ->
                                    val nombre = item["nombre"] ?: "Producto"
                                    val cantidad = item["cantidad"] ?: 0
                                    val precio = (item["precioUnitario"] as? Number)?.toDouble()
                                    val subtotal = (item["subtotal"] as? Number)?.toDouble()
                                    Text("- $nombre x$cantidad" + (precio?.let { " — ${moneda.format(it)} c/u" } ?: ""))
                                    if (subtotal != null) Text("  Importe: ${moneda.format(subtotal)}")
                                }
                                if (venta.total != null && venta.items.all { it["precioUnitario"] is Number }) {
                                    Button(
                                    onClick = {
                                        try {
                                            val receiptLines = venta.items.map { item ->
                                                val cantidad = (item["cantidad"] as? Number)?.toInt() ?: 0
                                                val unit = (item["precioUnitario"] as? Number)?.toDouble() ?: 0.0
                                                val subtotal = (item["subtotal"] as? Number)?.toDouble() ?: unit * cantidad
                                                ItemRecibo(
                                                    productoId = item["productoId"] as? String ?: "",
                                                    nombre = item["nombre"] as? String ?: "Producto",
                                                    cantidad = cantidad,
                                                    precioUnitario = unit,
                                                    subtotal = subtotal
                                                )
                                            }
                                            compartirComprobante(
                                                context,
                                                ReciboVenta(venta.id, venta.fecha, venta.vendedor, receiptLines, venta.total)
                                            )
                                        } catch (e: Exception) {
                                            error = "No se pudo compartir el comprobante: ${e.message}"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF049749))
                                    ) { Text("Compartir PDF", color = Color.White, fontSize = 14.sp) }
                                } else {
                                    Text("No hay datos de precio para generar el comprobante.")
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = onBackToMainClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Ir a Menú Principal", color = Color.Black)
                }
            }
        }
    }
}
