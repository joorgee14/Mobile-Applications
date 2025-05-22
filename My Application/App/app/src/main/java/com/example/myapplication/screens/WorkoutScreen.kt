package com.example.myapplication.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Data.Models.UserRepository
import com.example.myapplication.Data.Models.WorkoutExercise
import com.example.myapplication.Data.Models.WorkoutSession
import com.example.myapplication.receivers.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R

// AI PROMPT: HELP ME TO MAKE THIS SCREEN LOOKS BETTER VISUALLY

// AI PROMPT: HELP ME TO DELETE AN EXERCISE BEFORE SAVING THE SESSION


@Composable
fun WorkoutScreen(userId: String, userRepository: UserRepository = UserRepository()) {
    var exercise by rememberSaveable { mutableStateOf("") }
    var sets by rememberSaveable { mutableStateOf("") }
    var reps by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var exercisesList by remember { mutableStateOf<List<WorkoutExercise>>(listOf()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF8E24AA), Color(0xFF2196F3))
    )
    val cardColor = Color(0xFFFDFDFD)
    val lightBackground = Color(0xFFF3F3F7)
    val accentGreen = Color(0xFF00E676)

    // Get the context inside a composable function
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = lightBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(lightBackground)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.workout_title),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A148C),
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        WorkoutInputField(stringResource(id = R.string.exercise_label), exercise) { exercise = it }
                        Spacer(modifier = Modifier.height(12.dp))
                        WorkoutInputField(stringResource(id = R.string.sets_label), sets, KeyboardType.Number) { sets = it }
                        Spacer(modifier = Modifier.height(12.dp))
                        WorkoutInputField(stringResource(id = R.string.reps_label), reps, KeyboardType.Number) { reps = it }
                        Spacer(modifier = Modifier.height(12.dp))
                        WorkoutInputField(stringResource(id = R.string.weight_label), weight, KeyboardType.Number) { weight = it }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val validInput = exercise.isNotBlank() &&
                                        sets.toIntOrNull() != null &&
                                        reps.toIntOrNull() != null &&
                                        weight.toFloatOrNull() != null

                                if (validInput) {
                                    val newExercise = WorkoutExercise(
                                        exercise = exercise.uppercase(),
                                        sets = sets.toInt(),
                                        reps = reps.toInt(),
                                        weight = weight.toFloat()
                                    )
                                    exercisesList = exercisesList + newExercise
                                    exercise = ""
                                    sets = ""
                                    reps = ""
                                    weight = ""
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please fill in all fields correctly")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.add_exercise), color = Color.White)
                        }

                        // Set Alarm Button
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // Setting an alarm 30 seconds from now
                                val triggerTime = System.currentTimeMillis() + 30_000

                                AlarmManagerUtil.setOneTimeAlarm(context = context, triggerTime = triggerTime)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.set_alarm), color = Color.White)
                        }

                        Button(
                            onClick = {
                                AlarmManagerUtil.cancelAlarm(context)
                                AlarmSoundPlayer.stopAlarm() // <- Stops the sound if already playing
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.cancel_alarm), color = Color.White)
                        }

                    }
                }
            }

            if (exercisesList.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.your_exercises),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6A1B9A),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                items(exercisesList.indices.toList()) { index ->
                    val item = exercisesList[index]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🏋️ ${item.exercise}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Sets: ${item.sets}, Reps: ${item.reps}, Weight: ${item.weight} kg")
                            }
                            IconButton(onClick = {
                                exercisesList = exercisesList.toMutableList().also { it.removeAt(index) }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.delete_exercise),
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val session = WorkoutSession(
                            userId = userId,
                            date = System.currentTimeMillis(),
                            exercises = exercisesList
                        )
                        coroutineScope.launch {
                            userRepository.addWorkoutSession(userId, session)
                            exercisesList = emptyList()
                            snackbarHostState.showSnackbar("Workout logged successfully!")
                        }
                    },
                    enabled = exercisesList.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(id = R.string.log_workout), fontSize = 18.sp, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun WorkoutInputField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}
