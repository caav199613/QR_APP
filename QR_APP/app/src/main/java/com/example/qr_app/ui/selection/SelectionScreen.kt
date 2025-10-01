package com.example.qr_app.ui.selection

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun SelectionScreen(
    navController: NavController,
    viewModel: SelectionViewModel,
    onScanQr: () -> Unit
) {
    val buses by viewModel.buses.collectAsStateWithLifecycle()
    val conductores by viewModel.conductores.collectAsStateWithLifecycle()
    val estudiante by viewModel.estudiante.collectAsStateWithLifecycle()
    val registroResponse by viewModel.registroResponse.collectAsStateWithLifecycle()

    val selectedBus by viewModel.selectedBus.collectAsStateWithLifecycle()
    val selectedDriver by viewModel.selectedDriver.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Mostrar Snackbar cuando haya respuesta
    LaunchedEffect(registroResponse) {
        registroResponse?.let { response ->
            scope.launch {
                snackbarHostState.showSnackbar(response.message)
            }
            if (response.success) {
                viewModel.clearEstudiante() // ✅ limpiamos estudiante si éxito
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
