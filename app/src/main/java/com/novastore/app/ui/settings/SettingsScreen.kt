package com.novastore.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.novastore.app.data.GitHubApi
import com.novastore.app.data.Preferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Preferences(context) }

    var token by remember { mutableStateOf("") }
    var savedToken by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        savedToken = prefs.getGithubToken()
        token = savedToken
        GitHubApi.token = savedToken
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Настройки",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("GitHub", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Токен нужен для доступа к API GitHub. Без него лимит 60 запросов в час. С токеном — 5000.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Personal Access Token") },
                    placeholder = { Text("ghp_...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "Создать токен: github.com/settings/tokens (scope: public_repo)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                prefs.setGithubToken(token)
                                GitHubApi.token = token
                                savedToken = token
                                status = if (token.isBlank()) "Токен удалён" else "Токен сохранён"
                            }
                        }
                    ) {
                        Text("Сохранить")
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                checking = true
                                status = "Проверка..."
                                GitHubApi.token = token
                                val ok = GitHubApi.checkToken()
                                status = if (ok) "Токен работает" else "Токен не работает"
                                checking = false
                            }
                        },
                        enabled = !checking && token.isNotBlank()
                    ) {
                        Text("Проверить")
                    }
                    if (token.isNotBlank()) {
                        TextButton(onClick = {
                            scope.launch {
                                token = ""
                                prefs.setGithubToken("")
                                GitHubApi.token = ""
                                savedToken = ""
                                status = "Токен удалён"
                            }
                        }) {
                            Text("Очистить")
                        }
                    }
                }

                if (status.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (status.contains("работает")) Icons.Filled.CheckCircle else Icons.Filled.Error,
                            null,
                            tint = if (status.contains("работает"))
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(status, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (savedToken.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Сохранён токен: ${savedToken.take(7)}...${savedToken.takeLast(4)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("О приложении", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("NovaStore v1.1", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Магазин open-source приложений с прямыми ссылками из F-Droid и GitHub.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
