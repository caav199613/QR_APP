// network/RegistroRequest.kt
package com.example.qr_app.network

import com.google.gson.annotations.SerializedName

data class RegistroRequest(
    @SerializedName("id_estudiante")
    val idEstudiante: String,
    @SerializedName("id_bus")
    val idBus: String,
    @SerializedName("id_conductor")
    val idConductor: String,
    @SerializedName("fecha_y_hora")
    val fechaYHora: String
)