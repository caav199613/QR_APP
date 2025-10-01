package com.example.qr_app.ui.selection

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

    sealed class RegistroState {
        object Idle : RegistroState()
        object Loading : RegistroState()
        data class Success(val response: RegistroResponse) : RegistroState()
        data class Error(val message: String) : RegistroState()
    }

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

    private val _registroState = MutableStateFlow<RegistroState>(RegistroState.Idle)
    val registroState: StateFlow<RegistroState> = _registroState

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
        val estudianteId = _estudiante.value?.id
        val busId = _selectedBus.value?.id
        val conductorId = _selectedDriver.value?.id

        if (estudianteId != null && busId != null && conductorId != null) {
            viewModelScope.launch {
                _registroState.value = RegistroState.Loading
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
                    _registroState.value = RegistroState.Success(response)
                    _estudiante.value = null
                } catch (e: HttpException) {
                    val errBody = e.response()?.errorBody()?.string()
                    _registroState.value = RegistroState.Error("No se pudo registrar el estudiante: ${errBody ?: e.message()}")
                } catch (e: Exception) {
                    _registroState.value = RegistroState.Error("No se pudo registrar el estudiante: ${e.message}")
                }
            }
        }
    }


    fun clearEstudiante() {
        _estudiante.value = null
    }

}
