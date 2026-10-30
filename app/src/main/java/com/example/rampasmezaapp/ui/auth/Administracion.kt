package com.example.rampasmezaapp.ui.auth

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
import com.example.rampasmezaapp.data.RolUsuario
import com.example.rampasmezaapp.ui.inventario.ViewModelInventarioGlobal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class PerfilUsuario(val id: String, val nombre: String, val email: String, val rol: RolUsuario)
private data class CambioPrecio(
    val producto: String,
    val anterior: Double,
    val nuevo: Double,
    val usuario: String,
    val fecha: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Administracion(
    inventarioGlobalViewModel: ViewModelInventarioGlobal,
    nombreAdministrador: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val inventario by inventarioGlobalViewModel.estadoGlobal.collectAsState()
    var perfiles by remember { mutableStateOf<List<PerfilUsuario>>(emptyList()) }
    var producto by remember { mutableStateOf<Producto?>(null) }
    var menuProducto by remember { mutableStateOf(false) }
    var precio by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var historialPrecios by remember { mutableStateOf<List<CambioPrecio>>(emptyList()) }
    val moneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")) }

    suspend fun cargarPerfiles() {
        perfiles = firestore.collection("users").get().await().documents.map { doc ->
            PerfilUsuario(
                doc.id,
                doc.getString("name") ?: "Usuario",
                doc.getString("email") ?: "",
                RolUsuario.desde(doc.getString("role"))
            )
        }
    }

    suspend fun cargarHistorialPrecios() {
        historialPrecios = firestore.collection("price_history")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(30)
            .get().await().documents.map { doc ->
                val fecha = (doc.get("createdAt") as? Timestamp)?.toDate()
                CambioPrecio(
                    producto = doc.getString("producto") ?: "",
                    anterior = doc.getDouble("precioAnterior") ?: 0.0,
                    nuevo = doc.getDouble("precioNuevo") ?: 0.0,
                    usuario = doc.getString("usuario") ?: "",
                    fecha = fecha?.let {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                    } ?: "Fecha pendiente"
                )
            }
    }

    LaunchedEffect(Unit) {
        try {
            cargarPerfiles()
            cargarHistorialPrecios()
        } catch (e: Exception) {
            error = "No se pudieron cargar los usuarios: ${e.message}"
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Administración") }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Perfiles y roles", fontSize = 20.sp)
                Text("Administra los accesos del equipo.")
            }
            items(perfiles, key = { it.id }) { perfil ->
                var expandido by remember(perfil.rol) { mutableStateOf(false) }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(perfil.nombre)
                        Text(perfil.email, color = Color.DarkGray)
                        Button(
                            onClick = { expandido = true },
                            enabled = perfil.id != FirebaseAuth.getInstance().currentUser?.uid
                        ) { Text("Rol: ${perfil.rol.etiqueta}") }
                        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                            RolUsuario.entries.forEach { rol ->
                                DropdownMenuItem(text = { Text(rol.etiqueta) }, onClick = {
                                    expandido = false
                                    scope.launch {
                                        try {
                                            firestore.collection("users").document(perfil.id)
                                                .update("role", rol.etiqueta).await()
                                            try {
                                                cargarPerfiles()
                                            } catch (e: Exception) {
                                                error = "Rol actualizado; no se pudo refrescar la lista: ${e.message}"
                                            }
                                        } catch (e: Exception) {
                                            error = "No se pudo actualizar el rol: ${e.message}"
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Precios de productos", fontSize = 20.sp)
                Text("El precio nuevo se aplicará a las ventas futuras; las anteriores conservarán su precio.")
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Historial de precios", fontSize = 20.sp)
            }
            items(historialPrecios) { cambio ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(cambio.producto)
                        Text("${moneda.format(cambio.anterior)} → ${moneda.format(cambio.nuevo)}")
                        Text("${cambio.usuario} · ${cambio.fecha}", color = Color.DarkGray)
                    }
                }
            }
            item {
                Button(onClick = { menuProducto = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(producto?.name ?: "Seleccionar producto")
                }
                DropdownMenu(expanded = menuProducto, onDismissRequest = { menuProducto = false }) {
                    inventario.productos.forEach { item ->
                        DropdownMenuItem(text = { Text(item.name) }, onClick = {
                            producto = item
                            precio = if (item.price > 0) item.price.toString() else ""
                            menuProducto = false
                        })
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Precio unitario (MXN)") },
                    singleLine = true
                )
                error?.let { Text(it, color = Color.Red) }
                Button(
                    enabled = !cargando && producto != null && precio.toDoubleOrNull()?.let { it > 0 } == true,
                    onClick = {
                        val seleccionado = producto ?: return@Button
                        val nuevoPrecio = precio.toDoubleOrNull() ?: return@Button
                        cargando = true
                        error = null
                        scope.launch {
                            try {
                                val usuario = FirebaseAuth.getInstance().currentUser
                                    ?: throw IllegalStateException("Debes iniciar sesión")
                                val referencia = firestore.collection("inventory").document(seleccionado.id)
                                val historial = firestore.collection("price_history").document()
                                val precioAnterior = firestore.runTransaction { tx ->
                                    val doc = tx.get(referencia)
                                    val anterior = doc.getDouble("currentPrice") ?: seleccionado.price
                                    tx.update(referencia, "currentPrice", nuevoPrecio)
                                    tx.set(
                                        historial,
                                        mapOf(
                                            "productoId" to seleccionado.id,
                                            "producto" to seleccionado.name,
                                            "precioAnterior" to anterior,
                                            "precioNuevo" to nuevoPrecio,
                                            "changedBy" to usuario.uid,
                                            "usuario" to nombreAdministrador,
                                            "createdAt" to FieldValue.serverTimestamp()
                                        )
                                    )
                                    anterior
                                }.await()
                                inventarioGlobalViewModel.actualizarPrecioProducto(seleccionado.id, nuevoPrecio)
                                Toast.makeText(context, "Precio actualizado desde $precioAnterior", Toast.LENGTH_SHORT).show()
                                try {
                                    cargarHistorialPrecios()
                                } catch (e: Exception) {
                                    error = "Precio guardado; no se pudo actualizar el historial: ${e.message}"
                                }
                            } catch (e: Exception) {
                                error = "No se pudo guardar el precio: ${e.message}"
                            } finally {
                                cargando = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (cargando) "Guardando..." else "Guardar precio") }
            }
            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) { Text("Volver", color = Color.Black) }
            }
        }
    }
}
