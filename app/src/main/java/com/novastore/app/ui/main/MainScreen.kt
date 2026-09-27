package com.novastore.app.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novastore.app.BuildConfig
import com.novastore.app.data.AppInfo
import com.novastore.app.data.Catalog
import com.novastore.app.data.UpdateChecker
import com.novastore.app.util.AppIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onAppClick: (String) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Все") }
    var updateAvailable by remember { mutableStateOf<com.novastore.app.data.AppUpdate?>(null) }

    LaunchedEffect(Unit) {
        val latest = UpdateChecker.checkLatestVersion("amirkarls/NovaStore")
        if (latest != null && latest.version != BuildConfig.VERSION_NAME) {
            updateAvailable = latest
        }
    }

    val filtered = Catalog.apps.filter { app ->
        (selectedCategory == "Все" || app.category == selectedCategory) &&
        (query.isBlank() ||
            app.name.contains(query, true) ||
            app.descriptionRu.contains(query, true) ||
            app.description.contains(query, true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NovaStore", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            updateAvailable?.let { update ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "NovaStore обновился",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Доступна версия ${update.version}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.apkUrl))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Filled.Download, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Скачать")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Поиск приложений") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Catalog.categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { app ->
                    AppCard(app, onClick = { onAppClick(app.packageName) })
                }
                if (filtered.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Ничего не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppCard(app: AppInfo, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(app.name, app.iconUrl, 56)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(app.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    app.descriptionRu,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    app.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
