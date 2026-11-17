package dev.farllirs.presence.media

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.service.media.MediaBrowserService
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.net.Uri

class MediaListenerService : NotificationListenerService() {
    private var manager: MediaSessionManager? = null
    private val component by lazy { ComponentName(this, MediaListenerService::class.java) }
    private val controllerCallbacks = mutableMapOf<android.media.session.MediaSession.Token, Pair<MediaController, MediaController.Callback>>()
    private var installedMediaProviders: List<MediaSource> = emptyList()
    private val listener = MediaSessionManager.OnActiveSessionsChangedListener { controllers -> refresh(controllers.orEmpty()) }

    override fun onListenerConnected() {
        super.onListenerConnected()
        MediaSourcePreferences.initialize(this)
        installedMediaProviders = discoverMediaProviders()
        manager = getSystemService(MediaSessionManager::class.java)
        manager?.addOnActiveSessionsChangedListener(listener, component)
        refresh(manager?.getActiveSessions(component).orEmpty())
    }

    override fun onListenerDisconnected() {
        manager?.removeOnActiveSessionsChangedListener(listener)
        controllerCallbacks.values.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
        controllerCallbacks.clear()
        manager = null
        MediaSessionStore.publish(emptyList())
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        manager?.removeOnActiveSessionsChangedListener(listener)
        controllerCallbacks.values.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
        controllerCallbacks.clear()
        super.onDestroy()
    }

    private fun refresh(controllers: List<MediaController>) {
        val activeTokens = controllers.map { it.sessionToken }.toSet()
        controllerCallbacks.keys.filterNot { it in activeTokens }.forEach { token ->
            controllerCallbacks.remove(token)?.let { (controller, callback) -> controller.unregisterCallback(callback) }
        }
        controllers.forEach { controller ->
            if (controller.sessionToken !in controllerCallbacks) {
                val callback = object : MediaController.Callback() {
                    override fun onMetadataChanged(metadata: MediaMetadata?) = refresh(manager?.getActiveSessions(component).orEmpty())
                    override fun onPlaybackStateChanged(state: PlaybackState?) = refresh(manager?.getActiveSessions(component).orEmpty())
                }
                controllerCallbacks[controller.sessionToken] = controller to callback
                controller.registerCallback(callback)
            }
        }
        val activeSources = controllers.mapNotNull { controller ->
            runCatching {
                val metadata = controller.metadata
                val appName = runCatching {
                    val info = packageManager.getApplicationInfo(controller.packageName, 0)
                    packageManager.getApplicationLabel(info).toString()
                }.getOrDefault(controller.packageName)
                MediaSource(
                    packageName = controller.packageName,
                    appName = appName,
                    title = metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                        ?: metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
                    artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                        ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST),
                    album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM),
                    artwork = metadata?.let(::loadArtwork),
                    playing = controller.playbackState?.state == PlaybackState.STATE_PLAYING
                )
            }.getOrNull()
        }.distinctBy { it.packageName }
        val activeByPackage = activeSources.associateBy { it.packageName }
        val sources = (installedMediaProviders.map { activeByPackage[it.packageName] ?: it } + activeSources).distinctBy { it.packageName }
        MediaSessionStore.publish(sources)
    }

    @Suppress("DEPRECATION")
    private fun discoverMediaProviders(): List<MediaSource> = runCatching {
        packageManager.queryIntentServices(android.content.Intent(MediaBrowserService.SERVICE_INTERFACE), 0)
            .mapNotNull { it.serviceInfo?.applicationInfo }
            .distinctBy { it.packageName }
            .map { info -> MediaSource(info.packageName, packageManager.getApplicationLabel(info).toString(), null, null, null, null, false) }
    }.getOrDefault(emptyList())

    private fun loadArtwork(metadata: MediaMetadata): Bitmap? {
        metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)?.let { return it }
        metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)?.let { return it }
        val rawUri = metadata.getString(MediaMetadata.METADATA_KEY_ART_URI)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: return null
        return runCatching { contentResolver.openInputStream(Uri.parse(rawUri))?.use { BitmapFactory.decodeStream(it) } }.getOrNull()
    }
}
