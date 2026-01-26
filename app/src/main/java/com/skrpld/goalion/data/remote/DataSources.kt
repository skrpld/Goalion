package com.skrpld.goalion.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSource(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    suspend fun signUp(email: String, pass: String): FirebaseUser? {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        return result.user
    }

    suspend fun signIn(email: String, pass: String): FirebaseUser? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
        return result.user
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
class UserRemoteDataSource constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun upsertUser(user: NetworkUser) {
        firestore.collection("users")
            .document(user.id)
            .set(user, SetOptions.merge())
            .await()
    }

    suspend fun getUser(userId: String): NetworkUser? {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(NetworkUser::class.java)
    }

    suspend fun deleteUser(userId: String) {
        firestore.collection("users")
            .document(userId)
            .delete()
            .await()
    }
}

class ProfileRemoteDataSource constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun upsertProfile(profile: NetworkProfile) {
        firestore.collection("profiles")
            .document(profile.id)
            .set(profile, SetOptions.merge())
            .await()
    }

    suspend fun getProfilesByUser(userId: String): List<NetworkProfile> {
        return firestore.collection("profiles")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDeleted", false)
            .get()
            .await()
            .toObjects(NetworkProfile::class.java)
    }

    suspend fun deleteProfile(profileId: String) {
        firestore.collection("profiles")
            .document(profileId)
            .delete()
            .await()
    }
}

class GoalRemoteDataSource constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun upsertGoal(goal: NetworkGoal) {
        firestore.collection("goals")
            .document(goal.id)
            .set(goal, SetOptions.merge())
            .await()
    }

    suspend fun getGoalsByProfile(profileId: String): List<NetworkGoal> {
        return firestore.collection("goals")
            .whereEqualTo("profileId", profileId)
            .get()
            .await()
            .toObjects(NetworkGoal::class.java)
    }

    suspend fun getGoalsUpdatedAfter(profileId: String, lastSyncTimestamp: java.util.Date): List<NetworkGoal> {
        return firestore.collection("goals")
            .whereEqualTo("profileId", profileId)
            .whereGreaterThan("updatedAt", lastSyncTimestamp)
            .get()
            .await()
            .toObjects(NetworkGoal::class.java)
    }

    suspend fun deleteGoal(goalId: String) {
        firestore.collection("goals")
            .document(goalId)
            .delete()
            .await()
    }
}

class TaskRemoteDataSource constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun upsertTask(task: NetworkTask) {
        firestore.collection("tasks")
            .document(task.id)
            .set(task, SetOptions.merge())
            .await()
    }

    suspend fun getTasksByGoal(goalId: String): List<NetworkTask> {
        return firestore.collection("tasks")
            .whereEqualTo("goalId", goalId)
            .get()
            .await()
            .toObjects(NetworkTask::class.java)
    }

    suspend fun getTasksUpdatedAfter(goalId: String, lastSyncTimestamp: java.util.Date): List<NetworkTask> {
        return firestore.collection("tasks")
            .whereEqualTo("goalId", goalId)
            .whereGreaterThan("updatedAt", lastSyncTimestamp)
            .get()
            .await()
            .toObjects(NetworkTask::class.java)
    }

    suspend fun deleteTask(taskId: String) {
        firestore.collection("tasks")
            .document(taskId)
            .delete()
            .await()
    }
}