package dev.farllirs.presence.detector
class ForegroundReducer(private val knownPackages:Set<String>) {
 private val active=linkedSetOf<String>(); private var foreground:String?=null
 fun resumed(pkg:String, cls:String):String? { active += "$pkg/$cls"; foreground=pkg.takeIf { it in knownPackages }; return foreground }
 fun paused(pkg:String, cls:String):String? { active -= "$pkg/$cls"; if(foreground==pkg && active.none { it.startsWith("$pkg/") }) foreground=active.lastOrNull()?.substringBefore('/')?.takeIf { it in knownPackages }; return foreground }
 fun clear():String? { active.clear(); foreground=null; return null }
 fun current()=foreground
}
