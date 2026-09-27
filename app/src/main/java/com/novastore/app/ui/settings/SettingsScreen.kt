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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novastore.app.data.GitHubApi
import com.novastore.app.data.Preferences
import com.novastore.app.i18n.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    language: String,
    onLanguageChange: (String) -> Unit
) {
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
            Strings.get(language, "settings"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Язык / Language", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LangRow("Русский", "ru", language, onLanguageChange)
                LangRow("English", "en", language, onLanguageChange)
                LangRow("Українська", "uk", language, onLanguageChange)
                LangRow("Қазақша", "kk", language, onLanguageChange)
                LangRow("Español", "es", language, onLanguageChange)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get(language, "github_token"), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    Strings.get(language, "github_token_desc"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text(Strings.get(language, "github_token_hint")) },
                    placeholder = { Text("ghp_...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "github.com/settings/tokens (scope: public_repo)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch {
                            prefs.setGithubToken(token)
                            GitHubApi.token = token
                            savedToken = token
                            status = if (token.isBlank())
                                Strings.get(language, "token_removed")
                            else Strings.get(language, "token_saved")
                        }
                    }) {
                        Text(Strings.get(language, "save"))
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                checking = true
                                status = Strings.get(language, "checking")
                                GitHubApi.token = token
                                val ok = GitHubApi.checkToken()
                                status = if (ok) Strings.get(language, "token_works")
                                else Strings.get(language, "token_not_works")
                                checking = false
                            }
                        },
                        enabled = !checking && token.isNotBlank()
                    ) {
                        Text(Strings.get(language, "check"))
                    }
                    if (token.isNotBlank()) {
                        TextButton(onClick = {
                            scope.launch {
                                token = ""
                                prefs.setGithubToken("")
                                GitHubApi.token = ""
                                savedToken = ""
                                status = Strings.get(language, "token_removed")
                            }
                        }) {
                            Text(Strings.get(language, "clear"))
                        }
                    }
                }

                if (status.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (status == Strings.get(language, "token_works"))
                                Icons.Filled.CheckCircle
                            else Icons.Filled.Error,
                            null,
                            tint = if (status == Strings.get(language, "token_works"))
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
                        "${savedToken.take(7)}...${savedToken.takeLast(4)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get(language, "about"), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("NovaStore v1.9", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    Strings.get(language, "about_text"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LangRow(title: String, code: String, current: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = current == code, onClick = { onChange(code) })
    }
}
