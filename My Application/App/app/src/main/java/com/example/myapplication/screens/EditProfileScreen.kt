package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Data.Models.User
import com.example.myapplication.Data.Models.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.myapplication.R

//AI PROMPT: HELP ME TO CREATE A SCREEN WICH EDIT MY ACTUAL VALUES FOR NAME, WEIGHT AND HEIGHT
// AND STORES THIS NEW VALUES IN THE DATABASE


// AI PROMPT: HELP ME TO CREATE A BUTTON TO LOG OUT

@Composable
fun EditProfileScreen(navController: NavController, userId: String) {
    val auth = FirebaseAuth.getInstance()
    val email = auth.currentUser?.email ?: ""
    val repository = remember { UserRepository() }

    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf(0f) }
    var weight by remember { mutableStateOf(0f) }

    // Load user data once on screen load
    LaunchedEffect(userId) {
        user = repository.getUserByEmail(email)
        user?.let {
            name = it.name
            height = it.height
            weight = it.weight
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFBBDEFB), Color(0xFFE3F2FD))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = stringResource(id = R.string.edit_profile_title),
                    fontSize = 28.sp,
                    color = Color(0xFF1565C0)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Spacer(modifier = Modifier.height(20.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Height field
                OutlinedTextField(
                    value = if (height == 0f) "" else height.toString(),
                    onValueChange = { height = it.toFloatOrNull() ?: 0f },
                    label = { Text(stringResource(id = R.string.height_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Weight field
                OutlinedTextField(
                    value = if (weight == 0f) "" else weight.toString(),
                    onValueChange = { weight = it.toFloatOrNull() ?: 0f },
                    label = { Text(stringResource(id = R.string.weight_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save changes button
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            user?.let {
                                repository.updateUserProfile(
                                    email = email,
                                    name = name,
                                    height = height,
                                    weight = weight
                                )
                            }
                        }
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
                ) {
                    Text(
                        text = stringResource(id = R.string.save_changes),
                        color = Color.White
                    )
                }
            }
        }
    }
}
