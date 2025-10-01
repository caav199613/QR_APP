package com.example.qr_app.network

import com.example.qr_app.model.Bus
import com.example.qr_app.model.Conductor
import com.example.qr_app.model.Estudiante
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("bus")
    suspend fun getBuses(): List<Bus>

    @GET("conductor")
    suspend fun getConductores(): List<Conductor>

    @POST("registros")
    suspend fun postRegistro(@Body registro: RegistroRequest): RegistroResponse
}

data class RegistroRequest(
    val estudianteId: String,
    val busId: String,
    val conductorId: String
)

data class RegistroResponse(
    val success: Boolean,
    val message: String
)
