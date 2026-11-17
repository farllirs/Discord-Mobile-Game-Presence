# Informe de pruebas

- **CI 44180560988:** pasó `testDebugUnitTest`, `lintDebug` y `assembleDebug` con los modos Juegos/Música y la búsqueda de proveedores multimedia. Artifact Debug generado.
- Pruebas unitarias: coincidencia del catálogo, app ajena, salida de juego, cambio de juego y varias Activities del mismo package.
- Galaxy A23 / Android 14 (API 34): una versión anterior del APK se instaló y probó. Con Usage Access concedido, el servicio foreground mostró Roblox como juego actual y al volver a Inicio pasó a “No se detecta un juego”.
- El APK alpha actual compila y está publicado, pero el rediseño visual/modo Música no se han verificado en el teléfono. La prueba física de reproductores y carátulas queda pendiente.
- Discord está instalado en el teléfono; no se intentó publicar actividad. La documentación oficial no permite los estados externos solicitados mediante Social SDK.
- No se marca prueba de Minecraft, Free Fire, fuentes multimedia ni publicación Discord como exitosa con datos simulados.
