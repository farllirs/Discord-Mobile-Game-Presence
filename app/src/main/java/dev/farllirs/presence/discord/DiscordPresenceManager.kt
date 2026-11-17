package dev.farllirs.presence.discord
import android.content.Context
import android.content.pm.PackageManager
class DiscordPresenceManager(private val context:Context) {
 @Suppress("DEPRECATION")
 fun connection():DiscordConnection { val installed=try { if(android.os.Build.VERSION.SDK_INT>=33) context.packageManager.getPackageInfo("com.discord",PackageManager.PackageInfoFlags.of(0)) else context.packageManager.getPackageInfo("com.discord",0); true } catch(_:PackageManager.NameNotFoundException) { false }; if(!installed)return DiscordConnection.DISCORD_NOT_INSTALLED; if(!sdkBundled)return DiscordConnection.SDK_NOT_BUNDLED; return DiscordConnection.READY }
 fun publish(game:String):DiscordConnection=connection()
 private val sdkBundled=false // Requires the official portal package and a registered application; never claim success without SDK callbacks.
}
