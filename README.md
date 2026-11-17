# Farllirs Presence — Teto / XZP signal hub

Aplicación Android en Kotlin y Jetpack Compose para alternar entre **radar de juegos** y **audio deck**. La identidad usa Teto, XZP y Farllirs con una paleta grafito, rojo y cian. No emplea arte oficial de Discord ni recopila contenido en un servidor.

> Estado: prototipo en desarrollo. La detección de juegos y la interfaz ya compilan. El modo de música usa `MediaSessionManager` tras habilitar el acceso especial de notificaciones y muestra el tema activo, artista, álbum, carátula y reproductor cuando la app de música publica una sesión Android compatible. La capa Discord permanece deshabilitada: el Social SDK oficial no respalda publicar juegos externos ni un estado Listening arbitrario desde una app auxiliar.

## Modos

- **Juegos:** UsageStatsManager y eventos `ACTIVITY_RESUMED` / `ACTIVITY_PAUSED`. Solo reconoce los packages configurados. Catálogo inicial: Free Fire, Roblox y Minecraft; se puede editar.
- **Música:** descubre reproductores instalados que exponen el servicio multimedia estándar y suma sesiones activas compatibles; permite filtrar qué fuentes aceptar. Lee título, artista, álbum, carátula y nombre de app desde `MediaSessionManager`. No inspecciona texto de notificaciones, no guarda el historial y no envía los metadatos por red.
- **Ajustes:** lista editable de juegos y selector de fuentes de música.

Para el modo Música, Android pide habilitar **Acceso a notificaciones**. Android solo permite enumerar las sesiones activas a una app que tenga ese acceso concedido mediante `NotificationListenerService`; la app luego consulta únicamente los metadatos de sesiones multimedia activas. El catálogo de apps depende de la visibilidad de paquetes Android: se consultan únicamente apps que anuncian el servicio estándar de navegador multimedia; otros reproductores aparecen cuando Android expone su sesión activa.

## Discord: límites comprobados

Discord Social SDK añadió transporte RPC Android que puede publicar presencia en el cliente Discord instalado sin flujo OAuth ni token personal. Sin embargo, su referencia de `Activity` indica que las actividades pertenecen al juego que integra el SDK; el nombre y el identificador de aplicación no se pueden elegir para atribuir la presencia a otra app. La misma referencia dice que, aunque Discord define varios tipos de actividad, para el Social SDK el tipo relevante es `Playing`. La existencia de `Listening` en el enum global no equivale a soporte para que esta app ponga una canción externa como Listening.

Por eso Farllirs Presence **no afirma conexión ni publicación Rich Presence**, y no usa RPC privado/antiguo, tokens personales ni automatización. Para publicar una canción o juego externo, la vía compatible es que Discord o la app/juego de origen proporcione una integración oficial que lo admita. La detección y visualización local funcionan de forma independiente.

Fuentes primarias:

- [Discord Social SDK](https://discord.com/developers/docs/social-sdk/overview)
- [Discord Social SDK: Activity](https://discord.com/developers/docs/social-sdk/classdiscordpp_1_1Activity.html)
- [Discord Social SDK: release notes](https://discord.com/developers/docs/social-sdk/release_notes.html)
- [Android: MediaSessionManager](https://developer.android.com/reference/android/media/session/MediaSessionManager)
- [Android: MediaMetadata](https://developer.android.com/reference/android/media/MediaMetadata)

## Compilar

Requiere Android Studio, JDK 17 y Android SDK Platform 35:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions ejecuta pruebas unitarias, lint y build Debug en push y pull request, y publica el APK como artifact. Un tag `v*` construye un APK Release. Sin secrets, queda sin firmar. Para firmar, configura `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS` y `RELEASE_KEY_PASSWORD` en Actions Secrets; CI borra el keystore temporal al terminar.

## Instalar y permisos

1. Instala el APK en Android 8 o posterior.
2. Para Juegos, habilita **Acceso de uso** desde Ajustes Android cuando la app lo solicite; toca **Iniciar radar**.
3. Para Música, toca **Permitir acceso a música** y habilita Farllirs Presence en **Acceso a notificaciones**. Abre o reproduce audio en un reproductor compatible.
4. En **Ajustes**, edita el catálogo y filtra los reproductores multimedia detectados.
5. Detén el radar cuando no lo necesites. El modo musical deja de recibir sesiones cuando el usuario retira el acceso.

Samsung puede suspender el servicio de detección de juegos; si eso ocurre, permite actividad en segundo plano o batería sin restricciones en Ajustes > Aplicaciones > Farllirs Presence > Batería. Android 14 exige tipo declarado para el servicio foreground; esta app declara `specialUse`. No hay reinicio automático del radar.

## Pruebas y límites conocidos

- Pruebas unitarias: paquetes configurados, apps ajenas, salida, cambio y múltiples Activities.
- GitHub Actions: `testDebugUnitTest`, `lintDebug` y `assembleDebug`.
- Galaxy A23 Android 14: se instaló el APK y se observó Roblox como juego actual en la notificación foreground. La prueba física de Minecraft, Free Fire y modo Música queda pendiente.
- Discord: no se probó publicación porque la vía oficial no soporta los estados externos solicitados. Ver [REPORT-TESTS.md](REPORT-TESTS.md).

`UsageStatsManager` identifica paquetes, no determina por sí mismo qué apps son juegos. `MediaSessionManager` depende de que el reproductor publique una sesión de Android. Ninguno de los dos permisos permite mostrar metadatos a Discord por sí mismo.
