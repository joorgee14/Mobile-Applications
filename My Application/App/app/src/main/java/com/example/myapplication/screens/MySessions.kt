package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Data.Models.UserRepository
import com.example.myapplication.Data.Models.WorkoutSession
import com.example.myapplication.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


// AI PROMPT: HELP ME TO MAKE THIS SCREEN LOOKS BETTER VISUALLY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySessionsScreen(navController: NavController, userId: String) {
    val workoutSessions = remember { mutableStateOf<List<WorkoutSession>>(listOf()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val userRepository = UserRepository()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val sessions = userRepository.getWorkoutSessionsByUser(userId)
                workoutSessions.value = sessions.sortedByDescending { it.date }
                isLoading.value = false
            } catch (e: Exception) {
                errorMessage.value = context.getString(R.string.error_loading_sessions, e.message)
                isLoading.value = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.my_workout_sessions),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF6A1B9A)
                )
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F0F8))
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    errorMessage.value?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    if (workoutSessions.value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_sessions_found),
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 20.dp)
                        )
                    } else {
                        LazyColumn {
                            items(workoutSessions.value) { session ->
                                BeautifulWorkoutCard(session)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeautifulWorkoutCard(session: WorkoutSession) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = convertTimestampToDate(session.date),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF8E24AA),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            session.exercises.forEachIndexed { index, exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = stringResource(R.string.exercise_icon),
                        tint = Color(0xFF5E35B1),
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.exercise,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "Sets: ${exercise.sets} • Reps: ${exercise.reps} • Weight: ${exercise.weight} kg",
                            fontSize = 14.sp,
                            color = Color(0xFF616161)
                        )
                    }
                }

                if (index != session.exercises.lastIndex) {
                    Divider(
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

fun convertTimestampToDate(timestamp: Long): String {
    val date = Date(timestamp)
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(date)
}
