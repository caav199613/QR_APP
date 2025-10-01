package com.example.qr_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.qr_app.ui.selection.SelectionViewModel
import com.example.qr_app.ui.theme.BusAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // actionBar?.hide() // ⚠️ Si usas Theme sin ActionBar, no hace falta
        setContent {
            BusAppTheme {
                val navController = rememberNavController()
                val selectionViewModel: SelectionViewModel = viewModel()
                NavGraph(
                    navController = navController,
                    selectionViewModel = selectionViewModel
                )
            }
        }
    }
}
