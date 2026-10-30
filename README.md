# Rampas Meza

## Funciones integradas

- Notas de venta y cotizaciones en PDF con QR y opción de compartir desde Android.
- Cotizaciones con aprobación administrativa, expiración a 30 días y conversión a venta.
- Perfiles de Administrador, Vendedor y Almacenista.
- Historial de precios y registro auditable de movimientos de inventario.

## Configuración inicial

1. Despliega las reglas de Firestore con `firebase deploy --only firestore:rules`.
2. Registra la primera cuenta desde la app y, en Firebase Console, cambia `users/{uid}.role` a `Administrador`. Los roles no se asignan desde el registro.
3. Entra con esa cuenta, define los precios de los productos y asigna los roles restantes desde **Administración**.

Los comprobantes incluyen el nombre y el logotipo conocidos de Rampas Meza. No incluyen domicilio ni datos fiscales porque no están configurados en el proyecto.