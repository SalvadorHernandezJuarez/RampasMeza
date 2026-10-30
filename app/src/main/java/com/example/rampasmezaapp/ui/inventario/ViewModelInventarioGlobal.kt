package com.example.rampasmezaapp.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampasmezaapp.data.Producto
import com.example.rampasmezaapp.data.DatosProductos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EstadoInventarioGlobal(
    val productos: List<Producto> = DatosProductos.productos,
    val cargando: Boolean = true,
    val error: String? = null
)

class ViewModelInventarioGlobal : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser != null) {
            cargarInventarioCompleto()
        }
    }
    
    private val _estadoGlobal = MutableStateFlow(EstadoInventarioGlobal())
    val estadoGlobal: StateFlow<EstadoInventarioGlobal> = _estadoGlobal.asStateFlow()

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }

    private fun cargarInventarioCompleto() {
        viewModelScope.launch {
            try {
                val productosActualizados = mutableListOf<Producto>()
                
                for (producto in DatosProductos.productos) {
                    val docRef = firestore.collection("inventory").document(producto.id)
                    val doc = docRef.get().await()
                    
                    if (!doc.exists()) {
                        docRef.set(mapOf("currentStock" to 0, "currentPrice" to producto.price)).await()
                        productosActualizados.add(producto.copy(currentStock = 0))
                    } else {
                        val stockActual = doc.getLong("currentStock")?.toInt() ?: 0
                        val precio = doc.getDouble("currentPrice") ?: producto.price
                        productosActualizados.add(producto.copy(currentStock = stockActual, price = precio))
                    }
                }
                
                _estadoGlobal.value = EstadoInventarioGlobal(
                    productos = productosActualizados,
                    cargando = false,
                    error = null
                )
            } catch (e: Exception) {
                _estadoGlobal.value = EstadoInventarioGlobal(
                    productos = DatosProductos.productos,
                    cargando = false,
                    error = "Error al cargar inventario: ${e.message}"
                )
            }
        }
    }

    fun actualizarStockProducto(productoId: String, nuevoStock: Int) {
        val productosActualizados = _estadoGlobal.value.productos.map { producto ->
            if (producto.id == productoId) producto.copy(currentStock = nuevoStock) else producto
        }
        _estadoGlobal.value = _estadoGlobal.value.copy(productos = productosActualizados)
    }

    fun actualizarPrecioProducto(productoId: String, nuevoPrecio: Double) {
        val productosActualizados = _estadoGlobal.value.productos.map { producto ->
            if (producto.id == productoId) producto.copy(price = nuevoPrecio) else producto
        }
        _estadoGlobal.value = _estadoGlobal.value.copy(productos = productosActualizados)
    }
}
