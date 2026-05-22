package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.Language
import com.example.util.LocalizationManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel

@Composable
fun SettingsSectionView(viewModel: QuranAudioViewModel) {
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanTotal by viewModel.scanTotalEst.collectAsState()

    val langState by LocalizationManager.currentLanguage.collectAsState()
    val context = LocalContext.current

    // Local configuration states
    var scanDirectory by remember { mutableStateOf("/storage/emulated/0/Music") }
    var skipDirectories by remember { mutableStateOf("Android, WhatsApp, System, Cache") }
    var batterySaver by remember { mutableStateOf(false) }
    var darkThemeActive by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Text(
            text = LocalizationManager.get("settings"),
            color = PureWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = LocalizationManager.get("quick_desc"),
            color = LightGrayText,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Interface Language Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightGlassBg),
            border = BorderStroke(1.dp, BorderWhiteAlpha)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LocalizationManager.get("language"),
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = LocalizationManager.get("language_desc"),
                    color = LightGrayText,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Language.values().forEach { l ->
                        val isSelected = langState == l
                        Button(
                            onClick = {
                                LocalizationManager.language = l
                                Toast.makeText(context, if (l == Language.AR) "تم تغيير لغة التطبيق إلى العربية" else "Language updated to English", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) QuranPrimary else Color(0x14FFFFFF)
                            ),
                            border = if (!isSelected) BorderStroke(1.dp, Color(0x33FFFFFF)) else null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = l.nativeName,
                                color = if (isSelected) Color.Black else PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Scanner Engine Management
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightGlassBg),
            border = BorderStroke(1.dp, BorderWhiteAlpha)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LocalizationManager.get("scan_device"),
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = scanDirectory,
                    onValueChange = { scanDirectory = it },
                    label = { Text(LocalizationManager.get("scan_target")) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QuranPrimary,
                        unfocusedBorderColor = LightGrayText,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = skipDirectories,
                    onValueChange = { skipDirectories = it },
                    label = { Text(LocalizationManager.get("ignore_warn")) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QuranPrimary,
                        unfocusedBorderColor = LightGrayText,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isScanning) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Active Scanner: Scanned $scanProgress folders...", color = QuranPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { viewModel.stopStorageScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = ContrastAccent),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(LocalizationManager.get("cancel"), color = PureWhite, fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(color = QuranPrimary, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startStorageScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(LocalizationManager.get("trigger_scan"), color = Color.Black)
                        }

                        Button(
                            onClick = { viewModel.generateSampleQuranLibrary() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF)),
                            border = BorderStroke(1.dp, QuranPrimary),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = QuranPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(LocalizationManager.get("populate"), color = PureWhite)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // System Parameters Switches
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightGlassBg),
            border = BorderStroke(1.dp, BorderWhiteAlpha)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Battery Saver Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LocalizationManager.get("battery_eco"),
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = LocalizationManager.get("battery_eco_desc"),
                            color = LightGrayText,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = batterySaver,
                        onCheckedChange = { batterySaver = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = QuranPrimary, checkedTrackColor = QuranPrimary.copy(alpha = 0.5f))
                    )
                }

                Divider(color = Color(0x14FFFFFF), modifier = Modifier.padding(vertical = 12.dp))

                // Dark theme Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LocalizationManager.get("astro_theme"),
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = LocalizationManager.get("astro_theme_desc"),
                            color = LightGrayText,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = darkThemeActive,
                        onCheckedChange = {
                            darkThemeActive = it
                            Toast.makeText(context, "Astrovelet Dark Scheme fixed active.", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = QuranPrimary, checkedTrackColor = QuranPrimary.copy(alpha = 0.5f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Backup, Import & Export Data Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightGlassBg),
            border = BorderStroke(1.dp, BorderWhiteAlpha)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LocalizationManager.get("backup_sync"),
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, LocalizationManager.get("export_success"), Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF)),
                        border = BorderStroke(1.dp, QuranPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = QuranPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LocalizationManager.get("export_xml"), color = PureWhite, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, LocalizationManager.get("import_success"), Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF)),
                        border = BorderStroke(1.dp, QuranPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = QuranPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LocalizationManager.get("import_sync"), color = PureWhite, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.clearAllFiles()
                        Toast.makeText(context, LocalizationManager.get("reset_db_desc"), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ContrastAccent.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, ContrastAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ContrastAccent)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LocalizationManager.get("reset_db"), color = ContrastAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ABOUT & OFF-LINE AI LOGS SECTION
        Text(
            text = LocalizationManager.get("about"),
            color = PureWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MediumGlassBg),
            border = BorderStroke(1.dp, BorderWhiteAlpha)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = QuranPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Quran Audio Manager AI", color = PureWhite, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = LocalizationManager.get("about_app_desc"),
                    color = LightGrayText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = LocalizationManager.get("about_app_version"),
                    color = QuranPrimary.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
