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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ItemVentaUI(val fecha: String, val total: Double?, val items: List<Map<String, Any?>>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialVentas(onBackToMainClick: () -> Unit) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    var ventas by remember { mutableStateOf<List<ItemVentaUI>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val snap = firestore.collection("ventas").orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING).get().await()
            val lista = snap.documents.map { doc ->
                val fecha = doc.getString("fecha") ?: ""
                val total = doc.getDouble("total")
                val items = (doc.get("items") as? List<Map<String, Any?>>) ?: emptyList()
                ItemVentaUI(fecha, total, items)
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
                                if (venta.total != null) Text("Total: ${venta.total}")
                                Spacer(Modifier.height(8.dp))
                                venta.items.forEach { item ->
                                    val nombre = item["nombre"] ?: "Producto"
                                    val cantidad = item["cantidad"] ?: 0
                                    Text("- $nombre x$cantidad")
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


