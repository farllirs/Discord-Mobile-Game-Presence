package dev.farllirs.presence
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.farllirs.presence.data.GameEntry
import dev.farllirs.presence.data.PreferencesRepository
import dev.farllirs.presence.discord.DiscordPresenceManager
import dev.farllirs.presence.permissions.PermissionManager
import dev.farllirs.presence.service.GameDetectionService
import kotlinx.coroutines.launch
private val ink=Color(0xFF100D18); private val panel=Color(0xFF1B1726); private val purple=Color(0xFFB58CFF); private val mint=Color(0xFF73F0CB)
class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{Screen()} }
 @Composable private fun Screen(){ val repo=remember{PreferencesRepository(applicationContext)}; val games by repo.games.collectAsState(initial=emptyList()); val scope=rememberCoroutineScope(); var access by remember{mutableStateOf(PermissionManager.hasUsageAccess(this))}; var running by remember{mutableStateOf(false)}; var currentGame by remember{mutableStateOf<String?>(null)}; var adding by remember{mutableStateOf(false)}; var settings by remember{mutableStateOf(false)}; var name by remember{mutableStateOf("")}; var pkg by remember{mutableStateOf("")}; val owner=LocalLifecycleOwner.current
 val notificationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
 DisposableEffect(owner){val obs=LifecycleEventObserver{_,e->if(e==Lifecycle.Event.ON_RESUME)access=PermissionManager.hasUsageAccess(this@MainActivity)};owner.lifecycle.addObserver(obs);onDispose{owner.lifecycle.removeObserver(obs)}}
 DisposableEffect(Unit){val receiver=object:BroadcastReceiver(){override fun onReceive(context:Context?,intent:Intent?){currentGame=intent?.getStringExtra(GameDetectionService.EXTRA_GAME)}};val filter=IntentFilter(GameDetectionService.ACTION_STATUS);if(android.os.Build.VERSION.SDK_INT>=33)registerReceiver(receiver,filter,Context.RECEIVER_NOT_EXPORTED) else registerReceiver(receiver,filter);onDispose{unregisterReceiver(receiver)}}
 MaterialTheme(colorScheme=darkColorScheme(background=ink,surface=panel,primary=purple,secondary=mint,onBackground=Color.White,onSurface=Color.White)){Scaffold(containerColor=ink){padding->LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(16.dp),contentPadding=PaddingValues(top=24.dp,bottom=30.dp)){
 item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("FARLLIRS / XZP",color=purple,fontWeight=FontWeight.Black);Text("Presence",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)};IconButton(onClick={settings=!settings}){Icon(Icons.Default.Settings,"Configuración",tint=purple)}}}
 item{Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(24.dp)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("SESIÓN ACTUAL",color=mint);Text(currentGame?:if(running)"Buscando juego…" else "Sin juego detectado",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(if(running)"DETECCIÓN ACTIVA" else "DETECCIÓN DETENIDA",color=if(running)mint else purple);Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Button(onClick={if(access){if(android.os.Build.VERSION.SDK_INT>=33)notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS);startForegroundService(Intent(this@MainActivity,GameDetectionService::class.java).setAction(GameDetectionService.ACTION_START));running=true}else startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))},modifier=Modifier.weight(1f)){Text(if(access)"Iniciar" else "Dar acceso")};OutlinedButton(onClick={stopService(Intent(this@MainActivity,GameDetectionService::class.java));running=false;currentGame=null},modifier=Modifier.weight(1f)){Text("Detener")}}}}
 item{Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("DISCORD",color=purple);val discord=remember{DiscordPresenceManager(this@MainActivity).connection()};Text(if(discord==dev.farllirs.presence.discord.DiscordConnection.DISCORD_NOT_INSTALLED)"Discord no está instalado" else "SDK oficial aún no configurado",fontWeight=FontWeight.SemiBold);Text("La detección local está separada. No se publica actividad hasta verificar una conexión real.",color=Color.LightGray,style=MaterialTheme.typography.bodySmall)}}
 if(settings){item{Text("JUEGOS RECONOCIDOS",color=mint,style=MaterialTheme.typography.titleMedium)};item{Text("Solo se comparan estos package names; no se recopilan datos de otras apps.",color=Color.LightGray,style=MaterialTheme.typography.bodySmall)};items(games,key={it.packageName}){g->Row(Modifier.fillMaxWidth().background(panel,RoundedCornerShape(14.dp)).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(g.displayName,fontWeight=FontWeight.Bold);Text(g.packageName,color=Color.LightGray,style=MaterialTheme.typography.labelSmall)};TextButton(onClick={scope.launch{repo.saveGames(games.filterNot{it.packageName==g.packageName})}}){Text("Quitar")}}};item{OutlinedButton(onClick={adding=true},modifier=Modifier.fillMaxWidth()){Text("Añadir juego")}};item{Text(if(access)"Acceso a estadísticas concedido" else "Acceso a estadísticas requerido",color=if(access)mint else purple)};item{TextButton(onClick={startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))}){Text("Configurar acceso de uso")}}}
 }} }}
 if(adding)AlertDialog(onDismissRequest={adding=false},title={Text("Añadir juego")},text={Column{OutlinedTextField(name,{name=it},label={Text("Nombre visible")});OutlinedTextField(pkg,{pkg=it},label={Text("Package name")})}},confirmButton={TextButton(onClick={runCatching{GameEntry(pkg.trim(),name.trim())}.onSuccess{entry->scope.launch{repo.saveGames(games.filterNot{it.packageName==entry.packageName}+entry);adding=false;name="";pkg=""}}}){Text("Guardar")}},dismissButton={TextButton(onClick={adding=false}){Text("Cancelar")}})
 }
}
