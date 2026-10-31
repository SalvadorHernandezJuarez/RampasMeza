package com.example.rampasmezaapp.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object LocalDemoStore {
    const val EMAIL = "salvador.hernandez@gmail.com"
    private const val PASSWORD = "1234"
    private const val PREFERENCES = "local_demo"
    private const val INVENTORY = "inventory"
    private lateinit var preferences: android.content.SharedPreferences

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }

    fun acceptsCredentials(email: String, password: String): Boolean =
        email.trim().equals(EMAIL, ignoreCase = true) && password == PASSWORD

    @Synchronized
    fun products(): List<Producto> {
        val saved = JSONObject(preferences.getString(INVENTORY, "{}").orEmpty())
        return DatosProductos.productos.map { product ->
            val stored = saved.optJSONObject(product.id)
            product.copy(
                currentStock = stored?.optInt("stock", 0) ?: 0,
                price = stored?.optDouble("price", product.price) ?: product.price
            )
        }
    }

    @Synchronized
    fun updateStock(productId: String, stock: Int) {
        val inventory = currentInventory()
        val stored = inventory.optJSONObject(productId) ?: newInventoryEntry(productId)
        stored.put("stock", stock)
        inventory.put(productId, stored)
        persistInventory(inventory)
    }

    @Synchronized
    fun updatePrice(productId: String, price: Double) {
        val inventory = currentInventory()
        val stored = inventory.optJSONObject(productId) ?: newInventoryEntry(productId)
        stored.put("price", price)
        inventory.put(productId, stored)
        persistInventory(inventory)
    }

    @Synchronized
    fun records(collection: String): List<Map<String, Any?>> {
        val array = JSONArray(preferences.getString(collection, "[]").orEmpty())
        return (0 until array.length()).mapNotNull { array.optJSONObject(it)?.toMap() }
    }

    @Synchronized
    fun addRecord(collection: String, values: Map<String, Any?>): String {
        val array = JSONArray(preferences.getString(collection, "[]").orEmpty())
        val id = UUID.randomUUID().toString()
        val record = JSONObject()
        values.forEach { (key, value) -> record.put(key, value.toJsonValue()) }
        record.put("id", id)
        array.put(record)
        preferences.edit().putString(collection, array.toString()).apply()
        return id
    }

    @Synchronized
    fun updateRecord(collection: String, id: String, changes: Map<String, Any?>) {
        val array = JSONArray(preferences.getString(collection, "[]").orEmpty())
        for (index in 0 until array.length()) {
            val record = array.optJSONObject(index) ?: continue
            if (record.optString("id") != id) continue
            changes.forEach { (key, value) -> record.put(key, value.toJsonValue()) }
            preferences.edit().putString(collection, array.toString()).apply()
            return
        }
        throw IllegalArgumentException("No se encontró el registro local $id")
    }

    fun role(): RolUsuario =
        RolUsuario.desde(preferences.getString("demo_role", RolUsuario.ADMINISTRADOR.etiqueta))

    fun updateRole(role: RolUsuario) {
        preferences.edit().putString("demo_role", role.etiqueta).apply()
    }

    private fun currentInventory() = JSONObject(preferences.getString(INVENTORY, "{}").orEmpty())

    private fun newInventoryEntry(productId: String) = JSONObject().apply {
            put("stock", 0)
            put("price", DatosProductos.productos.firstOrNull { it.id == productId }?.price ?: 0.0)
        }

    private fun persistInventory(inventory: JSONObject) {
        preferences.edit().putString(INVENTORY, inventory.toString()).apply()
    }

    private fun Map<String, Any?>.toJson(): JSONObject = JSONObject().apply {
        this@toJson.forEach { (key, value) -> put(key, value.toJsonValue()) }
    }

    private fun JSONObject.toMap(): Map<String, Any?> = keys().asSequence().associateWith { key ->
        when (val value = opt(key)) {
            JSONObject.NULL -> null
            is JSONObject -> value.toMap()
            is JSONArray -> (0 until value.length()).map { index ->
                when (val item = value.opt(index)) {
                    JSONObject.NULL -> null
                    is JSONObject -> item.toMap()
                    else -> item
                }
            }
            else -> value
        }
    }

    private fun Any?.toJsonValue(): Any = when (this) {
        null -> JSONObject.NULL
        is Map<*, *> -> {
            val values = entries.mapNotNull { (key, value) ->
                (key as? String)?.let { it to value }
            }.toMap()
            values.toJson()
        }
        is Iterable<*> -> JSONArray().apply { forEach { put(it.toJsonValue()) } }
        else -> this
    }
}
