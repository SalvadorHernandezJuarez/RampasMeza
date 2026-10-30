package com.example.rampasmezaapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rampasmezaapp.ui.auth.LoginScreen
import com.example.rampasmezaapp.ui.auth.RegisterScreen
import com.example.rampasmezaapp.ui.inventario.PanelDeControl
import com.example.rampasmezaapp.ui.inventario.AnadirStock
import com.example.rampasmezaapp.ui.inventario.AjustesInventario
import com.example.rampasmezaapp.ui.auth.Administracion
import com.example.rampasmezaapp.ui.ventas.Cotizaciones
import com.example.rampasmezaapp.data.RolUsuario

@Composable
fun AuthNavigation(
    navController: NavHostController,
    authViewModel: com.example.rampasmezaapp.ui.auth.AuthViewModel,
    inventarioGlobalViewModel: com.example.rampasmezaapp.ui.inventario.ViewModelInventarioGlobal
) {
    val authState = authViewModel.authState.collectAsState().value

    LaunchedEffect(authState.user, authState.profileLoading) {
        if (authState.user != null && !authState.profileLoading) {
            navController.navigate("inventory") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onRegisterClick = {
                    navController.navigate("register")
                },
                isLoading = authState.isLoading || authState.profileLoading,
                errorMessage = authState.errorMessage
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterClick = { email, name, password ->
                    authViewModel.register(email, name, password)
                },
                onLoginClick = {
                    navController.navigate("login")
                },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage
            )
        }

                composable("inventory") {
                    PanelDeControl(
                        rol = authState.rol,
                        onLogoutClick = {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onAddStockClick = {
                            navController.navigate("add_stock")
                        },
                        onRegistrarVentasClick = {
                            navController.navigate("registrar_ventas")
                        },
                        onHistorialVentasClick = {
                            navController.navigate("historial_ventas")
                        },
                        onReporteInventarioClick = {
                            navController.navigate("reporte_inventario")
                        },
                        onCotizacionesClick = { navController.navigate("cotizaciones") },
                        onAjustesInventarioClick = { navController.navigate("ajustes_inventario") },
                        onAdministracionClick = { navController.navigate("administracion") }
                    )
                }

                composable("add_stock") {
                    AnadirStock(
                        inventarioGlobalViewModel = inventarioGlobalViewModel,
                        onBackToMainClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Registrar ventas
                composable("registrar_ventas") {
                    com.example.rampasmezaapp.ui.ventas.RegistrarVentas(
                        inventarioGlobalViewModel = inventarioGlobalViewModel,
                        nombreVendedor = authState.nombre,
                        onBackToMainClick = { navController.popBackStack() }
                    )
                }

                // Historial de ventas
                composable("historial_ventas") {
                    com.example.rampasmezaapp.ui.ventas.HistorialVentas(
                        onBackToMainClick = { navController.popBackStack() }
                    )
                }

                // Reporte de inventario con QR
                composable("reporte_inventario") {
                    com.example.rampasmezaapp.ui.inventario.ReporteInventario(
                        inventarioGlobalViewModel = inventarioGlobalViewModel,
                        onBackToMainClick = { navController.popBackStack() }
                    )
                }

                composable("cotizaciones") {
                    if (authState.rol != RolUsuario.ALMACENISTA) {
                        Cotizaciones(
                            inventarioGlobalViewModel = inventarioGlobalViewModel,
                            nombreVendedor = authState.nombre,
                            esAdministrador = authState.rol == RolUsuario.ADMINISTRADOR,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("ajustes_inventario") {
                    if (authState.rol != RolUsuario.VENDEDOR) {
                        AjustesInventario(
                            inventarioGlobalViewModel = inventarioGlobalViewModel,
                            nombreUsuario = authState.nombre,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("administracion") {
                    if (authState.rol == RolUsuario.ADMINISTRADOR) {
                        Administracion(
                            inventarioGlobalViewModel = inventarioGlobalViewModel,
                            nombreAdministrador = authState.nombre,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
    }
}
