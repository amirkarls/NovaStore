package com.novastore.app.ui.detail

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.novastore.app.data.Catalog
import com.novastore.app.data.Downloader
import com.novastore.app.data.GitHubApi
import com.novastore.app.data.ReleaseInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(packageName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = Catalog.apps.firstOrNull { it.packageName == packageName }
    val scope = rememberCoroutineScope()
    var release by remember { mutableStateOf<ReleaseInfo?>(null) }
    var loading by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("") }

    LaunchedEffect(packageName) {
        if (app != null) {
            release = GitHubApi.getLatestRelease(app.github)
        }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app?.name ?: "—") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (app == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Приложение не найдено")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = app.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(app.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            AssistChip(onClick = {}, label = { Text(app.category) })
            Spacer(Modifier.height(20.dp))
            Text(app.descriptionRu, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                app.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Загрузка...")
                        }
                    } else if (release?.apkUrl != null) {
                        Text("Версия: ${release!!.version}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Размер: ${"%.1f".format(release!!.size / 1024.0 / 1024.0)} МБ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    status = "Получение ссылки..."
                                    val url = Downloader.resolveApkUrl(app.github) ?: release!!.apkUrl
                                    if (url != null) {
                                        val fileName = "${app.name}-${release!!.version}.apk"
                                            .replace(" ", "_")
                                        Downloader.enqueue(context, url, fileName)
                                        status = "Скачивание началось — проверь уведомления"
                                    } else {
                                        status = "Не удалось найти APK"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Download, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Скачать APK")
                        }
                        if (status.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(status, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Text("Релиз не найден на GitHub")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/${app.github}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.OpenInNew, null)
                Spacer(Modifier.width(8.dp))
                Text("Открыть на GitHub")
            }
        }
    }
}
