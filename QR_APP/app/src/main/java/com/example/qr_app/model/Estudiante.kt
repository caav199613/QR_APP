package com.example.qr_app.model

data class Estudiante(
    val nombre: String,
    val tipo_identificacion: String,
    val numero_identificacion: String,
    val correo: String,
    val telefono: String,
    val jornada: String,
    val grado: String,
    val codigo_grado: Int,
    val acudiente: String,
    val numero_acudiente: String
)
