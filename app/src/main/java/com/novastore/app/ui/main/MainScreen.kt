package com.novastore.app.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
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
import com.novastore.app.data.CatalogExtra
import com.novastore.app.data.CatalogExtra2
import com.novastore.app.data.UpdateChecker
import com.novastore.app.data.categoryLabel
import com.novastore.app.i18n.Strings
import com.novastore.app.util.AppIcon
import com.novastore.app.util.AppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    language: String,
    updateNotifications: Boolean,
    onAppClick: (String) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var updateAvailable by remember { mutableStateOf<com.novastore.app.data.AppUpdate?>(null) }
    var dismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (updateNotifications) {
            val latest = UpdateChecker.checkLatestVersion("amirkarls/NovaStore")
            if (latest != null && latest.version != BuildConfig.VERSION_NAME) {
                updateAvailable = latest
            }
        }
    }

    val allApps = remember { Catalog.apps + CatalogExtra.apps + CatalogExtra2.apps }
    val categories = Catalog.categories

    val filtered = allApps.filter { app ->
        val matchesCategory = selectedCategory == "all" || app.category == selectedCategory
        if (!matchesCategory) return@filter false

        if (query.isBlank()) return@filter true

        val q = query.lowercase()
        val name = app.name.lowercase()
        val desc = app.description(language).lowercase()
        val cat = categoryLabel(language, app.category).lowercase()
        val author = app.author.lowercase()
        val pkg = app.packageName.lowercase()

        name.contains(q) || desc.contains(q) || cat.contains(q) || author.contains(q) || pkg.contains(q)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.get(language, "app_name"), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (updateNotifications && updateAvailable != null && !dismissed) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${Strings.get(language, "update_available")} ${updateAvailable!!.version}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(Strings.get(language, "update_text"), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateAvailable!!.apkUrl))
                                context.startActivity(intent)
                            }) { Text(Strings.get(language, "update_button")) }
                            TextButton(onClick = { dismissed = true }) { Text(Strings.get(language, "later")) }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(Strings.get(language, "search_hint")) },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(categoryLabel(language, cat)) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { app ->
                    AppCard(app, language, onClick = { onAppClick(app.packageName) })
                }
                if (filtered.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(Strings.get(language, "nothing_found"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppCard(app: AppInfo, language: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val installed = remember(app.packageName) { AppUtils.isInstalled(context, app.packageName) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(app.name, app.iconUrl, 56)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        app.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (installed) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    app.description(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (app.author.isNotBlank()) {
                    Text(
                        "${Strings.get(language, "author")}: ${app.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    categoryLabel(language, app.category),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
