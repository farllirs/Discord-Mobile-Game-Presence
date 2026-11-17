package dev.farllirs.presence.media

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Track metadata stays in memory; it is never written to storage or sent over the network. */
data class MediaSource(
    val packageName: String,
    val appName: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val artwork: Bitmap?,
    val playing: Boolean
)

data class MediaSessionState(val sources: List<MediaSource> = emptyList(), val current: MediaSource? = null)

object MediaSessionStore {
    private val mutableState = MutableStateFlow(MediaSessionState())
    val state = mutableState.asStateFlow()

    fun publish(sources: List<MediaSource>) {
        val active = sources.firstOrNull { it.playing && MediaSourcePreferences.isEnabled(it.packageName) }
        mutableState.value = MediaSessionState(sources, active)
    }

    fun select(packageName: String, allPackages: Set<String>) {
        MediaSourcePreferences.toggle(packageName, allPackages)
        publish(state.value.sources)
    }
}

object MediaSourcePreferences {
    private const val PREFS = "media_sources"
    private const val KEY = "enabled_packages"

    fun isEnabled(packageName: String): Boolean {
        val prefs = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return true
        if (!prefs.contains(KEY)) return true
        return prefs.getStringSet(KEY, emptySet()).orEmpty().contains(packageName)
    }

    @Volatile private var appContext: Context? = null
    fun initialize(context: Context) { appContext = context.applicationContext }

    fun toggle(packageName: String, allPackages: Set<String>) {
        val context = appContext ?: return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = if (prefs.contains(KEY)) prefs.getStringSet(KEY, emptySet()).orEmpty() else allPackages
        val updated = existing.toMutableSet().apply { if (contains(packageName)) remove(packageName) else add(packageName) }
        if (updated.containsAll(allPackages)) prefs.edit().remove(KEY).apply()
        else prefs.edit().putStringSet(KEY, updated).apply()
    }
}
