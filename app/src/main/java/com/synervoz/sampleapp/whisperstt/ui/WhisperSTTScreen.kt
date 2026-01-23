package com.synervoz.sampleapp.whisperstt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synervoz.sampleapp.whisperstt.data.TranscriptionItem
import com.synervoz.sampleapp.whisperstt.data.WhisperModel
import com.synervoz.sampleapp.whisperstt.viewmodel.WhisperSTTViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhisperSTTScreen(
    viewModel: WhisperSTTViewModel = viewModel()
) {
    val transcriptions by viewModel.transcriptions.observeAsState(emptyList())
    val vadState by viewModel.vadState.observeAsState("--")
    val isRunning by viewModel.isRunning.observeAsState(false)
    val isInitialized by viewModel.isInitialized.observeAsState(false)
    val error by viewModel.error.observeAsState()

    var vadThreshold by remember { mutableFloatStateOf(0.6f) }
    var minSilenceDuration by remember { mutableIntStateOf(100) }
    var selectedModel by remember { mutableStateOf(WhisperModel.TINY) }

    LaunchedEffect(Unit) {
        if (!isInitialized) {
            viewModel.initialize()
        }
    }

    error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Whisper STT Example",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        VadStateCard(
            vadState = vadState,
            vadThreshold = vadThreshold,
            onVadThresholdChange = {
                vadThreshold = it
                viewModel.updateVadThreshold(it)
            },
            minSilenceDuration = minSilenceDuration,
            onMinSilenceDurationChange = {
                minSilenceDuration = it
                viewModel.updateMinSilenceDuration(it)
            }
        )

        ControlsSection(
            selectedModel = selectedModel,
            onModelChange = {
                selectedModel = it
                viewModel.setWhisperModel(it)
            },
            isRunning = isRunning,
            isInitialized = isInitialized,
            onStart = { viewModel.start() },
            onStop = { viewModel.stop() }
        )

        TranscriptionSection(
            transcriptions = transcriptions,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun VadStateCard(
    vadState: String,
    vadThreshold: Float,
    onVadThresholdChange: (Float) -> Unit,
    minSilenceDuration: Int,
    onMinSilenceDurationChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
//            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 12.dp),
            ) {
                Text(
                    text = "VAD State:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = vadState,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            ThresholdControl(
                label = "VAD Threshold",
                value = vadThreshold,
                onValueChange = onVadThresholdChange,
                range = 0.1f..1.0f,
                step = 0.1f
            )

            DurationControl(
                label = "Min Silence Duration",
                value = minSilenceDuration,
                onValueChange = onMinSilenceDurationChange,
                range = 50..500,
                step = 50
            )
        }
    }
}

@Composable
fun ControlsSection(
    selectedModel: WhisperModel,
    onModelChange: (WhisperModel) -> Unit,
    isRunning: Boolean,
    isInitialized: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModelSelection(
                selectedModel = selectedModel,
                onModelChange = onModelChange
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = "Whisper Backend:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "CPU",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStart,
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start")
                }

                Button(
                    onClick = onStop,
                    enabled = isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
fun ThresholdControl(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    step: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = {
                val newValue = (value - step).coerceIn(range)
                onValueChange(newValue)
            },
            enabled = value > range.start
        ) {
            Text("-", style = MaterialTheme.typography.titleLarge)
        }

        Text(
            text = String.format("%.1f", value),
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        IconButton(
            onClick = {
                val newValue = (value + step).coerceIn(range)
                onValueChange(newValue)
            },
            enabled = value < range.endInclusive
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun DurationControl(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = {
                val newValue = (value - step).coerceIn(range)
                onValueChange(newValue)
            },
            enabled = value > range.first
        ) {
            Text("-", style = MaterialTheme.typography.titleLarge)
        }

        Text(
            text = "${value}ms",
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        IconButton(
            onClick = {
                val newValue = (value + step).coerceIn(range)
                onValueChange(newValue)
            },
            enabled = value < range.last
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelection(
    selectedModel: WhisperModel,
    onModelChange: (WhisperModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Whisper Model:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Card(
                modifier = Modifier
                    .menuAnchor()
                    .width(120.dp),
                onClick = { expanded = !expanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedModel.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                WhisperModel.values().forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model.displayName) },
                        onClick = {
                            onModelChange(model)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TranscriptionSection(
    transcriptions: List<TranscriptionItem>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(transcriptions.size) {
        if (transcriptions.isNotEmpty()) {
            listState.animateScrollToItem(transcriptions.size - 1)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Transcriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = transcriptions,
                    key = { it.timestamp }
                ) { transcription ->
                    TranscriptionItem(transcription = transcription)
                }
            }
        }
    }
}

@Composable
fun TranscriptionItem(
    transcription: TranscriptionItem
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = transcription.text,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Processing time: ${transcription.processingTimeMs}ms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WhisperSTTScreenPreview() {
    MaterialTheme {
        WhisperSTTScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun VadStateCardPreview() {
    MaterialTheme {
        VadStateCard(
            vadState = "Speaking",
            vadThreshold = 0.6f,
            onVadThresholdChange = {},
            minSilenceDuration = 100,
            onMinSilenceDurationChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TranscriptionItemPreview() {
    MaterialTheme {
        TranscriptionItem(
            transcription = TranscriptionItem(
                text = "This is a sample transcription text that shows what the user said.",
                processingTimeMs = 145
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ControlsSectionPreview() {
    MaterialTheme {
        ControlsSection(
            selectedModel = WhisperModel.TINY,
            onModelChange = {},
            isRunning = false,
            isInitialized = true,
            onStart = {},
            onStop = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TranscriptionSectionPreview() {
    MaterialTheme {
        TranscriptionSection(
            transcriptions = listOf(
                TranscriptionItem("First transcription", 120),
                TranscriptionItem("Second transcription with a longer text that might wrap to multiple lines", 200),
                TranscriptionItem("Third transcription", 95)
            ),
            modifier = Modifier.height(400.dp)
        )
    }
}