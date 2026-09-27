package com.novastore.app.ui.detail

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Download
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
import com.novastore.app.data.Preferences
import com.novastore.app.data.ReleaseInfo
import com.novastore.app.i18n.Strings
import com.novastore.app.ui.main.categoryLabel
import com.novastore.app.util.ApkInstaller
import com.novastore.app.util.AppIcon
import com.novastore.app.util.AppUtils
import com.novastore.app.util.DownloadService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    packageName: String,
    language: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { Preferences(context) }
    val app = Catalog.apps.firstOrNull { it.packageName == packageName }
    val scope = rememberCoroutineScope()
    var releases by remember { mutableStateOf<List<ReleaseInfo>>(emptyList()) }
    var selectedRelease by remember { mutableStateOf<ReleaseInfo?>(null) }
    var loading by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("") }
    var isInstalled by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var downloading by remember { mutableStateOf(false) }
    var downloadId by remember { mutableStateOf<Long?>(null) }
    var needsPermission by remember { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        if (app != null) {
            isInstalled = AppUtils.isInstalled(context, app.packageName)
            needsPermission = !ApkInstaller.hasInstallPermission(context)
            if (!app.fdroid && app.github != null) {
                releases = GitHubApi.getReleases(app.github!!)
                selectedRelease = releases.firstOrNull()
            }
            val savedId = prefs.getDownloadId()
            val savedApp = prefs.getDownloadApp()
            if (savedId > 0 && savedApp == app.packageName) {
                downloadId = savedId
                downloading = true
            }
        }
        loading = false
    }

    LaunchedEffect(downloadId) {
        val id = downloadId ?: return@LaunchedEffect
        while (true) {
            val p = DownloadService.getProgress(context, id)
            progress = p.percent
            if (p.status == DownloadManager.STATUS_SUCCESSFUL) {
                downloading = false
                status = Strings.get(language, "download_finished")
                val file = File(
                    android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    ),
                    "NovaStore/${app?.name}.apk".replace(" ", "_")
                )
                if (file.exists()) ApkInstaller.installApk(context, file)
                prefs.clearDownload()
                break
            }
            if (p.status == DownloadManager.STATUS_FAILED) {
                downloading = false
                status = Strings.get(language, "download_failed")
                prefs.clearDownload()
                break
            }
            delay(500)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app?.name ?: "—") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (app == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Strings.get(language, "app_not_found"))
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
            AssistChip(onClick = {}, label = { Text(categoryLabel(language, app.category)) })
            Spacer(Modifier.height(20.dp))
            Text(app.description(language), style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(24.dp))

            if (needsPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(Strings.get(language, "needs_permission"), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            Strings.get(language, "needs_permission_desc"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { ApkInstaller.requestInstallPermission(context) }) {
                            Text(Strings.get(language, "allow"))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(Strings.get(language, "loading"))
                        }
                    } else {
                        if (releases.isNotEmpty() && !downloading) {
                            Text(Strings.get(language, "version"), style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedRelease?.version ?: "—")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    releases.forEach { rel ->
                                        DropdownMenuItem(
                                            text = { Text(rel.version) },
                                            onClick = {
                                                selectedRelease = rel
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        if (downloading) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        downloading = true
                                        progress = 0f
                                        status = ""
                                        val url = selectedRelease?.apkUrl
                                            ?: Downloader.resolveApkUrl(app)
                                        if (url != null) {
                                            val fileName = "${app.name}.apk".replace(" ", "_")
                                            val id = DownloadService.enqueue(context, url, fileName)
                                            downloadId = id
                                            prefs.setDownloadId(id)
                                            prefs.setDownloadApp(app.packageName)
                                        } else {
                                            downloading = false
                                            status = Strings.get(language, "link_not_found")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    if (isInstalled) Icons.Filled.Update else Icons.Filled.Download,
                                    null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isInstalled) Strings.get(language, "update")
                                    else Strings.get(language, "install")
                                )
                            }
                        }

                        if (status.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(status, style = MaterialTheme.typography.bodySmall)
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
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null)
                    Spacer(Modifier.width(8.dp))
                    Text(Strings.get(language, "open_github"))
                }
            }
        }
    }
}
