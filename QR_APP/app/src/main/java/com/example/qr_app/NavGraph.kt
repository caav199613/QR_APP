package com.example.qr_app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.qr_app.ui.qr.QrScannerScreen
import com.example.qr_app.ui.selection.SelectionScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "selection") {
        composable("selection") {
            SelectionScreen(
                onScanQr = { navController.navigate("qr_scanner") }
            )
        }
        composable("qr_scanner") {
            QrScannerScreen(onBack = { navController.popBackStack() })
        }
    }
}

composable("qrScanner") {
    QrScannerScreen(
        onBack = { navController.popBackStack() },
        onResult = { id, estudiante ->
            // Guardar el ID en ViewModel o State
            // Ejemplo: estudiante!!.numero_identificacion
            // Puedes navegar de regreso a SelectionScreen con los datos
            navController.previousBackStackEntry?.savedStateHandle?.set("estudiante", estudiante)
            navController.popBackStack()
        }
    )
}
