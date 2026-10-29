package com.example.rampasmezaapp.data

data class Producto(
    val id: String,
    val name: String,
    val description: String,
    val currentStock: Int = 0,
    val imageResource: String = ""
)

object DatosProductos {
    val productos = listOf(
        Producto("1", "PTR 1 1/2\" X 1 1/2\"", "Perfil tubular cuadrado utilizado en estructuras ligeras y marcos de refuerzo.", 0, "ptr"),
        Producto("2", "PTR 2\" X 2\"", "Perfil tubular cuadrado de aplicación general en estructuras metálicas de mediana resistencia.", 0, "ptr"),
        Producto("3", "PTR 2 1/2\" X 2 1/2\"", "Perfil estructural empleado en bastidores, soportes y elementos de carga media.", 0, "ptr"),
        Producto("4", "PTR 3\" X 3\"", "Tubo estructural cuadrado diseñado para columnas y elementos principales de soporte.", 0, "ptr"),
        Producto("5", "PTR 4\" X 2\"", "Perfil rectangular utilizado en travesaños, refuerzos horizontales y estructuras de mediana carga.", 0, "ptr"),
        Producto("6", "PTR 6\" X 2\"", "Tubo rectangular de alta rigidez empleado en sistemas de carga y rampas estructurales.", 0, "ptr"),
        Producto("7", "PTR 6\" X 3\"", "Perfil estructural rectangular de alta resistencia para aplicaciones industriales.", 0, "ptr"),
        Producto("8", "PTR 4\" X 3\"", "Tubo rectangular con buena capacidad estructural, usado en marcos metálicos y vigas secundarias.", 0, "ptr"),
        Producto("9", "PTR 6\" X 4\"", "Perfil tubular de gran sección transversal utilizado en estructuras de alta exigencia mecánica.", 0, "ptr"),
        Producto("10", "PTR 3\" X 2\"", "Tubo rectangular de aplicación estructural en ensamblajes medianos.", 0, "ptr"),
        Producto("11", "SOLERA 1/8\" X 3/4\"", "Platina metálica de baja sección usada en uniones ligeras y componentes secundarios.", 0, "solera"),
        Producto("12", "SOLERA 1/8\" X 1\"", "Elemento plano metálico empleado en refuerzos y fijaciones.", 0, "solera"),
        Producto("13", "SOLERA 1/8\" X 1 1/2\"", "Platina delgada utilizada en estructuras metálicas livianas.", 0, "solera"),
        Producto("14", "SOLERA 1/8\" X 2\"", "Elemento plano para refuerzos longitudinales y uniones estructurales.", 0, "solera"),
        Producto("15", "SOLERA 1/8\" X 3\"", "Platina de baja rigidez empleada en soportes ligeros y terminaciones.", 0, "solera"),
        Producto("16", "SOLERA 1/4\" X 1\"", "Platina de media resistencia utilizada en uniones estructurales y refuerzos.", 0, "solera"),
        Producto("17", "SOLERA 1/4\" X 1 1/2\"", "Elemento plano metálico diseñado para estructuras con carga moderada.", 0, "solera"),
        Producto("18", "SOLERA 1/4\" X 2\"", "Platina empleada en bastidores, travesaños y refuerzos de carga media.", 0, "solera"),
        Producto("19", "SOLERA 1/4\" X 2 1/2\"", "Elemento de acero plano usado en puntos de apoyo estructural.", 0, "solera"),
        Producto("20", "SOLERA 3/8\" X 1\"", "Platina de alta densidad utilizada en componentes industriales.", 0, "solera"),
        Producto("21", "SOLERA 3/8\" X 1 1/2\"", "Elemento metálico de media-alta resistencia para estructuras de carga intermedia.", 0, "solera"),
        Producto("22", "SOLERA 3/8\" X 2\"", "Platina de uso estructural para anclajes o amarres metálicos.", 0, "solera"),
        Producto("23", "SOLERA 3/8\" X 2 1/2\"", "Platina plana utilizada en refuerzos de marcos o bases.", 0, "solera"),
        Producto("24", "SOLERA 1/2\" X 1 1/2\"", "Elemento sólido para estructuras de carga pesada.", 0, "solera"),
        Producto("25", "SOLERA 1/2\" X 2 1/2\"", "Platina gruesa empleada en soportes y refuerzos principales.", 0, "solera"),
        Producto("26", "SOLERA 1/2\" X 3\"", "Platina de gran espesor utilizada en bases estructurales y zonas de alta carga.", 0, "solera"),
        Producto("27", "SOLERA 3/16\" X 1 1/2\"", "Elemento intermedio utilizado en estructuras ligeras con requerimiento de rigidez.", 0, "solera"),
        Producto("28", "SOLERA 5/8\" X 1 1/2\"", "Platina de alta resistencia para refuerzos estructurales.", 0, "solera"),
        Producto("29", "SOLERA 5/8\" X 2\"", "Platina maciza utilizada en uniones principales y bastidores metálicos.", 0, "solera"),
        Producto("30", "SOLERA 5/8\" X 3\"", "Elemento de alta rigidez diseñado para estructuras industriales.", 0, "solera"),
        Producto("31", "TUBO 2\" CED. 40", "Tubo de acero de pared gruesa utilizado en sistemas estructurales y de conducción.", 0, "tubo"),
        Producto("32", "TUBO 5/4\" CED. 40", "Tubo de acero de diámetro intermedio empleado en pasamanos y refuerzos.", 0, "tubo"),
        Producto("33", "TUBO 1\" CED. 40", "Tubo de acero estándar utilizado en estructuras ligeras y conexiones metálicas.", 0, "tubo"),
        Producto("34", "VIGA IPS 3\"", "Viga tipo I fabricada en acero, utilizada en estructuras portantes y marcos principales.", 0, "viga"),
        Producto("35", "CANAL 3\"", "Perfil estructural en forma de U empleado en vigas secundarias y soportes.", 0, "canal"),
        Producto("36", "PERFIL 3/4\" X 3/4\" CAL 18", "Perfil cuadrado de lámina delgada utilizado en estructuras livianas o de acabado.", 0, "perfil"),
        Producto("37", "REDONDO 1/2\" SÓLIDO", "Barra maciza de sección circular utilizada en ejes, pasadores y refuerzos.", 0, "redondo"),
        Producto("38", "CUADRADO 1/2\" SÓLIDO", "Barra maciza de sección cuadrada empleada en elementos estructurales.", 0, "cuadrado"),
        Producto("39", "CUADRADO 3/8\" SÓLIDO", "Barra sólida de menor sección para piezas secundarias y componentes mecánicos.", 0, "cuadrado"),
        Producto("40", "POLÍN 3\" CAL 14", "Perfil estructural rectangular de acero galvanizado, empleado en techumbres y estructuras metálicas.", 0, "polin"),
        Producto("41", "LÁMINA ANTIDERRAPANTE 1/8\" X 3' X 10'", "Lámina de acero con superficie texturizada para piso estructural de baja carga.", 0, "lamina_antiderrapante"),
        Producto("42", "LÁMINA ANTIDERRAPANTE 1/8\" X 4' X 10'", "Lámina texturizada de acero al carbón para recubrimientos de acceso.", 0, "lamina_antiderrapante"),
        Producto("43", "LÁMINA ANTIDERRAPANTE 1/4\" X 3' X 10'", "Lámina antiderrapante de alto espesor para aplicaciones industriales.", 0, "lamina_antiderrapante"),
        Producto("44", "LÁMINA ANTIDERRAPANTE 1/4\" X 4' X 10'", "Placa de acero texturizada utilizada en rampas y zonas de carga.", 0, "lamina_antiderrapante"),
        Producto("45", "LÁMINA LISA 1/8\" X 4' X 10'", "Lámina de acero al carbón sin relieve, usada en fabricación de piezas y cortes estructurales.", 0, "lamina_lisa")
    )
}
