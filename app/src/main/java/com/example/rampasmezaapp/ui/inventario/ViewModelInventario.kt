package com.example.rampasmezaapp.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampasmezaapp.data.Producto
import com.example.rampasmezaapp.data.DatosProductos
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.rampasmezaapp.ui.inventario.EmailJSHelper

data class EstadoInventario(
    val productos: List<Producto> = emptyList(),
    val productoSeleccionado: Producto? = null,
    val cantidadStock: String = "",
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

class ViewModelInventario(
    private val viewModelGlobal: ViewModelInventarioGlobal
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _estadoInventario = MutableStateFlow(EstadoInventario())
    val estadoInventario: StateFlow<EstadoInventario> = _estadoInventario.asStateFlow()

    init {
        // Los productos se cargarán desde el composable usando collectAsState
    }

    fun seleccionarProducto(producto: Producto) {
        _estadoInventario.value = _estadoInventario.value.copy(
            productoSeleccionado = producto,
            mensajeError = null,
            mensajeExito = null
        )
    }

    fun actualizarCantidadStock(cantidad: String) {
        _estadoInventario.value = _estadoInventario.value.copy(
            cantidadStock = cantidad,
            mensajeError = null,
            mensajeExito = null
        )
    }

    fun anadirStock() {
        val estado = _estadoInventario.value
        val productoSeleccionado = estado.productoSeleccionado
        val cantidadStock = estado.cantidadStock.toIntOrNull()

        if (productoSeleccionado == null) {
            _estadoInventario.value = estado.copy(mensajeError = "Selecciona un producto")
            return
        }

        if (cantidadStock == null || cantidadStock <= 0) {
            _estadoInventario.value = estado.copy(mensajeError = "Ingresa una cantidad válida")
            return
        }

        viewModelScope.launch {
            _estadoInventario.value = estado.copy(cargando = true, mensajeError = null)
            
            try {
                val docRef = firestore.collection("inventory").document(productoSeleccionado.id)
                val doc = docRef.get().await()
                
                val stockActual = doc.getLong("currentStock")?.toInt() ?: 0
                val nuevoStock = stockActual + cantidadStock
                
                docRef.update("currentStock", nuevoStock).await()
                
                viewModelGlobal.actualizarStockProducto(productoSeleccionado.id, nuevoStock)

                // Enviar alerta por correo si stock bajo
                if (nuevoStock <= 10) {
                    EmailJSHelper.enviarAlertaStockBajo(
                        nombreProducto = productoSeleccionado.name,
                        stock = nuevoStock.toString()
                    )
                }
                
                _estadoInventario.value = estado.copy(
                    cargando = false,
                    mensajeExito = "Stock añadido exitosamente. Nuevo stock: $nuevoStock",
                    cantidadStock = ""
                )
            } catch (e: Exception) {
                _estadoInventario.value = estado.copy(
                    cargando = false,
                    mensajeError = "Error al añadir stock: ${e.message}"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _estadoInventario.value = _estadoInventario.value.copy(
            mensajeError = null,
            mensajeExito = null
        )
    }
}
