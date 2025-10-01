package com.example.qr_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.qr_app.ui.theme.BusAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusAppTheme {
                NavGraph(navController = rememberNavController())
            }
        }
    }
}
