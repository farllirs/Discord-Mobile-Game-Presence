package dev.farllirs.presence.data
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
private val Context.store by preferencesDataStore("farllirs_presence")
class PreferencesRepository(private val context: Context) {
 private val key = stringPreferencesKey("games")
 val games: Flow<List<GameEntry>> = context.store.data.map { p -> runCatching { val a=JSONArray(p[key] ?: JSONArray(GameCatalog.defaults.map { JSONObject().put("package",it.packageName).put("name",it.displayName) }).toString()); (0 until a.length()).map { a.getJSONObject(it).let { o -> GameEntry(o.getString("package"),o.getString("name")) } } }.getOrDefault(GameCatalog.defaults) }
 suspend fun saveGames(games:List<GameEntry>) { val a=JSONArray().apply { games.forEach { put(JSONObject().put("package",it.packageName).put("name",it.displayName)) } }; context.store.edit { it[key]=a.toString() } }
}
