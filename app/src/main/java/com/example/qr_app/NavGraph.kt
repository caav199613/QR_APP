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
fun NavGraph(navController: NavHostController) {
    val selectionViewModel: SelectionViewModel = viewModel()

    NavHost(navController = navController, startDestination = "selection") {
        composable("selection") {
            SelectionScreen(
                navController = navController,
                onScanQr = { navController.navigate("qr_scanner") },
                viewModel = TODO()
            )
        }
        composable("qr_scanner") {
            QrScannerScreen(
                navController = navController,
                viewModel = selectionViewModel,
                onBack = { navController.popBackStack() },
                onResult = { estudiante ->
                    // Guardar estudiante en ViewModel
                    selectionViewModel.setEstudiante(estudiante)
                    navController.popBackStack()
                }
            )
        }
    }
}
