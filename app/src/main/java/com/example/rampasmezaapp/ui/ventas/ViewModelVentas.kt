package com.example.rampasmezaapp.ui.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rampasmezaapp.data.Producto
import com.example.rampasmezaapp.ui.inventario.EmailJSHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

data class ItemVenta(val producto: Producto, val cantidad: Int)
data class ItemRecibo(
    val productoId: String,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)
data class ReciboVenta(
    val id: String,
    val fecha: String,
    val vendedor: String,
    val items: List<ItemRecibo>,
    val total: Double
)

data class EstadoVentas(
    val items: List<ItemVenta> = emptyList(),
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null,
    val recibo: ReciboVenta? = null
)

class ViewModelVentas : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _estado = MutableStateFlow(EstadoVentas())
    val estado: StateFlow<EstadoVentas> = _estado.asStateFlow()

    fun agregarItem(producto: Producto, cantidad: Int) {
        if (cantidad <= 0) return
        _estado.value = _estado.value.copy(items = _estado.value.items + ItemVenta(producto, cantidad))
    }

    fun limpiar() {
        _estado.value = EstadoVentas()
    }

    fun registrarVenta(
        nombreVendedor: String,
        onStocksActualizados: (Map<String, Int>) -> Unit
    ) {
        val items = _estado.value.items
        if (items.isEmpty() || _estado.value.cargando) return

        _estado.value = _estado.value.copy(cargando = true, mensajeError = null, mensajeExito = null)
        viewModelScope.launch {
            try {
                val usuario = FirebaseAuth.getInstance().currentUser
                    ?: throw IllegalStateException("Debes iniciar sesión para registrar una venta")
                val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date())
                val ventaRef = firestore.collection("ventas").document()
                val cantidades = items.groupBy { it.producto.id }.mapValues { (_, repetidos) ->
                    val total = repetidos.sumOf { it.cantidad.toLong() }
                    if (total > Int.MAX_VALUE) throw IllegalArgumentException("La cantidad total supera el límite permitido")
                    total.toInt()
                }
                val productos = items.associateBy { it.producto.id }

                val cambios = firestore.runTransaction { tx ->
                    val lecturas = cantidades.keys.associateWith { id ->
                        val ref = firestore.collection("inventory").document(id)
                        Triple(ref, tx.get(ref), cantidades.getValue(id))
                    }
                    val lineas = lecturas.map { (id, lectura) ->
                        val (ref, snap, cantidad) = lectura
                        val producto = productos.getValue(id).producto
                        val stock = snap.getLong("currentStock")?.toInt() ?: 0
                        val precio = snap.getDouble("currentPrice") ?: producto.price
                        if (!snap.exists()) throw IllegalStateException("No existe el producto ${producto.name}")
                        if (precio <= 0.0) throw IllegalStateException("Configura el precio de ${producto.name} antes de vender")
                        if (stock < cantidad) throw IllegalStateException("Stock insuficiente para ${producto.name}")
                        Triple(ref, stock - cantidad, ItemRecibo(id, producto.name, cantidad, precio, redondear(precio * cantidad)))
                    }
                    lineas.forEach { (ref, stockNuevo, _) ->
                        tx.update(
                            ref,
                            mapOf("currentStock" to stockNuevo, "lastSaleId" to ventaRef.id)
                        )
                    }
                    val reciboItems = lineas.map { it.third }
                    val total = redondear(reciboItems.sumOf { it.subtotal })
                    tx.set(
                        ventaRef,
                        mapOf(
                            "fecha" to fecha,
                            "items" to reciboItems.map {
                                mapOf(
                                    "productoId" to it.productoId,
                                    "nombre" to it.nombre,
                                    "cantidad" to it.cantidad,
                                    "precioUnitario" to it.precioUnitario,
                                    "subtotal" to it.subtotal
                                )
                            },
                            "total" to total,
                            "createdBy" to usuario.uid,
                            "vendedor" to nombreVendedor,
                            "cantidades" to cantidades
                        )
                    )
                    Pair(lineas.associate { it.first.id to it.second }, reciboItems to total)
                }.await()

                val stocks = cambios.first
                onStocksActualizados(stocks)
                stocks.forEach { (id, stock) ->
                    if (stock <= 10) {
                        productos[id]?.producto?.let {
                            EmailJSHelper.enviarAlertaStockBajo(it.name, stock.toString())
                        }
                    }
                }
                val recibo = ReciboVenta(ventaRef.id, fecha, nombreVendedor, cambios.second.first, cambios.second.second)
                _estado.value = EstadoVentas(
                    mensajeExito = "Venta registrada",
                    recibo = recibo
                )
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(cargando = false, mensajeError = e.message ?: "No se pudo registrar la venta")
            }
        }
    }

    private fun redondear(valor: Double): Double = round(valor * 100.0) / 100.0
}
