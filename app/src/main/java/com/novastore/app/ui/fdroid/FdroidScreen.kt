package com.novastore.app.ui.fdroid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.novastore.app.data.FdroidApi
import com.novastore.app.data.FdroidApp
import com.novastore.app.data.FdroidResult
import com.novastore.app.i18n.Strings
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FdroidScreen(
    language: String,
    onAppClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var allApps by remember { mutableStateOf<List<FdroidApp>>(emptyList()) }
    var visibleApps by remember { mutableStateOf<List<FdroidApp>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    suspend fun reload(force: Boolean) {
        loading = true
        errorMessage = null
        when (val result = FdroidApi.loadCatalog(context, force)) {
            is FdroidResult.Success -> {
                allApps = result.apps
                currentPage = 0
                visibleApps = result.apps.take(PAGE_SIZE)
                errorMessage = null
            }
            is FdroidResult.Error -> {
                allApps = emptyList()
                visibleApps = emptyList()
                errorMessage = result.message
            }
        }
        loading = false
    }

    LaunchedEffect(Unit) {
        reload(false)
    }

    // Поиск — фильтруем и сбрасываем на первую страницу
    LaunchedEffect(query) {
        val source = if (query.isBlank()) allApps else allApps.filter { app ->
            val q = query.lowercase()
            app.name.lowercase().contains(q) ||
            app.packageName.lowercase().contains(q) ||
            app.summary.lowercase().contains(q) ||
            app.author.lowercase().contains(q)
        }
        currentPage = 0
        visibleApps = source.take(PAGE_SIZE)
    }

    // Пагинация — когда пользователь доходит до конца, добавляем ещё 100
    LaunchedEffect(listState, visibleApps.size, query) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastIndex ->
                if (lastIndex >= visibleApps.size - 5) {
                    val source = if (query.isBlank()) allApps else allApps.filter { app ->
                        val q = query.lowercase()
                        app.name.lowercase().contains(q) ||
                        app.packageName.lowercase().contains(q) ||
                        app.summary.lowercase().contains(q) ||
                        app.author.lowercase().contains(q)
                    }
                    val nextPage = currentPage + 1
                    val newEnd = minOf((nextPage + 1) * PAGE_SIZE, source.size)
                    if (newEnd > visibleApps.size) {
                        visibleApps = source.take(newEnd)
                        currentPage = nextPage
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("F-Droid", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        scope.launch { reload(true) }
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = Strings.get(language, "refresh"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(Strings.get(language, "search_hint")) },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true
            )

            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Loading F-Droid catalog...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "First launch may take 10-30 seconds",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                errorMessage != null -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.CloudOff,
                                        null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Отсутствует соединение с сервером F-Droid",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Пожалуйста, подождите. Возможно, сервер F-Droid временно недоступен, или у вас нет интернета.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { scope.launch { reload(true) } },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Text(Strings.get(language, "refresh"))
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(visibleApps) { app ->
                            FdroidCard(app, onClick = { onAppClick(app.packageName) })
                        }
                        if (visibleApps.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        Strings.get(language, "nothing_found"),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (visibleApps.size < (if (query.isBlank()) allApps.size else allApps.size)) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FdroidCard(app: FdroidApp, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = app.iconUrl,
                contentDescription = app.name,
                modifier = Modifier.size(56.dp),
                loading = { Box(Modifier.size(56.dp)) },
                error = {
                    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                        Text(
                            app.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                success = { SubcomposeAsyncImageContent() }
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(app.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    app.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                if (app.author.isNotBlank()) {
                    Text(
                        app.author,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "—",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        app.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
