package dev.farllirs.presence.detector
import org.junit.Assert.assertEquals
import org.junit.Test
class ForegroundReducerTest {
 private fun state()=ForegroundReducer(setOf("free.fire","roblox","minecraft"))
 @Test fun recognizesCatalogAndIgnoresOtherApps(){val r=state();assertEquals(null,r.resumed("settings","Main"));assertEquals("free.fire",r.resumed("free.fire","Game"))}
 @Test fun exitClearsGame(){val r=state();r.resumed("minecraft","Main");assertEquals(null,r.paused("minecraft","Main"))}
 @Test fun switchingGamesRetainsLatest(){val r=state();r.resumed("free.fire","Game");assertEquals("roblox",r.resumed("roblox","Game"));assertEquals("roblox",r.paused("free.fire","Game"))}
 @Test fun multipleActivitiesAreHandled(){val r=state();r.resumed("minecraft","Main");r.resumed("minecraft","World");assertEquals("minecraft",r.paused("minecraft","Main"))}
 @Test fun unknownGameIsIgnored(){assertEquals(null,state().resumed("unknown.game","Main"))}
}
