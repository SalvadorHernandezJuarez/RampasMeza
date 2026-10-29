package com.example.rampasmezaapp.ui.inventario

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.rampasmezaapp.data.Producto
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteInventario(
    inventarioGlobalViewModel: ViewModelInventarioGlobal,
    onBackToMainClick: () -> Unit
) {
    val estado by inventarioGlobalViewModel.estadoGlobal.collectAsState()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    fun generarJsonCompacto(productos: List<Producto>): String {
        val sb = StringBuilder()
        productos.forEachIndexed { index, p ->
            if (index > 0) sb.append('\n')
            sb.append(p.name.replace("\"", "'"))
            sb.append(" Stock:")
            sb.append(p.currentStock)
        }
        return sb.toString()
    }

    fun generarQR(contenido: String, size: Int = 720): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, size, size, hints)
            val ancho = bitMatrix.width
            val alto = bitMatrix.height
            val bmp = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
            for (x in 0 until ancho) {
                for (y in 0 until alto) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reporte de Inventario",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.rampasmezaapp.R.drawable.logo_rampas_meza),
                contentDescription = "Logo Rampas Meza",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBackToMainClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Ir a Menú Principal", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val json = generarJsonCompacto(estado.productos)
                    val bmp = generarQR(json)
                    if (bmp == null) {
                        mensajeError = "No se pudo generar el QR. Intenta reducir el contenido."
                    } else {
                        mensajeError = null
                        qrBitmap = bmp
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generar QR del inventario", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mensajeError != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0E0))) {
                    Text(
                        text = mensajeError!!,
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (qrBitmap != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("QR generado (inventario actual)", fontWeight = FontWeight.Medium, color = Color.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Inventario",
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val bmp = qrBitmap ?: return@Button
                            try {
                                val cacheDir = File(context.cacheDir, "qr")
                                if (!cacheDir.exists()) cacheDir.mkdirs()
                                val outFile = File(cacheDir, "qr_inventario.png")
                                FileOutputStream(outFile).use { fos ->
                                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                }
                                val uri: Uri = FileProvider.getUriForFile(
                                    context,
                                    context.packageName + ".fileprovider",
                                    outFile
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir QR"))
                            } catch (e: Exception) {
                                mensajeError = "No se pudo compartir el QR."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Compartir QR (PNG)", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            val bmp = qrBitmap ?: return@Button
                            try {
                                val filename = "qr_inventario_${System.currentTimeMillis()}.png"
                                val resolver = context.contentResolver
                                val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                                } else {
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                }
                                val contentValues = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                                    }
                                }
                                val uri = resolver.insert(imageCollection, contentValues)
                                if (uri != null) {
                                    resolver.openOutputStream(uri).use { os: OutputStream? ->
                                        if (os == null) throw IllegalStateException("Salida nula")
                                        bmp.compress(Bitmap.CompressFormat.PNG, 100, os)
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        contentValues.clear()
                                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                                        resolver.update(uri, contentValues, null, null)
                                    }
                                    mensajeError = null
                                } else {
                                    mensajeError = "No se pudo guardar el QR."
                                }
                            } catch (e: Exception) {
                                mensajeError = "Error al guardar el QR."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar QR (PNG)", color = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Inventario actual", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(estado.productos) { p ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(p.name, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Stock: ${p.currentStock}", color = Color(0xFF049749))
                        }
                    }
                }
            }
        }
    }
}


