package com.example.rampasmezaapp.ui.inventario

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rampasmezaapp.R
import com.example.rampasmezaapp.data.RolUsuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelDeControl(
    rol: RolUsuario,
    onLogoutClick: () -> Unit,
    onAddStockClick: () -> Unit,
    onRegistrarVentasClick: () -> Unit,
    onHistorialVentasClick: () -> Unit,
    onReporteInventarioClick: () -> Unit,
    onCotizacionesClick: () -> Unit,
    onAjustesInventarioClick: () -> Unit,
    onAdministracionClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Panel de Control", fontWeight = FontWeight.Bold, color = Color.Black)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_rampas_meza),
                contentDescription = "Logo Rampas Meza",
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp))
            )
            Text("Acceso: ${rol.etiqueta}", color = Color.DarkGray)
            Spacer(modifier = Modifier.height(32.dp))

            if (rol != RolUsuario.VENDEDOR) {
                botonMenu("Añadir Stock", onAddStockClick)
                botonMenu("Devoluciones y ajustes", onAjustesInventarioClick)
            }
            if (rol != RolUsuario.ALMACENISTA) {
                botonMenu("Registrar Ventas", onRegistrarVentasClick)
                botonMenu("Historial de Ventas", onHistorialVentasClick)
                botonMenu("Cotizaciones", onCotizacionesClick)
            }
            botonMenu("Generar reporte de inventario", onReporteInventarioClick)
            if (rol == RolUsuario.ADMINISTRADOR) {
                botonMenu("Administrar usuarios, roles y precios", onAdministracionClick, true)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ColumnScope.botonMenu(
    texto: String,
    onClick: () -> Unit,
    verde: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (verde) Color(0xFF049749) else Color(0xFFFFD700)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            texto,
            fontSize = 18.sp,
            color = if (verde) Color.White else Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}
