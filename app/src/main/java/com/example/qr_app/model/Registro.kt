package com.example.qr_app.model

class Registro {
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
