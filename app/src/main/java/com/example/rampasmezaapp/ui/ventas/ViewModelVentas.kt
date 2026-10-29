package com.example.rampasmezaapp.ui.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampasmezaapp.data.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.rampasmezaapp.ui.inventario.EmailJSHelper

data class ItemVenta(val producto: Producto, val cantidad: Int)

data class EstadoVentas(
    val items: List<ItemVenta> = emptyList(),
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

class ViewModelVentas : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _estado = MutableStateFlow(EstadoVentas())
    val estado: StateFlow<EstadoVentas> = _estado.asStateFlow()

    fun agregarItem(producto: Producto, cantidad: Int) {
        _estado.value = _estado.value.copy(items = _estado.value.items + ItemVenta(producto, cantidad))
    }

    fun limpiar() {
        _estado.value = EstadoVentas()
    }

    fun registrarVenta(onStocksActualizados: (Map<String, Int>) -> Unit) {
        val items = _estado.value.items
        if (items.isEmpty()) return

        _estado.value = _estado.value.copy(cargando = true, mensajeError = null, mensajeExito = null)

        viewModelScope.launch {
            try {
                val cambios = firestore.runTransaction { tx ->
                    // 1) LEER primero todos los documentos involucrados
                    val lecturas = items.map { item ->
                        val ref = firestore.collection("inventory").document(item.producto.id)
                        val snap = tx.get(ref)
                        Triple(item, ref, snap)
                    }

                    // 2) Validar y calcular nuevos stocks sin escribir aún
                    val cambiosStock = mutableMapOf<String, Int>()
                    lecturas.forEach { (item, ref, snap) ->
                        val actual = snap.getLong("currentStock")?.toInt() ?: 0
                        val nuevo = actual - item.cantidad
                        if (nuevo < 0) throw IllegalStateException("Stock insuficiente para ${item.producto.name}")
                        cambiosStock[item.producto.id] = nuevo
                    }

                    // 3) ESCRIBIR actualizaciones de inventario
                    lecturas.forEach { (item, ref, _) ->
                        val nuevo = cambiosStock[item.producto.id] ?: return@forEach
                        tx.update(ref, "currentStock", nuevo)
                    }

                    // 4) ESCRIBIR documento de venta
                    val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val itemsDoc = items.map {
                        mapOf(
                            "productoId" to it.producto.id,
                            "nombre" to it.producto.name,
                            "cantidad" to it.cantidad
                        )
                    }
                    val ventaDoc = mapOf(
                        "fecha" to fecha,
                        "items" to itemsDoc,
                        "total" to null
                    )
                    tx.set(firestore.collection("ventas").document(), ventaDoc)

                    cambiosStock.toMap()
                }.await()

                onStocksActualizados(cambios)


                cambios.forEach { (id, nuevoStock) ->
                    val producto = items.find { it.producto.id == id }?.producto
                    if (producto != null && nuevoStock <= 10) {
                        EmailJSHelper.enviarAlertaStockBajo(
                            nombreProducto = producto.name,
                            stock = nuevoStock.toString()
                        )
                    }
                }
                _estado.value = _estado.value.copy(cargando = false, mensajeExito = "Venta registrada", items = emptyList())
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(cargando = false, mensajeError = e.message)
            }
        }
    }}


