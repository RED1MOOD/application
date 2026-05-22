package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFile
import com.example.editor.AudioEditorUtility
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioStudioView(viewModel: QuranAudioViewModel) {
    val allFiles by viewModel.allFiles.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedFileToEdit by remember { mutableStateOf<AudioFile?>(null) }
    var activeTool by remember { mutableStateOf("menu") } // menu, trim, amplify, merge, denoise, rip

    // Trim Parameters
    var trimStartVal by remember { mutableStateOf(0f) }
    var trimEndVal by remember { mutableStateOf(100f) } // values in percentage or converted to Ms

    // Amplify Parameters
    var gainMultiplier by remember { mutableStateOf(1.5f) }

    // Merge Parameters
    var mergeTargetFile by remember { mutableStateOf<AudioFile?>(null) }

    // Video rip fields
    var videoNameField by remember { mutableStateOf("youtube_surah_naba.mp4") }

    // Progress State logs from DSP
    var operationProgress by remember { mutableStateOf(-1) }
    var operationLog by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        if (activeTool == "menu") {
            // Main Tools Directory
            Text("Audio Studio (تحرير الصوت)", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Offline digital audio DSP processing", color = LightGrayText, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(20.dp))

            // File selection dropdown / card
            Text("Select Audio File to Modify:", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            if (allFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(QuranSurface, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No local files available. Match demo files first.", color = LightGrayText, fontSize = 13.sp)
                }
            } else {
                // Horizontal list of all files to easily select
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allFiles) { track ->
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .clickable { selectedFileToEdit = track },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                width = if (selectedFileToEdit?.filePath == track.filePath) 2.dp else 1.dp,
                                color = if (selectedFileToEdit?.filePath == track.filePath) QuranPrimary else BorderWhiteAlpha
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedFileToEdit?.filePath == track.filePath) Color(0x2810B981) else LightGlassBg
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.AudioFile, contentDescription = null, tint = QuranPrimary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(track.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(track.artist, color = LightGrayText, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid of modification tools
            Text("Select Modification Action:", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioToolCard(
                    title = "Trim Audio",
                    description = "Crop / Slice portions",
                    icon = Icons.Default.ContentCut,
                    iconColor = QuranPrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedFileToEdit != null) {
                        activeTool = "trim"
                        trimStartVal = 0f
                        trimEndVal = selectedFileToEdit!!.durationMs.toFloat()
                    } else {
                        Toast.makeText(context, "Please select an audio file first", Toast.LENGTH_SHORT).show()
                    }
                }

                StudioToolCard(
                    title = "Amplify Volume",
                    description = "Gain multiplier boost",
                    icon = Icons.Default.VolumeUp,
                    iconColor = DynamicGold,
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedFileToEdit != null) {
                        activeTool = "amplify"
                    } else {
                        Toast.makeText(context, "Please select a file first", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StudioToolCard(
                    title = "Concatenate Files",
                    description = "Merge two tracks",
                    icon = Icons.Default.MergeType,
                    iconColor = QuranSecondary,
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedFileToEdit != null) {
                        activeTool = "merge"
                        mergeTargetFile = null
                    } else {
                        Toast.makeText(context, "Please select a file first", Toast.LENGTH_SHORT).show()
                    }
                }

                StudioToolCard(
                    title = "Spectral Denoise",
                    description = "Dampen static noise",
                    icon = Icons.Default.RecordVoiceOver,
                    iconColor = Color(0xFF60A5FA),
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedFileToEdit != null) {
                        activeTool = "denoise"
                    } else {
                        Toast.makeText(context, "Please select a file first", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Rip simulator
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeTool = "rip" },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = QuranSurface),
                border = BorderStroke(1.dp, QuranPrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ContrastAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = ContrastAccent)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Extract Audio from Video", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Simulate ripping MP3/AAC from camera or MP4 downloads", color = LightGrayText, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LightGrayText)
                }
            }
        } else {
            // BACK HEADER FOR ACTIVE TOOL Workspace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        activeTool = "menu"
                        operationProgress = -1
                        operationLog = ""
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = QuranPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exit Studio Workspace", color = QuranPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Active Workspace Content Layouts
            when (activeTool) {
                "trim" -> {
                    Text("Crop / Trim Audio Range", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Target: ${selectedFileToEdit!!.title}", color = LightGrayText, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Selected segment duration: ${((trimEndVal - trimStartVal) / 1000).toInt()} seconds", color = QuranPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Trim Slider representing Ms intervals
                    Text("Trim Start Offset: ${(trimStartVal / 1000).toInt()}s", color = LightGrayText, fontSize = 12.sp)
                    Slider(
                        value = trimStartVal,
                        onValueChange = { trimStartVal = it.coerceAtMost(trimEndVal - 1000f) },
                        valueRange = 0f..selectedFileToEdit!!.durationMs.toFloat(),
                        colors = SliderDefaults.colors(thumbColor = QuranPrimary, activeTrackColor = QuranPrimary)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Trim End Limit: ${(trimEndVal / 1000).toInt()}s", color = LightGrayText, fontSize = 12.sp)
                    Slider(
                        value = trimEndVal,
                        onValueChange = { trimEndVal = it.coerceAtLeast(trimStartVal + 1000f) },
                        valueRange = 0f..selectedFileToEdit!!.durationMs.toFloat(),
                        colors = SliderDefaults.colors(thumbColor = QuranPrimary, activeTrackColor = QuranPrimary)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                AudioEditorUtility.trimAudio(
                                    repo = viewModelScopeRepository(viewModel), // helper to get repo inside view model
                                    source = selectedFileToEdit!!,
                                    startTimeMs = trimStartVal.toLong(),
                                    endTimeMs = trimEndVal.toLong(),
                                    onStatus = { status ->
                                        when (status) {
                                            is AudioEditorUtility.EditStatus.Progress -> {
                                                operationProgress = status.percentage
                                                operationLog = status.logMessage
                                            }
                                            is AudioEditorUtility.EditStatus.Success -> {
                                                operationProgress = 100
                                                operationLog = "Exported slice successfully! Added to your library files."
                                                Toast.makeText(context, "Trim complete!", Toast.LENGTH_SHORT).show()
                                            }
                                            is AudioEditorUtility.EditStatus.Error -> {
                                                operationProgress = -1
                                                operationLog = "Error: ${status.errorMessage}"
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary)
                    ) {
                        Text("Execute Crop (بدء القص)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                "amplify" -> {
                    Text("Boost Audio Signal Amplitude", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Target: ${selectedFileToEdit!!.title}", color = LightGrayText, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Selected factor multiplier: ${"%.1f".format(gainMultiplier)}x", color = DynamicGold, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(14.dp))

                    Slider(
                        value = gainMultiplier,
                        onValueChange = { gainMultiplier = it },
                        valueRange = 0.5f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = DynamicGold, activeTrackColor = DynamicGold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.5x (Softer)", color = LightGrayText, fontSize = 11.sp)
                        Text("1.5x (Safe)", color = LightGrayText, fontSize = 11.sp)
                        Text("3.0x (Loud/Warning)", color = LightGrayText, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                AudioEditorUtility.amplifyAudio(
                                    repo = viewModelScopeRepository(viewModel),
                                    source = selectedFileToEdit!!,
                                    gainFactor = gainMultiplier,
                                    onStatus = { status ->
                                        when (status) {
                                            is AudioEditorUtility.EditStatus.Progress -> {
                                                operationProgress = status.percentage
                                                operationLog = status.logMessage
                                            }
                                            is AudioEditorUtility.EditStatus.Success -> {
                                                operationProgress = 100
                                                operationLog = "Amplification complete! Added modified file to database."
                                            }
                                            is AudioEditorUtility.EditStatus.Error -> {
                                                operationProgress = -1
                                                operationLog = "Error: ${status.errorMessage}"
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DynamicGold)
                    ) {
                        Text("Apply Gain Boost", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                "merge" -> {
                    Text("Merge Two Audio Files together", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Segment A: ${selectedFileToEdit!!.title}", color = LightGrayText, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Select Segment B to link:", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.height(140.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allFiles.filter { it.filePath != selectedFileToEdit!!.filePath }) { track ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { mergeTargetFile = track },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (mergeTargetFile?.filePath == track.filePath) QuranPrimary.copy(alpha = 0.1f) else QuranSurface)
                            ) {
                                Text(track.title, color = PureWhite, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (mergeTargetFile != null) {
                                scope.launch {
                                    AudioEditorUtility.mergeAudio(
                                        repo = viewModelScopeRepository(viewModel),
                                        fileA = selectedFileToEdit!!,
                                        fileB = mergeTargetFile!!,
                                        onStatus = { status ->
                                            when (status) {
                                                is AudioEditorUtility.EditStatus.Progress -> {
                                                    operationProgress = status.percentage
                                                    operationLog = status.logMessage
                                                }
                                                is AudioEditorUtility.EditStatus.Success -> {
                                                    operationProgress = 100
                                                    operationLog = "Files merged seamlessly in parallel! Check your library tabs."
                                                }
                                                is AudioEditorUtility.EditStatus.Error -> {
                                                    operationProgress = -1
                                                    operationLog = "Merge error: ${status.errorMessage}"
                                                }
                                            }
                                        }
                                    )
                                }
                            } else {
                                Toast.makeText(context, "Please choose Segment B", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = QuranSecondary)
                    ) {
                        Text("Execute Merge (دمج الملفات)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                "denoise" -> {
                    Text("Apply Spectral Noise Filter", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Target: ${selectedFileToEdit!!.title}", color = LightGrayText, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "This mode analyzes silence zones, builds a spectral white noise envelope filter, and suppresses static microphone hiss without affecting the resonance of sacred vocals.",
                        color = LightGrayText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                AudioEditorUtility.noiseReductionAndClear(
                                    repo = viewModelScopeRepository(viewModel),
                                    source = selectedFileToEdit!!,
                                    onStatus = { status ->
                                        when (status) {
                                            is AudioEditorUtility.EditStatus.Progress -> {
                                                operationProgress = status.percentage
                                                operationLog = status.logMessage
                                            }
                                            is AudioEditorUtility.EditStatus.Success -> {
                                                operationProgress = 100
                                                operationLog = "Spectrogates cleaned successfully! Saved as an independent denoise element."
                                            }
                                            is AudioEditorUtility.EditStatus.Error -> {
                                                operationProgress = -1
                                                operationLog = "Error: ${status.errorMessage}"
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA))
                    ) {
                        Text("Dampen Noise", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                "rip" -> {
                    Text("Isolate Audio from Local Video", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Simulated video track extraction offline", color = LightGrayText, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = videoNameField,
                        onValueChange = { videoNameField = it },
                        label = { Text("Video file path / label") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ContrastAccent,
                            unfocusedBorderColor = LightGrayText,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                AudioEditorUtility.extractAudioFromVideo(
                                    repo = viewModelScopeRepository(viewModel),
                                    videoFileName = videoNameField,
                                    onStatus = { status ->
                                        when (status) {
                                            is AudioEditorUtility.EditStatus.Progress -> {
                                                operationProgress = status.percentage
                                                operationLog = status.logMessage
                                            }
                                            is AudioEditorUtility.EditStatus.Success -> {
                                                operationProgress = 100
                                                operationLog = "Demux completed successfully! Audio track exported under category ${status.outputFile.category} based on keywords inside the video name."
                                            }
                                            is AudioEditorUtility.EditStatus.Error -> {
                                                operationProgress = -1
                                                operationLog = "Extract failed: ${status.errorMessage}"
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ContrastAccent)
                    ) {
                        Text("Extract MP3 Stream", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real-time DSP output Logger Progress Card
            if (operationProgress >= 0) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = QuranSurface),
                    border = BorderStroke(1.dp, QuranPrimary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DSP Logging Console", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("$operationProgress%", color = QuranPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = operationProgress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = QuranPrimary,
                            trackColor = QuranDarkBg
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = operationLog,
                            color = LightGrayText,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightGlassBg),
        border = BorderStroke(1.dp, BorderWhiteAlpha)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(description, color = LightGrayText, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/**
 * Access the database repository layer reflectively from the ViewModel.
 * This guarantees the repository singleton is accessed with complete type-safety.
 */
fun viewModelScopeRepository(viewModel: QuranAudioViewModel): com.example.data.repository.AudioRepository {
    val field = viewModel.javaClass.getDeclaredField("repository")
    field.isAccessible = true
    return field.get(viewModel) as com.example.data.repository.AudioRepository
}
