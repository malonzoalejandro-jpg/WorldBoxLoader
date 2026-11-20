package com.cosmic.worldboxloader

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.util.zip.ZipInputStream

class MainActivity : ComponentActivity() {
    external fun nativeLoaderStatus(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("worldboxloader")
        setContent {
            LoaderApp(
                onImport = { uri -> importMod(uri) },
                mods = remember { mutableStateListOf<String>() },
                nativeStatus = nativeLoaderStatus()
            )
        }
    }

    private fun importMod(uri: Uri): String {
        val resolver = contentResolver
        val name = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
        } ?: "imported_mod"
        val safeName = name.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._ -]"), "_")
        val modsDir = File(filesDir, "mods").apply { mkdirs() }
        val destination = File(modsDir, safeName)
        resolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: return "Could not open selected file"
        if (safeName.endsWith(".zip", ignoreCase = true)) {
            val outDir = File(modsDir, safeName.removeSuffix(".zip").removeSuffix(".ZIP"))
            outDir.mkdirs()
            try {
                ZipInputStream(destination.inputStream()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val out = File(outDir, entry.name).canonicalFile
                        if (!out.path.startsWith(outDir.canonicalPath + File.separator) &&
                            out != outDir.canonicalFile) {
                            throw SecurityException("Unsafe ZIP entry path")
                        }
                        if (entry.isDirectory) out.mkdirs() else {
                            out.parentFile?.mkdirs()
                            out.outputStream().use { zip.copyTo(it) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
                return "Imported and extracted $safeName"
            } catch (e: Exception) {
                outDir.deleteRecursively()
                return "ZIP import failed: ${e.message}"
            }
        }
        return "Imported $safeName"
    }
}

@Composable
private fun LoaderApp(
    onImport: (Uri) -> String,
    mods: MutableList<String>,
    nativeStatus: String
) {
    val bg = Color(0xFF0B0D12)
    val panel = Color(0xFF151923)
    val accent = Color(0xFF8B6CFF)
    var page by remember { mutableStateOf("Home") }
    var message by remember { mutableStateOf("Your mod library is ready.") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val result = onImport(uri)
            message = result
            if (result.startsWith("Imported")) mods.add(result.removePrefix("Imported ").removePrefix("and extracted "))
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = bg, surface = panel, primary = accent,
            onBackground = Color(0xFFF4F1FF), onSurface = Color(0xFFF4F1FF)
        )
    ) {
        Scaffold(containerColor = bg, bottomBar = {
            NavigationBar(containerColor = panel) {
                listOf("Home", "Browse", "Mods", "Setup").forEach { item ->
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { page = item },
                        icon = {
                            Icon(
                                when (item) {
                                    "Home" -> Icons.Default.Home
                                    "Mods" -> Icons.Default.Folder
                                    "Browse" -> Icons.Default.Public
                                    else -> Icons.Default.Settings
                                }, contentDescription = item
                            )
                        },
                        label = { Text(item) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = Color.White,
                            indicatorColor = Color(0xFF29213F)
                        )
                    )
                }
            }
        }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text("WORLDBOX", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Text("Loader", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(22.dp))
                when (page) {
                    "Home" -> {
                        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(22.dp)) {
                            Column(Modifier.padding(20.dp)) {
                                Text("MOD LIBRARY", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("Keep your mods organized.", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                                Text("Import ZIP content packs and keep DLL files in one place.", color = Color.LightGray)
                                Spacer(Modifier.height(18.dp))
                                Button(onClick = { picker.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.Add, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Import mod")
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(message, color = Color(0xFFB8B2C9), fontSize = 13.sp)
                        Spacer(Modifier.height(14.dp))
                        Text("Library overview", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${mods.size} imported item(s)", color = Color.LightGray)
                    }
                    "Browse" -> {
                        GameBananaBrowserScreen(onImportDownloaded = { picker.launch(arrayOf("*/*")) })
                    }
                    "Mods" -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Your mods", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            FilledTonalButton(onClick = { picker.launch(arrayOf("*/*")) }) { Text("Import") }
                        }
                        Spacer(Modifier.height(12.dp))
                        if (mods.isEmpty()) {
                            Text("Nothing here yet. Import a ZIP or DLL to add it to your library.", color = Color.LightGray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(mods) { mod ->
                                    Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                                        Text(mod, Modifier.fillMaxWidth().padding(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                            Column(Modifier.padding(18.dp)) {
                                Text("Integration status", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(nativeStatus, color = Color.LightGray, fontSize = 13.sp)
                                Spacer(Modifier.height(16.dp))
                                Text("Important", color = accent, fontWeight = FontWeight.Bold)
                                Text("This build is a mod-file manager only. DLL execution requires a compatible IL2CPP bridge and game-side integration. No game patching or code injection is included.", color = Color.LightGray, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
