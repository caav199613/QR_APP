package com.example.qr_app.ui.selection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun SelectionScreen(
    navController: NavController,
    viewModel: SelectionViewModel,
    onScanQr: () -> Unit
) {
    val fechaLocal = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // "2025-10-01T02:27:44"

    val buses by viewModel.buses.collectAsStateWithLifecycle()
    val conductores by viewModel.conductores.collectAsStateWithLifecycle()
    val estudiante by viewModel.estudiante.collectAsStateWithLifecycle()
    val registroResponse by viewModel.registroResponse.collectAsStateWithLifecycle(initialValue = null)

    val selectedBus by viewModel.selectedBus.collectAsStateWithLifecycle()
    val selectedDriver by viewModel.selectedDriver.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Mostrar Snackbar cuando haya respuesta
    LaunchedEffect(registroResponse) {
        if (registroResponse != null) {
            scope.launch {
                snackbarHostState.showSnackbar("Estudiante registrado con éxito")
            }
            viewModel.clearEstudiante()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("No se pudo registrar el estudiante")
            }
        }
    }



    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Cuadro estudiante
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Estudiante", style = MaterialTheme.typography.titleMedium)
                    if (estudiante != null) {
                        Text("Nombre: ${estudiante?.nombre}")
                        Text("Identificación: ${estudiante?.numero_identificacion}")
                    } else {
                        Text("No se ha escaneado un estudiante todavía")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Dropdown conductor
            Text("Seleccione el conductor:", style = MaterialTheme.typography.titleMedium)
            DropdownSelector(
                items = conductores.map { it.nombre },
                selected = selectedDriver?.nombre,
                onItemSelected = { nombre ->
                    conductores.find { it.nombre == nombre }?.let { viewModel.selectDriver(it) }
                },
                label = "Conductor"
            )

            Spacer(Modifier.height(16.dp))

            // Dropdown bus
            Text("Seleccione el bus:", style = MaterialTheme.typography.titleMedium)
            DropdownSelector(
                items = buses.map { "Bus ${it.numero} - ${it.placa}" },
                selected = selectedBus?.let { "Bus ${it.numero} - ${it.placa}" },
                onItemSelected = { texto ->
                    buses.find { "Bus ${it.numero} - ${it.placa}" == texto }
                        ?.let { viewModel.selectBus(it) }
                },
                label = "Bus"
            )

            Spacer(Modifier.height(24.dp))

            // Botón Escanear QR
            Button(
                onClick = onScanQr,
                enabled = selectedDriver != null && selectedBus != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Escanear QR")
            }

            Spacer(Modifier.height(16.dp))

            // Botón Enviar Registro
            Button(
                onClick = { viewModel.enviarRegistro() },
                enabled = estudiante != null && selectedDriver != null && selectedBus != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar Registro")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    items: List<String>,
    selected: String?,
    onItemSelected: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
