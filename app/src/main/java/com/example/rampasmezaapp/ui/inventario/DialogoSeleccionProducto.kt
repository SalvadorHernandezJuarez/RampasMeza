package com.example.rampasmezaapp.ui.inventario

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rampasmezaapp.R
import com.example.rampasmezaapp.data.Producto

@Composable
fun DialogoSeleccionProducto(
    productos: List<Producto>,
    onProductSelected: (Producto) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Seleccionar Producto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productos) { producto ->
                        ItemProducto(
                            producto = producto,
                            onClick = {
                                onProductSelected(producto)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemProducto(
    producto: Producto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageResource = when (producto.imageResource) {
                "ptr" -> R.drawable.ptr
                "solera" -> R.drawable.solera
                "tubo" -> R.drawable.tubo
                "viga" -> R.drawable.viga
                "canal" -> R.drawable.canal
                "perfil" -> R.drawable.perfil
                "redondo" -> R.drawable.redondo
                "cuadrado" -> R.drawable.cuadrado
                "polin" -> R.drawable.polin
                "lamina_antiderrapante" -> R.drawable.lamina_antiderrapante
                "lamina_lisa" -> R.drawable.lamina_lisa
                else -> R.drawable.ptr
            }
            
            androidx.compose.foundation.Image(
                painter = painterResource(id = imageResource),
                contentDescription = producto.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = producto.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = producto.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = buildString {
                        append("Stock actual: ")
                        append(producto.currentStock)
                        append(" unidades")
                        if (producto.currentStock <= 10) append("  |  Stock bajo")
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (producto.currentStock <= 10) Color.Red else Color(0xFF049749)
                )
            }
        }
    }
}
