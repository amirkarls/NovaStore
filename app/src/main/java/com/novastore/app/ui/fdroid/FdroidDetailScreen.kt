package com.novastore.app.ui.fdroid

import android.app.DownloadManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novastore.app.data.fdroid.FdroidRepository
import com.novastore.app.data.fdroid.db.FdroidEntity
import com.novastore.app.i18n.Strings
import com.novastore.app.util.ApkInstaller
import com.novastore.app.util.DownloadService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FdroidDetailScreen(
    packageName: String,
    language: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var app by remember { mutableStateOf<FdroidEntity?>(null) }
    var loading by remember { mutableStateOf(true) }
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var status by remember { mutableStateOf("") }
    var downloadId by remember { mutableStateOf<Long?>(null) }
    var needsPermission by remember { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        needsPermission = !ApkInstaller.hasInstallPermission(context)
        app = FdroidRepository.getByPackage(context, packageName)
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
                break
            }
            if (p.status == DownloadManager.STATUS_FAILED) {
                downloading = false
                status = Strings.get(language, "download_failed")
                break
            }
            delay(500)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app?.name ?: "F-Droid") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        app!!.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(app!!.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                app!!.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            AssistChip(onClick = {}, label = { Text(app!!.category) })
            Spacer(Modifier.height(20.dp))
            Text(app!!.summary, style = MaterialTheme.typography.bodyLarge)
            if (app!!.author.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${Strings.get(language, "author")}: ${app!!.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            if (needsPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(Strings.get(language, "needs_permission"), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(Strings.get(language, "needs_permission_desc"), style = MaterialTheme.typography.bodySmall)
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
                    if (app!!.version.isNotBlank()) {
                        Text(
                            "${Strings.get(language, "version")}: ${app!!.version}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    if (downloading) {
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    downloading = true
                                    progress = 0f
                                    status = ""
                                    val fileName = "${app!!.name}.apk".replace(" ", "_")
                                    val id = DownloadService.enqueue(context, app!!.apkUrl, fileName)
                                    downloadId = id
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Download, null)
                            Spacer(Modifier.width(8.dp))
                            Text(Strings.get(language, "install"))
                        }
                    }
                    if (status.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(status, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
