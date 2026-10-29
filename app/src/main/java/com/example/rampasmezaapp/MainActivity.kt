package com.example.rampasmezaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.rampasmezaapp.navigation.AuthNavigation
import com.example.rampasmezaapp.ui.auth.AuthViewModel
import com.example.rampasmezaapp.ui.inventario.ViewModelInventarioGlobal
import com.example.rampasmezaapp.ui.theme.RampasMEZAAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RampasMEZAAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel = AuthViewModel()
                    val inventarioGlobalViewModel = remember { ViewModelInventarioGlobal() }
                    
                    AuthNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                        inventarioGlobalViewModel = inventarioGlobalViewModel
                    )
                }
            }
        }
    }
}