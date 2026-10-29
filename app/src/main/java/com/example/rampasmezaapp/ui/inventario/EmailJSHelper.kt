package com.example.rampasmezaapp.ui.inventario

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object EmailJSHelper {
    private const val SERVICE_ID = "service_zk9ieod"
    private const val TEMPLATE_ID = "template_jdlj6yd"
    private const val PUBLIC_KEY = "PGfz43LYEWquBCaiq"

    fun enviarAlertaStockBajo(nombreProducto: String, stock: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://api.emailjs.com/api/v1.0/email/send")
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "POST"
                con.setRequestProperty("Content-Type", "application/json")
                con.doOutput = true
                val body = JSONObject()
                body.put("service_id", SERVICE_ID)
                body.put("template_id", TEMPLATE_ID)
                body.put("user_id", PUBLIC_KEY)
                val params = JSONObject()
                params.put("name", nombreProducto)
                params.put("message", "Stock: $stock")
                body.put("template_params", params)
                val os = OutputStreamWriter(con.outputStream)
                os.write(body.toString())
                os.flush()
                os.close()
                con.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {

            }
        }
    }
}
