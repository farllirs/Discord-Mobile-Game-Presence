package dev.farllirs.presence.service
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import dev.farllirs.presence.data.PreferencesRepository
import dev.farllirs.presence.detector.GameDetector
import dev.farllirs.presence.permissions.PermissionManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
class GameDetectionService:Service() {
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO); private var detector:GameDetector?=null; private var last:String?="\u0000"; private var pollJob:Job?=null
 override fun onStartCommand(intent:Intent?,flags:Int,id:Int):Int { if(intent?.action==ACTION_STOP){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return START_NOT_STICKY}; startForeground(42,NotificationHelper.create(this,"Detección local activa")); if(!PermissionManager.hasUsageAccess(this)){stopSelf();return START_NOT_STICKY}; if(detector==null)detector=GameDetector(getSystemService(UsageStatsManager::class.java)); if(pollJob==null)pollJob=scope.launch { while(isActive) { val games=PreferencesRepository(this@GameDetectionService).games.first(); val pkg=detector?.poll(games.map { it.packageName }.toSet()); if(pkg!=last){last=pkg; val name=games.firstOrNull { it.packageName==pkg }?.displayName; val msg=name?.let{"Jugando: $it"}?:"No se detecta un juego"; getSystemService(android.app.NotificationManager::class.java).notify(42,NotificationHelper.create(this@GameDetectionService,msg)); sendBroadcast(Intent(ACTION_STATUS).setPackage(packageName).putExtra(EXTRA_GAME,name))}; delay(1000) } }; return START_STICKY }
 override fun onDestroy(){scope.cancel();super.onDestroy()}; override fun onBind(intent:Intent?):IBinder?=null
 companion object { const val ACTION_START="dev.farllirs.presence.START"; const val ACTION_STOP="dev.farllirs.presence.STOP"; const val ACTION_STATUS="dev.farllirs.presence.STATUS"; const val EXTRA_GAME="game_name" }
}
