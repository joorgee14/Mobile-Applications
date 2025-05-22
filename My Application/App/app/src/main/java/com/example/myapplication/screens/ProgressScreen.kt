package com.example.myapplication.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapplication.Data.Models.UserRepository
import com.example.myapplication.Data.Models.WorkoutSession
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.core.entry.entryModelOf
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R

// AI PROMPT: HELP ME TO CREATE GRAPHS OF THE EXERCISES A USER HAS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavHostController,
    userId: String,
    onBackToHome: () -> Unit
) {
    val userRepository = UserRepository()
    var workoutSessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedExercise by remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        workoutSessions = userRepository.getWorkoutSessionsByUser(userId)
        isLoading = false
    }

    val exerciseProgress = remember(workoutSessions) {
        getExerciseProgressOverTime(workoutSessions)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.progress_tracker_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackToHome() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.select_exercise),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDropdownExpanded = !isDropdownExpanded },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            ),
                            shape = MaterialTheme.shapes.large,
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        )
                        {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = selectedExercise ?: stringResource(id = R.string.choose_exercise),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            exerciseProgress.keys.forEach { exerciseName ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = exerciseName)
                                    },
                                    onClick = {
                                        selectedExercise = exerciseName
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                selectedExercise?.let { exerciseName ->
                    val entries = exerciseProgress[exerciseName] ?: emptyList()
                    item {
                        LineChartSection(entries)
                    }
                }
            }
        }
    }
}

@Composable
fun LineChartSection(entries: List<Pair<Long, Float>>) {
    val sortedEntries = entries.sortedBy { it.first }
    val maxPoints = 10
    val limitedEntries = if (sortedEntries.size > maxPoints) {
        sortedEntries.takeLast(maxPoints)
    } else {
        sortedEntries
    }

    if (limitedEntries.isEmpty()) {
        Text(stringResource(id = R.string.no_data_available))
        return
    }

    val xLabels = limitedEntries.map { it.first } // timestamps
    val chartEntries = limitedEntries.mapIndexed { index, entry ->
        index.toFloat() to entry.second // use index as x-value
    }

    val entryModel = entryModelOf(*chartEntries.toTypedArray())
    val maxWeight = limitedEntries.maxOfOrNull { it.second } ?: 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.progress_over_time),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Chart(
            chart = lineChart(),
            model = entryModel,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(
                valueFormatter = { value, _ ->
                    val index = value.toInt()
                    if (index in xLabels.indices) convertTimestampToDate(xLabels[index]) else ""
                },
                labelRotationDegrees = 90f,
                guideline = null
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = "Max weight: $maxWeight kg",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun getExerciseProgressOverTime(sessions: List<WorkoutSession>): Map<String, List<Pair<Long, Float>>> {
    val progressMap = mutableMapOf<String, MutableList<Pair<Long, Float>>>()
    for (session in sessions.sortedBy { it.date }) {
        for (exercise in session.exercises) {
            val list = progressMap.getOrPut(exercise.exercise) { mutableListOf() }
            list.add(session.date to exercise.weight)
        }
    }
    return progressMap
}
