package com.cosmic.worldboxloader

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private data class GbMod(val id: String, val name: String, val pageUrl: String)

private object GameBananaApi {
    private const val BASE = "https://gamebanana.com/apiv11"

    private fun getJson(path: String): JSONObject {
        val connection = URL("$BASE/$path").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12000
        connection.readTimeout = 15000
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "WorldBoxLoader/0.2 Android")
        try {
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            if (body.isBlank()) throw IllegalStateException("GameBanana returned an empty response")
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun records(json: JSONObject): List<JSONObject> {
        val array = json.optJSONArray("_aRecords") ?: return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val value = array.opt(i)
                if (value is JSONObject) add(value)
            }
        }
    }

    suspend fun search(query: String): List<GbMod> = withContext(Dispatchers.IO) {
        val gameQuery = URLEncoder.encode("WorldBox", "UTF-8")
        val gameJson = getJson("Util/Search/Results?_sSearchString=$gameQuery&_sModelName=Game&_nPage=1")
        val game = records(gameJson).firstOrNull {
            it.optString("_sName").contains("worldbox", ignoreCase = true) ||
                it.optString("_sTitle").contains("worldbox", ignoreCase = true)
        } ?: throw IllegalStateException("Could not identify the WorldBox game entry on GameBanana")

        val gameId = game.optString("_idRow").ifBlank { game.optString("_id") }
        if (gameId.isBlank()) throw IllegalStateException("GameBanana did not return a WorldBox game ID")

        val encoded = URLEncoder.encode(query.ifBlank { " " }, "UTF-8")
        val result = getJson("Util/Search/Results?_sSearchString=$encoded&_idGameRow=$gameId&_sModelName=Mod&_nPage=1")
        records(result).mapNotNull { item ->
            val id = item.optString("_idRow").ifBlank { item.optString("_id") }
            val name = item.optString("_sName").ifBlank { item.optString("_sTitle") }
            if (id.isBlank() || name.isBlank()) null else {
                val page = item.optString("_sProfileUrl").ifBlank { "https://gamebanana.com/mods/$id" }
                GbMod(id, name, page)
            }
        }
    }
}

@Composable
fun GameBananaBrowserScreen(onImportDownloaded: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("ModernBox") }
    var results by remember { mutableStateOf<List<GbMod>>(emptyList()) }
    var status by remember { mutableStateOf("Search WorldBox mods on GameBanana.") }
    var loading by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("GameBanana", style = MaterialTheme.typography.headlineSmall)
        Text("Browse WorldBox submissions. DLL files must match your game build and runtime.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search mods") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    loading = true
                    status = "Searching GameBanana…"
                    scope.launch {
                        try {
                            results = GameBananaApi.search(query)
                            status = if (results.isEmpty()) "No matching mods found." else "${results.size} result(s) found."
                        } catch (error: Exception) {
                            status = error.message ?: "Could not reach GameBanana."
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading
            ) { Text(if (loading) "Searching…" else "Search") }
            OutlinedButton(onClick = onImportDownloaded) { Text("Import downloaded DLL") }
        }
        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results, key = { it.id }) { mod ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(mod.name, style = MaterialTheme.typography.titleMedium)
                        Text("GameBanana mod #${mod.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mod.pageUrl)))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Open mod page") }
                    }
                }
            }
        }
        Text(
            "This version opens the official mod page for manual download. Imported DLLs are stored in the loader library; they are not injected into WorldBox yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
