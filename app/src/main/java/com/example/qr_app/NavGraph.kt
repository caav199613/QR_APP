package com.example.qr_app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.qr_app.ui.qr.QrScannerScreen
import com.example.qr_app.ui.selection.SelectionScreen
import com.example.qr_app.ui.selection.SelectionViewModel

@Composable
fun NavGraph(navController: NavHostController, selectionViewModel: SelectionViewModel) {
    val selectionViewModel: SelectionViewModel = viewModel() // mismo owner, por ejemplo en Activity/NavHost

    NavHost(navController, startDestination = "selection") {
        composable("selection") {
            SelectionScreen(
                navController = navController,
                viewModel = selectionViewModel,
                onScanQr = { navController.navigate("qrScanner") }
            )
        }

        composable("qrScanner") {
            QrScannerScreen(
                navController = navController,
                viewModel = selectionViewModel, // <-- REUTILIZAS LA MISMA INSTANCIA
                onBack = { navController.popBackStack() },
                onResult = { estudiante ->
                    // opcional: aquí también puedes manejar algo si quieres
                }
            )
        }
    }

}
