# Farllirs Presence — Discord Mobile Game Presence

App Android Kotlin + Jetpack Compose con identidad **Farllirs / XZP** (violeta y menta). Estado: prototipo. Detector/catálogo/UI implementados; publicación Discord aún no está activa y no se presenta como conexión real.

## Discord: verificación

La [documentación Social SDK](https://discord.com/developers/docs/social-sdk/overview) y sus [notas de versión](https://discord.com/developers/docs/social-sdk/release_notes.html) describen soporte del SDK Android (7+) y Rich Presence por RPC no autenticado hacia el cliente Android de Discord, sin OAuth ni token personal. Esta app requiere Android 8 o posterior. El SDK está destinado a juegos que lo integran. Esta aplicación observa otras apps, y no es un juego que incorpore el SDK. El paquete SDK se obtiene en Developer Portal para una aplicación configurada según [Getting Started](https://discord.com/developers/docs/social-sdk/getting_started.html); no es dependencia pública disponible en este entorno. La referencia de Activity especifica que la actividad pertenece al juego actual y que su nombre e identificador de aplicación no pueden elegirse libremente desde el SDK. Así que el SDK oficial no ofrece una vía documentada para que esta app anuncie "Playing Free Fire/Roblox/Minecraft" cuando esos juegos no integran SDK. No se afirma que Discord admita este caso de uso. No se usan user tokens, RPC legado, endpoints privados o automatización.

Alternativas legítimas: presencia nativa publicada por el juego, solicitar al desarrollador integrar Social SDK o aportar/disponer del paquete oficial y confirmar que sus términos permiten esta integración. La capa Discord está aislada y no declara éxito ficticio.

## Funciones

- Catálogo editable por nombre/package name; incluye Free Fire, Roblox, Minecraft.
- `UsageStatsManager` local: compara solo packages configurados, eventos `ACTIVITY_RESUMED` y `ACTIVITY_PAUSED`.
- Servicio foreground con notificación, inicio/detención, DataStore; Settings para Usage Access.
- Sin historial, telemetría ni transmisión de uso de otras aplicaciones.

## Compilar

Android Studio con JDK 17 y Android SDK Platform 35; o Gradle 8.11.1:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Actions ejecuta unit tests, lint y build debug, y publica artifact. Tags `v*` generan APK release; queda sin firmar si no se configuran secretos. Para firmar, define `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS` y `RELEASE_KEY_PASSWORD` en Actions Secrets; el archivo temporal se elimina al terminar y no se registran las claves.

## Instalar

1. Instalar APK en Android 8+.
2. En Ajustes > Acceso de uso, habilitar Farllirs Presence.
3. Regresar y tocar **Iniciar**; la notificación persistente indica el servicio activo.
4. En Configuración, añadir/retirar juegos usando package name.
5. **Detener** para cerrar el servicio.

Samsung puede suspender servicios: en Ajustes > Aplicaciones > Farllirs Presence > Batería, permitir actividad en segundo plano/sin restricciones. Android 14 exige tipo de Foreground Service y declara `specialUse`; Play puede requerir justificarlo. Reinicio automático se deja desactivado.

## Pruebas y limitaciones

Unit tests cubren paquetes de juego, apps ajenas, salida, cambio entre juegos y múltiples activities. No sustituyen pruebas reales de permiso/dispositivo. Informe en [REPORT-TESTS.md](REPORT-TESTS.md). La validación física en Galaxy A23 Android 14 y publicación real Discord están pendientes. `UsageStatsManager` registra eventos de apps; solo el catálogo determina qué se trata como juego y el usuario debe conceder Usage Access.
