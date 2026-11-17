# Informe de pruebas

- **CI 44177572104:** `testDebugUnitTest`, `lintDebug` y `assembleDebug` pasaron; artifact Debug generado.
- Pruebas unitarias cubren coincidencia del catálogo, app normal, salida de juego, cambio de juego y varias Activities del mismo package.
- Galaxy A23 / Android 14 (API 34): APK instalado. Usage Access concedido durante la prueba. El servicio foreground apareció con su notificación; al abrir Roblox, la notificación indicó `Jugando: Roblox`; al salir a inicio volvió a `No se detecta un juego`.
- Discord está instalado en el teléfono; no se intentó publicar actividad porque la documentación oficial no ofrece el uso externo requerido.
- Prueba real de Minecraft, Free Fire y MediaSession con reproductor de música: pendiente. Se respetará la instrucción del usuario de no volver a usar ADB sin preguntarle antes.
- No se marca prueba de medios ni de publicación Discord como exitosa con datos simulados.
