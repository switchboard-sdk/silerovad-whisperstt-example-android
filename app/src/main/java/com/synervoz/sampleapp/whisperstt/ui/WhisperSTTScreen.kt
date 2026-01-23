package com.synervoz.sampleapp.whisperstt.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synervoz.sampleapp.whisperstt.data.TranscriptionItem
import com.synervoz.sampleapp.whisperstt.data.WhisperModel
import com.synervoz.sampleapp.whisperstt.viewmodel.WhisperSTTViewModel

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
            .padding(Dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.verticalSpacing)
    ) {
        Text(
            text = "Whisper STT Example",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
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

        WhisperSection(
            selectedModel = selectedModel,
            onModelChange = {
                selectedModel = it
                viewModel.setWhisperModel(it)
            }
        )

        TranscriptionSection(
            transcriptions = transcriptions,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = {
                if (isRunning) {
                    viewModel.stop()
                } else {
                    viewModel.start()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        ) {
            Text(if (isRunning) "Stop" else "Start")
        }
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
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small

    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimensions.cardPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(Dimensions.controlRowHeight)
            ) {
                Text(
                    text = "VAD State",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = vadState,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = Dimensions.endPadding)
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
fun WhisperSection(
    selectedModel: WhisperModel,
    onModelChange: (WhisperModel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimensions.cardPadding),
        ) {
            ModelSelection(
                selectedModel = selectedModel,
                onModelChange = onModelChange
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(Dimensions.controlRowHeight)
                    .padding(vertical = Dimensions.smallPadding),
            ) {
                Text(
                    text = "Whisper Backend",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "CPU",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = Dimensions.endPadding)
                )
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
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Text(
            text = label,
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
            modifier = Modifier.width(Dimensions.valueDisplayWidth),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
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
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Text(
            text = label,
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
            text = "${value} ms",
            modifier = Modifier.width(Dimensions.durationDisplayWidth),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
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

@Composable
fun ModelSelection(
    selectedModel: WhisperModel,
    onModelChange: (WhisperModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Text(
            text = "Whisper Model",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Box {
            Row(
                modifier = Modifier
                    .clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedModel.displayName,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(Dimensions.smallPadding))

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSize)
                )
            }

            DropdownMenu(
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
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.cardPadding)
        ) {

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(Dimensions.horizontalSpacing),
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
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.smallPadding),
        ) {
            Text(
                text = transcription.text,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Transcription time: ${transcription.processingTimeMs}ms",
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
fun WhisperSectionPreview() {
    MaterialTheme {
        WhisperSection(
            selectedModel = WhisperModel.TINY,
            onModelChange = {}
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
            modifier = Modifier.height(400.dp) // Preview height
        )
    }
}