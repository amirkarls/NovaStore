package com.novastore.app.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novastore.app.data.Catalog
import com.novastore.app.data.Downloader
import com.novastore.app.data.GitHubApi
import com.novastore.app.data.ReleaseInfo
import com.novastore.app.util.AppIcon
import com.novastore.app.util.AppUtils
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
    var isInstalled by remember { mutableStateOf(false) }
    var hasUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        if (app != null) {
            isInstalled = AppUtils.isInstalled(context, app.packageName)
            if (app.apkUrl == null && !app.fdroid && app.github != null) {
                release = GitHubApi.getLatestRelease(app.github!!)
            }
            hasUpdate = isInstalled
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
            AppIcon(app.name, app.iconUrl, 96)
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
                    when {
                        loading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(12.dp))
                                Text("Загрузка...")
                            }
                        }
                        isInstalled && !hasUpdate -> {
                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false
                            ) {
                                Icon(Icons.Filled.Check, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Установлено")
                            }
                        }
                        isInstalled && hasUpdate -> {
                            Button(
                                onClick = {
                                    scope.launch {
                                        status = "Подготовка..."
                                        val url = Downloader.resolveApkUrl(app)
                                        if (url != null) {
                                            Downloader.enqueue(context, url, "${app.name}.apk".replace(" ", "_"))
                                            status = "Обновление скачивается..."
                                        } else {
                                            status = "Ссылка не найдена"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Update, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Обновить")
                            }
                            if (status.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(status, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    scope.launch {
                                        status = "Подготовка..."
                                        val url = Downloader.resolveApkUrl(app)
                                        if (url != null) {
                                            Downloader.enqueue(context, url, "${app.name}.apk".replace(" ", "_"))
                                            status = "Установка началась..."
                                        } else {
                                            status = "Ссылка не найдена"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Download, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Установить")
                            }
                            if (status.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(status, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (app.github != null) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/${app.github}"))
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
}
