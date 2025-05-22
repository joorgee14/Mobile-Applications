package com.example.myapplication.Data.Models

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    // Function to save a user to Firestore
    suspend fun saveUser(userId: String, user: User) {
        try {
            db.collection("users")
                .document(userId)  // The document ID will be the user's userId
                .set(user)         // Save the User object to Firestore
                .await()           // Wait for the operation to complete
        } catch (e: Exception) {
            println("Error saving user: ${e.message}")
        }
    }

    // Function to save a new workout session associated with the user
    suspend fun addWorkoutSession(userEmail: String, workoutSession: WorkoutSession) {
        try {
            // Create a new document in the workout_sessions collection
            val sessionRef = db.collection("workout_sessions")
                .add(workoutSession.copy(userId = userEmail))  // Use the userEmail as userId
                .await()

            println("Workout session saved successfully with ID: ${sessionRef.id}")
        } catch (e: Exception) {
            println("Error saving workout session: ${e.message}")
        }
    }

    // Function to retrieve a user by their email
    suspend fun getUserByEmail(email: String): User? {
        return try {
            val document = db.collection("users")
                .document(email)  // Use the email as the document name
                .get()
                .await()
            document.toObject(User::class.java)  // Convert the document to a User object
        } catch (e: Exception) {
            null  // Return null if an error occurs
        }
    }




    // Function to retrieve the user's workout sessions
    suspend fun getWorkoutSessionsByUser(userId: String): List<WorkoutSession> {
        val workoutSessions = mutableListOf<WorkoutSession>()
        try {
            val result = db.collection("workout_sessions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            workoutSessions.addAll(result.documents.map { document ->
                val dateLong = document.getLong("date") ?: 0L
                val exercisesData = document.get("exercises") as? List<Map<String, Any>> ?: emptyList()
                val exercises = exercisesData.map { exerciseData ->
                    val exerciseName = exerciseData["exercise"] as? String ?: ""
                    val sets = (exerciseData["sets"] as? Long)?.toInt() ?: 0
                    val reps = (exerciseData["reps"] as? Long)?.toInt() ?: 0
                    val weight = (exerciseData["weight"] as? Double)?.toFloat() ?: 0.0f
                    WorkoutExercise(exerciseName, sets, reps, weight)
                }
                val userId = document.getString("userId") ?: ""
                WorkoutSession(userId, dateLong, exercises)
            })
        } catch (e: Exception) {
            println("Error retrieving workout sessions: ${e.message}")
        }
        return workoutSessions
    }

    // Function to convert a timestamp to a readable date string
    fun convertTimestampToDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return dateFormat.format(date)
    }

    // Function to update the user's profile data in the database
    suspend fun updateUserProfile(email: String, name: String, height: Float, weight: Float) {
        try {
            val userRef = db.collection("users").whereEqualTo("email", email).get().await()

            if (userRef.documents.isNotEmpty()) {
                val userDocId = userRef.documents.first().id

                val updatedUser = mapOf(
                    "name" to name,
                    "height" to height,
                    "weight" to weight
                )

                db.collection("users").document(userDocId).update(updatedUser)
                println("User profile updated successfully")
            }
        } catch (e: Exception) {
            println("Error updating user profile: ${e.message}")
        }
    }
}
