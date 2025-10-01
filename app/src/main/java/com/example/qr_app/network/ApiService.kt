package com.example.qr_app.network

import com.example.qr_app.model.Bus
import com.example.qr_app.model.Conductor
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("buses")
    suspend fun getBuses(): List<Bus>

    @GET("conductores")
    suspend fun getConductores(): List<Conductor>

    @POST("registros")
    suspend fun postRegistro(@Body registro: RegistroRequest): RegistroResponse
}

data class RegistroResponse(
    val id_estudiante: String,
    val id_bus: String,
    val id_conductor: String,
    val fecha_y_hora: String,
    val id: String
)

