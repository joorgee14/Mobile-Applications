package com.example.myapplication.Data.Models

data class User(
    val name: String = "",
    val email: String = "",
    val weight: Float = 0f,
    val height: Float = 0f
)

data class WorkoutExercise(
    val exercise: String,
    val sets: Int,
    val reps: Int,
    val weight: Float
)

data class WorkoutSession(
    val userId: String,
    val date: Long,
    val exercises: List<WorkoutExercise>
)
