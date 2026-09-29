package com.novastore.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novastore.app.BuildConfig
import com.novastore.app.data.GitHubApi
import com.novastore.app.data.Preferences
import com.novastore.app.i18n.Strings
import com.novastore.app.ui.theme.AccentColors
import com.novastore.app.ui.theme.AccentNames
import com.novastore.app.util.IconSwitcher
import kotlinx.coroutines.launch

data class IconOption(val id: String, val label: String, val color: Color)

val iconOptions = listOf(
    IconOption("white", "White", Color(0xFFFFFFFF)),
    IconOption("purple", "Purple", Color(0xFFD0BCFF)),
    IconOption("blue", "Blue", Color(0xFF8AB4F8)),
    IconOption("pink", "Pink", Color(0xFFFF6EC7)),
    IconOption("green", "Green", Color(0xFF6EFFB2)),
    IconOption("orange", "Orange", Color(0xFFFFA756))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    language: String,
    dynamicColor: Boolean,
    accentIndex: Int,
    smoothAnimations: Boolean,
    updateNotifications: Boolean,
    onLanguageChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onAccentColorChange: (Int) -> Unit,
    onSmoothAnimationsChange: (Boolean) -> Unit,
    onUpdateNotificationsChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Preferences(context) }

    var token by remember { mutableStateOf("") }
    var savedToken by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var currentIcon by remember { mutableStateOf("white") }

    LaunchedEffect(Unit) {
        savedToken = prefs.getGithubToken()
        token = savedToken
        GitHubApi.token = savedToken
        currentIcon = prefs.getAppIcon()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(Strings.get(language, "settings"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

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
                Text(Strings.get(language, "appearance"), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                SwitchRow(
                    title = Strings.get(language, "material_you"),
                    subtitle = Strings.get(language, "colors_from_wallpaper"),
                    checked = dynamicColor,
                    onChange = onDynamicColorChange
                )
                if (!dynamicColor) {
                    Spacer(Modifier.height(16.dp))
                    Text(Strings.get(language, "accent_color"), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    AccentColors.forEachIndexed { index, color ->
                        key(index) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = MaterialTheme.shapes.small, color = color, modifier = Modifier.size(36.dp)) {}
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    AccentNames.getOrElse(index) { "Color" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(selected = accentIndex == index, onClick = { onAccentColorChange(index) })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                SwitchRow(
                    title = Strings.get(language, "animations"),
                    subtitle = Strings.get(language, "animations_desc"),
                    checked = smoothAnimations,
                    onChange = onSmoothAnimationsChange
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get(language, "app_icon"), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose your app icon on home screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconOptions.forEach { option ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A1A1A))
                                    .border(
                                        width = if (currentIcon == option.id) 3.dp else 1.dp,
                                        color = if (currentIcon == option.id) option.color else Color(0xFF333333),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        currentIcon = option.id
                                        scope.launch {
                                            prefs.setAppIcon(option.id)
                                            IconSwitcher.switchIcon(context, option.id)
                                        }
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⚡",
                                        color = option.color,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                option.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentIcon == option.id) option.color else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get(language, "update_notifications"), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                SwitchRow(
                    title = Strings.get(language, "update_notifications"),
                    subtitle = Strings.get(language, "update_notifications_desc"),
                    checked = updateNotifications,
                    onChange = onUpdateNotificationsChange
                )
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
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch {
                            prefs.setGithubToken(token)
                            GitHubApi.token = token
                            savedToken = token
                            status = if (token.isBlank()) Strings.get(language, "token_removed")
                            else Strings.get(language, "token_saved")
                        }
                    }) { Text(Strings.get(language, "save")) }
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
                    ) { Text(Strings.get(language, "check")) }
                    if (token.isNotBlank()) {
                        TextButton(onClick = {
                            scope.launch {
                                token = ""
                                prefs.setGithubToken("")
                                GitHubApi.token = ""
                                savedToken = ""
                                status = Strings.get(language, "token_removed")
                            }
                        }) { Text(Strings.get(language, "clear")) }
                    }
                }
                if (status.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (status == Strings.get(language, "token_works")) Icons.Filled.CheckCircle
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
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(Strings.get(language, "about"), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("NovaStore ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
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
fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
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
