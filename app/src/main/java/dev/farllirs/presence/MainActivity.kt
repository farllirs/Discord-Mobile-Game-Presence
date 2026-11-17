package dev.farllirs.presence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.farllirs.presence.data.GameEntry
import dev.farllirs.presence.data.PreferencesRepository
import dev.farllirs.presence.discord.DiscordConnection
import dev.farllirs.presence.discord.DiscordPresenceManager
import dev.farllirs.presence.media.MediaSessionStore
import dev.farllirs.presence.media.MediaSourcePreferences
import dev.farllirs.presence.permissions.PermissionManager
import dev.farllirs.presence.service.GameDetectionService
import kotlinx.coroutines.launch

private val background = Color(0xFF100D12)
private val card = Color(0xFF211920)
private val raised = Color(0xFF2A1D25)
private val red = Color(0xFFFF5574)
private val cyan = Color(0xFF78E6DC)
private val muted = Color(0xFFB5AAB2)

private enum class AppMode { GAMES, MUSIC, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PresenceScreen() }
    }

    @Composable
    private fun PresenceScreen() {
        val repository = remember { PreferencesRepository(applicationContext) }
        val games by repository.games.collectAsState(initial = emptyList())
        val media by MediaSessionStore.state.collectAsState()
        val scope = rememberCoroutineScope()
        var mode by remember { mutableStateOf(AppMode.GAMES) }
        var usageAccess by remember { mutableStateOf(PermissionManager.hasUsageAccess(this)) }
        var mediaAccess by remember { mutableStateOf(hasMediaAccess()) }
        var detectionRunning by remember { mutableStateOf(false) }
        var detectedGame by remember { mutableStateOf<String?>(null) }
        var addGame by remember { mutableStateOf(false) }
        var gameName by remember { mutableStateOf("") }
        var packageName by remember { mutableStateOf("") }
        val owner = LocalLifecycleOwner.current
        val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

        DisposableEffect(owner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    usageAccess = PermissionManager.hasUsageAccess(this@MainActivity)
                    mediaAccess = hasMediaAccess()
                }
            }
            owner.lifecycle.addObserver(observer)
            onDispose { owner.lifecycle.removeObserver(observer) }
        }
        DisposableEffect(Unit) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    detectedGame = intent?.getStringExtra(GameDetectionService.EXTRA_GAME)
                }
            }
            ContextCompat.registerReceiver(this@MainActivity, receiver, IntentFilter(GameDetectionService.ACTION_STATUS), ContextCompat.RECEIVER_NOT_EXPORTED)
            onDispose { unregisterReceiver(receiver) }
        }

        MaterialTheme(colorScheme = darkColorScheme(background = background, surface = card, primary = red, secondary = cyan, onBackground = Color.White, onSurface = Color.White)) {
            Scaffold(containerColor = background) { insets ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(insets).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 18.dp, bottom = 30.dp)
                ) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("TETO", color = red, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    Text("// XZP", color = cyan, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                                Text("FARLLIRS · SIGNAL HUB", color = muted, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                            }
                            TextButton(onClick = { mode = AppMode.SETTINGS }) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = cyan)
                                Spacer(Modifier.width(6.dp))
                                Text("AJUSTES", color = cyan, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(raised).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            ModeButton("JUEGOS", mode == AppMode.GAMES, Modifier.weight(1f)) { mode = AppMode.GAMES }
                            ModeButton("MÚSICA", mode == AppMode.MUSIC, Modifier.weight(1f)) { mode = AppMode.MUSIC }
                            ModeButton("AJUSTES", mode == AppMode.SETTINGS, Modifier.weight(1f)) { mode = AppMode.SETTINGS }
                        }
                    }
                    when (mode) {
                        AppMode.GAMES -> {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(22.dp)) {
                                    Column(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF321C28), card))).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("01 / GAME RADAR", color = red, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelLarge)
                                        Text(detectedGame ?: if (detectionRunning) "Escaneando en primer plano…" else "Sin juego detectado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                        Text(if (detectionRunning) "RADAR ACTIVO · SOLO TU CATÁLOGO" else "RADAR EN PAUSA", color = cyan, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Button(onClick = {
                                                if (usageAccess) {
                                                    if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                    startForegroundService(Intent(this@MainActivity, GameDetectionService::class.java).setAction(GameDetectionService.ACTION_START))
                                                    detectionRunning = true
                                                } else startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                            }, modifier = Modifier.weight(1f)) { Text(if (usageAccess) "Iniciar radar" else "Dar acceso") }
                                            OutlinedButton(onClick = {
                                                stopService(Intent(this@MainActivity, GameDetectionService::class.java))
                                                detectionRunning = false
                                                detectedGame = null
                                            }, modifier = Modifier.weight(1f)) { Text("Detener") }
                                        }
                                        Text(if (usageAccess) "Acceso de uso: listo" else "Android requiere que habilites Acceso de uso", color = muted, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            item { DiscordStatusCard(this@MainActivity) }
                        }
                        AppMode.MUSIC -> {
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(22.dp)) {
                                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Text("02 / AUDIO DECK", color = cyan, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelLarge)
                                        val current = media.current
                                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(112.dp).clip(RoundedCornerShape(18.dp)).background(raised), contentAlignment = Alignment.Center) {
                                                if (current?.artwork != null) Image(current.artwork.asImageBitmap(), contentDescription = "Carátula de ${current.title ?: "la canción"}", modifier = Modifier.fillMaxSize())
                                                else Icon(Icons.Default.MusicNote, contentDescription = null, tint = red, modifier = Modifier.size(48.dp))
                                            }
                                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                                Text(current?.title ?: if (mediaAccess) "Esperando reproducción" else "Conecta tu reproductor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                                Text(current?.artist ?: "Título y artista", color = muted)
                                                Text(current?.appName ?: "App multimedia", color = cyan, style = MaterialTheme.typography.labelMedium)
                                                current?.album?.let { Text(it, color = muted, style = MaterialTheme.typography.bodySmall) }
                                            }
                                        }
                                        Button(onClick = {
                                            if (!mediaAccess) startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                            else mode = AppMode.SETTINGS
                                        }, modifier = Modifier.fillMaxWidth()) {
                                            Text(if (mediaAccess) "Elegir fuentes multimedia" else "Permitir acceso a música")
                                        }
                                        Text("La app lee solo metadatos de sesiones multimedia activas. No guarda canciones ni analiza notificaciones.", color = muted, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            item { DiscordStatusCard(this@MainActivity, listeningMode = true) }
                        }
                        AppMode.SETTINGS -> {
                            item { Text("CONFIGURACIÓN · FARLLIRS / XZP", color = red, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace) }
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(18.dp)) {
                                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("JUEGOS", color = cyan, fontFamily = FontFamily.Monospace)
                                        Text("Se reconocen únicamente los packages que agregues aquí.", color = muted, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            items(games, key = { it.packageName }) { game ->
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(card).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) { Text(game.displayName, fontWeight = FontWeight.Bold); Text(game.packageName, color = muted, style = MaterialTheme.typography.labelSmall) }
                                    TextButton(onClick = { scope.launch { repository.saveGames(games.filterNot { it.packageName == game.packageName }) } }) { Text("Quitar", color = red) }
                                }
                            }
                            item { OutlinedButton(onClick = { addGame = true }, modifier = Modifier.fillMaxWidth()) { Text("+ Añadir juego") } }
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(18.dp)) {
                                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("FUENTES DE MÚSICA", color = cyan, fontFamily = FontFamily.Monospace)
                                        Text(if (mediaAccess) "Acceso multimedia habilitado" else "Permiso especial requerido para leer título, artista y carátula de sesiones activas.", color = muted, style = MaterialTheme.typography.bodySmall)
                                        if (!mediaAccess) OutlinedButton(onClick = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) { Text("Abrir acceso multimedia") }
                                        if (media.sources.isEmpty()) Text("Abre un reproductor para descubrirlo aquí.", color = muted)
                                    }
                                }
                            }
                            items(media.sources, key = { "media:${it.packageName}" }) { source ->
                                val allPackages = media.sources.map { it.packageName }.toSet()
                                val enabled = MediaSourcePreferences.isEnabled(source.packageName)
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(card).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) { Text(source.appName, fontWeight = FontWeight.Bold); Text(source.packageName, color = muted, style = MaterialTheme.typography.labelSmall) }
                                    Switch(checked = enabled, onCheckedChange = { MediaSessionStore.select(source.packageName, allPackages) })
                                }
                            }
                            item { DiscordStatusCard(this@MainActivity) }
                        }
                    }
                }
            }
        }
        if (addGame) AlertDialog(
            onDismissRequest = { addGame = false },
            title = { Text("Añadir juego") },
            text = { Column { OutlinedTextField(gameName, { gameName = it }, label = { Text("Nombre visible") }); OutlinedTextField(packageName, { packageName = it }, label = { Text("Package name") }) } },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { GameEntry(packageName.trim(), gameName.trim()) }.onSuccess { entry ->
                        scope.launch {
                            repository.saveGames((games.filterNot { it.packageName == entry.packageName } + entry).sortedBy { it.displayName })
                            addGame = false
                            gameName = ""
                            packageName = ""
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { addGame = false }) { Text("Cancelar") } }
        )
    }

    @Composable
    private fun ModeButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
        TextButton(onClick = onClick, modifier = modifier.background(if (selected) red else Color.Transparent, RoundedCornerShape(12.dp))) {
            Text(label, color = if (selected) background else muted, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }

    @Composable
    private fun DiscordStatusCard(context: Context, listeningMode: Boolean = false) {
        val status = remember { DiscordPresenceManager(context).connection() }
        Card(colors = CardDefaults.cardColors(containerColor = card), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (listeningMode) "DISCORD / LISTENING" else "DISCORD / PLAYING", color = red, fontFamily = FontFamily.Monospace)
                Text(if (status == DiscordConnection.DISCORD_NOT_INSTALLED) "Discord no está instalado" else "Publicación oficial no disponible", fontWeight = FontWeight.SemiBold)
                Text(
                    if (listeningMode) "Android puede leer la música; el Social SDK de Discord no documenta publicar audio de otras apps como Listening."
                    else "Discord Social SDK solo asocia actividad al juego que lo integra. No publicaremos juegos externos como si fueran el juego de esta app.",
                    color = muted, style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    private fun hasMediaAccess(): Boolean = Build.VERSION.SDK_INT >= 27 && getSystemService(NotificationManager::class.java)
        .isNotificationListenerAccessGranted(ComponentName(this, dev.farllirs.presence.media.MediaListenerService::class.java))
}
