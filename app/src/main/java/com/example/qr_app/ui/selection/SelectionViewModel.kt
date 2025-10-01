package com.example.qr_app.ui.selection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr_app.model.Bus
import com.example.qr_app.model.Conductor
import com.example.qr_app.model.Estudiante
import com.example.qr_app.network.RegistroRequest
import com.example.qr_app.network.RegistroResponse
import com.example.qr_app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class SelectionViewModel : ViewModel() {

    private val _selectedDriver = MutableStateFlow<Conductor?>(null)
    val selectedDriver: StateFlow<Conductor?> = _selectedDriver

    private val _selectedBus = MutableStateFlow<Bus?>(null)
    val selectedBus: StateFlow<Bus?> = _selectedBus

    private val _estudiante = MutableStateFlow<Estudiante?>(null)
    val estudiante: StateFlow<Estudiante?> = _estudiante

    private val _buses = MutableStateFlow<List<Bus>>(emptyList())
    val buses: StateFlow<List<Bus>> = _buses

    private val _conductores = MutableStateFlow<List<Conductor>>(emptyList())
    val conductores: StateFlow<List<Conductor>> = _conductores

    private val _registroResponse = MutableStateFlow<RegistroResponse?>(null)
    val registroResponse: StateFlow<RegistroResponse?> = _registroResponse

    fun selectDriver(driver: Conductor) {
        _selectedDriver.value = driver
    }

    fun selectBus(bus: Bus) {
        _selectedBus.value = bus
    }

    fun setEstudiante(est: Estudiante) {
        _estudiante.value = est
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _buses.value = RetrofitClient.apiService.getBuses()
                _conductores.value = RetrofitClient.apiService.getConductores()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun enviarRegistro() {
        val estudianteId = _estudiante.value?.id?.toString()
        val busId = _selectedBus.value?.id?.toString()
        val conductorId = _selectedDriver.value?.id?.toString()

        if (estudianteId != null && busId != null && conductorId != null) {
            viewModelScope.launch {
                try {
                    val fechaIso = Instant.now().atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                    val request = RegistroRequest(
                        idEstudiante = estudianteId,
                        idBus = busId,
                        idConductor = conductorId,
                        fechaYHora = fechaIso
                    )

                    val response = RetrofitClient.apiService.postRegistro(request)
                    _registroResponse.value = response
                } catch (e: HttpException) {
                    Log.e("SelectionVM", "HTTP error ${e.code()}: ${e.response()?.errorBody()?.string()}")
                    // Mostrar snackbar de error
                    _registroResponse.value = null // para que Compose lo interprete como fallo
                } catch (e: Exception) {
                    Log.e("SelectionVM", "Error enviando registro", e)
                    _registroResponse.value = null
                }
            }
        }
    }


    fun clearEstudiante() {
        _estudiante.value = null
    }

}
