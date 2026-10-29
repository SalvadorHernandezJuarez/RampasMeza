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

@Composable
fun AuthNavigation(
    navController: NavHostController,
    authViewModel: com.example.rampasmezaapp.ui.auth.AuthViewModel,
    inventarioGlobalViewModel: com.example.rampasmezaapp.ui.inventario.ViewModelInventarioGlobal
) {
    val authState = authViewModel.authState.collectAsState().value

    LaunchedEffect(authState.user) {
        if (authState.user != null) {
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
                isLoading = authState.isLoading,
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
                        }
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
    }
}
