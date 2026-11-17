package dev.farllirs.presence.detector
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
class GameDetector(private val stats:UsageStatsManager) {
 private var cursor=System.currentTimeMillis()-30_000L; private var packages:Set<String> = emptySet(); private var reducer=ForegroundReducer(packages)
 fun poll(known:Set<String>):String? { if(packages!=known) { packages=known; reducer=ForegroundReducer(known); cursor=System.currentTimeMillis()-30_000L }; val now=System.currentTimeMillis(); val events=stats.queryEvents(cursor,now)?:return null; val e=UsageEvents.Event(); while(events.hasNextEvent()) { events.getNextEvent(e); when(e.eventType) { UsageEvents.Event.ACTIVITY_RESUMED -> reducer.resumed(e.packageName,e.className?:""); UsageEvents.Event.ACTIVITY_PAUSED -> reducer.paused(e.packageName,e.className?:""); UsageEvents.Event.SCREEN_NON_INTERACTIVE -> reducer.clear() } }; cursor=now; return reducer.current() }
}
