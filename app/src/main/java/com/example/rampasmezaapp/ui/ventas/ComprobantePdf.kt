package com.example.rampasmezaapp.ui.ventas

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.example.rampasmezaapp.R
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

fun compartirComprobante(context: Context, recibo: ReciboVenta) {
    compartirPdf(context, recibo, "NOTA DE VENTA", "nota")
}

fun compartirCotizacionPdf(context: Context, recibo: ReciboVenta) {
    compartirPdf(context, recibo, "COTIZACIÓN", "cotizacion")
}

private fun compartirPdf(context: Context, recibo: ReciboVenta, titulo: String, tipo: String) {
    val pdf = crearPdf(context, recibo, titulo, tipo)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdf
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_SUBJECT, "$titulo ${recibo.id}")
        putExtra(Intent.EXTRA_TEXT, "$titulo de Rampas Meza")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir ${titulo.lowercase(Locale.getDefault())}"))
}

private fun crearPdf(context: Context, recibo: ReciboVenta, titulo: String, tipo: String): File {
    val document = PdfDocument()
    val ancho = 595
    val alto = 842
    val moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"))
    val lineasPorPagina = 20
    val grupos = recibo.items.chunked(lineasPorPagina).ifEmpty { listOf(emptyList()) }
    try {
        grupos.forEachIndexed { index, items ->
            val page = document.startPage(
                PdfDocument.PageInfo.Builder(ancho, alto, index + 1).create()
            )
            val canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
                textSize = 12f
            }
            val bold = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            var y = 52f
            canvas.drawText("RAMPAS MEZA", 40f, y, bold)
            BitmapFactory.decodeResource(context.resources, R.drawable.logo_rampas_meza)?.let { logo ->
                canvas.drawBitmap(logo, null, android.graphics.Rect(430, 24, 555, 105), paint)
                logo.recycle()
            }
            y += 24f
            canvas.drawText(titulo, 40f, y, bold)
            y += 22f
            canvas.drawText("Folio: ${recibo.id}", 40f, y, paint)
            y += 18f
            canvas.drawText("Fecha: ${recibo.fecha}", 40f, y, paint)
            y += 18f
            canvas.drawText("Vendedor: ${recibo.vendedor}", 40f, y, paint)
            y += 26f
            canvas.drawText("Producto", 40f, y, bold)
            canvas.drawText("Cant.", 340f, y, bold)
            canvas.drawText("Precio", 400f, y, bold)
            canvas.drawText("Importe", 500f, y, bold)
            y += 8f
            canvas.drawLine(40f, y, 555f, y, paint)
            y += 20f

            items.forEach { item ->
                val nombre = if (item.nombre.length > 38) item.nombre.take(35) + "..." else item.nombre
                canvas.drawText(nombre, 40f, y, paint)
                canvas.drawText(item.cantidad.toString(), 340f, y, paint)
                canvas.drawText(moneda.format(item.precioUnitario), 400f, y, paint)
                canvas.drawText(moneda.format(item.subtotal), 500f, y, paint)
                y += 20f
            }
            if (index == grupos.lastIndex) {
                y += 16f
                canvas.drawLine(40f, y, 555f, y, paint)
                y += 24f
                canvas.drawText("TOTAL: ${moneda.format(recibo.total)}", 360f, y, bold)
                val qr = generarQr("Rampas Meza|$tipo:${recibo.id}|total:${recibo.total}")
                canvas.drawBitmap(qr, null, android.graphics.Rect(40, 660, 160, 780), paint)
                qr.recycle()
                val etiquetaQr = if (tipo == "nota") "Escanea para identificar esta venta"
                else "Escanea para identificar esta cotizacion"
                canvas.drawText(etiquetaQr, 175f, 715f, paint)
            }
            canvas.drawText("Página ${index + 1} de ${grupos.size}", 480f, 815f, paint)
            document.finishPage(page)
        }
        val directory = File(context.cacheDir, "comprobantes")
        if (!directory.exists() && !directory.mkdirs()) {
            throw IllegalStateException("No se pudo crear el directorio del comprobante")
        }
        val file = File(directory, "${tipo}_${recibo.id}.pdf")
        FileOutputStream(file).use(document::writeTo)
        return file
    } finally {
        document.close()
    }
}

private fun generarQr(texto: String): Bitmap {
    val matrix = QRCodeWriter().encode(
        texto,
        BarcodeFormat.QR_CODE,
        300,
        300,
        mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
    )
    return Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
    }
}
