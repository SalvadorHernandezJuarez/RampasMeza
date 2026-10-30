package com.example.rampasmezaapp.data

enum class RolUsuario(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    VENDEDOR("Vendedor"),
    ALMACENISTA("Almacenista");

    companion object {
        fun desde(valor: String?): RolUsuario =
            entries.firstOrNull { it.etiqueta == valor } ?: VENDEDOR
    }
}
