package com.skrpld.goalion.data.sources.remote

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Remote data source for authentication-related operations using Firebase Authentication.
 * Manages user authentication, registration, and profile updates.
 */
class AuthRemoteDataSource(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {
    /**
     * Gets the currently authenticated Firebase user.
     *
     * @return The FirebaseUser if authenticated, null otherwise
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Gets the ID of the currently authenticated user.
     *
     * @return The user ID if authenticated, null otherwise
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Registers a new user with the provided email and password.
     *
     * @param email The user's email address
     * @param pass The user's password
     * @return The created FirebaseUser, or null if registration failed
     */
    suspend fun signUp(email: String, pass: String): FirebaseUser? {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        return result.user
    }

    /**
     * Authenticates a user with the provided email and password.
     *
     * @param email The user's email address
     * @param pass The user's password
     * @return The authenticated FirebaseUser, or null if authentication failed
     */
    suspend fun signIn(email: String, pass: String): FirebaseUser? {
        val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
        return result.user
    }

    /**
     * Logs out the currently authenticated user.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Updates the email address of the currently authenticated user.
     * Uses the recommended verifyBeforeUpdateEmail method which sends a verification email.
     *
     * @param newEmail The new email address for the user
     * @throws Exception if no user is logged in
     */
    suspend fun updateEmail(newEmail: String) {
        val user = firebaseAuth.currentUser ?: throw Exception("No user logged in")
        user.verifyBeforeUpdateEmail(newEmail).await()
    }

    /**
     * Re-authenticates the current user with the provided credentials.
     *
     * @param email The user's email address
     * @param pass The user's password
     */
    suspend fun reauthenticate(email: String, pass: String) {
        val user = firebaseAuth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(email, pass)
        user.reauthenticate(credential).await()
    }

    /**
     * Updates the password of the currently authenticated user.
     *
     * @param newPass The new password for the user
     * @throws Exception if no user is logged in
     */
    suspend fun updatePassword(newPass: String) {
        val user = firebaseAuth.currentUser ?: throw Exception("No user logged in")
        user.updatePassword(newPass).await()
    }
}

/**
 * Remote data source for user-related operations using Firestore.
 * Manages user data in Firestore database.
 */
class UserRemoteDataSource (private val firestore: FirebaseFirestore) {
    /**
     * Creates or updates a user in the remote database.
     *
     * @param user The NetworkUser object to upsert
     */
    suspend fun upsertUser(user: NetworkUser) {
        firestore.collection("users")
            .document(user.id)
            .set(user, SetOptions.merge())
            .await()
    }

    /**
     * Deletes a user from the remote database.
     *
     * @param userId The unique identifier of the user to delete
     */
    suspend fun deleteUser(userId: String) {
        firestore.collection("users")
            .document(userId)
            .delete().await()
    }

    /**
     * Retrieves a user from the remote database.
     *
     * @param userId The unique identifier of the user to retrieve
     * @return The NetworkUser object if found, null otherwise
     */
    suspend fun getUser(userId: String): NetworkUser? {
        return firestore.collection("users")
            .document(userId)
            .get().await().toObject(NetworkUser::class.java)
    }

    /**
     * Checks if a username is already taken by another user.
     *
     * @param name The username to check
     * @return True if the name is already taken, false otherwise
     */
    suspend fun isNameTaken(name: String): Boolean {
        val snapshot = firestore.collection("users")
            .whereEqualTo("name", name)
            .limit(1)
            .get()
            .await()
        return !snapshot.isEmpty
    }

    /**
     * Checks if an email is already taken by another user.
     *
     * @param email The email to check
     * @return True if the email is already taken, false otherwise
     */
    suspend fun isEmailTaken(email: String): Boolean {
        val snapshot = firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        return !snapshot.isEmpty
    }
}

/**
 * Remote data source for profile-related operations using Firestore.
 * Manages profile data in Firestore database.
 */
class ProfileRemoteDataSource (private val firestore: FirebaseFirestore) {
    /**
     * Creates or updates a profile in the remote database.
     *
     * @param profile The NetworkProfile object to upsert
     */
    suspend fun upsertProfile(profile: NetworkProfile) {
        firestore.collection("profiles")
            .document(profile.id)
            .set(profile, SetOptions.merge())
            .await()
    }

    /**
     * Deletes a profile from the remote database.
     *
     * @param profileId The unique identifier of the profile to delete
     */
    suspend fun deleteProfile(profileId: String) {
        firestore.collection("profiles")
            .document(profileId)
            .delete().await()
    }

    /**
     * Retrieves profiles that have been updated after a specific timestamp.
     *
     * @param userId The unique identifier of the user
     * @param lastUpdate The timestamp to compare against (in milliseconds)
     * @return A list of NetworkProfile objects that have been updated after the specified time
     */
    suspend fun getProfilesUpdatedAfter(userId: String, lastUpdate: Long): List<NetworkProfile> {
        return firestore.collection("profiles")
            .whereEqualTo("userId", userId)
            .whereGreaterThan("updatedAt", lastUpdate)
            .get().await().toObjects(NetworkProfile::class.java)
    }
}

/**
 * Remote data source for goal-related operations using Firestore.
 * Manages goal data in Firestore database.
 */
class GoalRemoteDataSource constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Creates or updates a goal in the remote database.
     *
     * @param goal The NetworkGoal object to upsert
     */
    suspend fun upsertGoal(goal: NetworkGoal) {
        firestore.collection("goals")
            .document(goal.id)
            .set(goal, SetOptions.merge())
            .await()
    }

    /**
     * Deletes a goal from the remote database.
     *
     * @param goalId The unique identifier of the goal to delete
     */
    suspend fun deleteGoal(goalId: String) {
        firestore.collection("goals")
            .document(goalId)
            .delete().await()
    }

    /**
     * Retrieves goals that have been updated after a specific date.
     *
     * @param profileId The unique identifier of the profile
     * @param lastUpdate The date to compare against
     * @return A list of NetworkGoal objects that have been updated after the specified date
     */
    suspend fun getGoalsUpdatedAfter(profileId: String, lastUpdate: Date): List<NetworkGoal> {
        return firestore.collection("goals")
            .whereEqualTo("profileId", profileId)
            .whereGreaterThan("updatedAt", lastUpdate)
            .get().await().toObjects(NetworkGoal::class.java)
    }
}

/**
 * Remote data source for task-related operations using Firestore.
 * Manages task data in Firestore database.
 */
class TaskRemoteDataSource constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Creates or updates a task in the remote database.
     *
     * @param task The NetworkTask object to upsert
     */
    suspend fun upsertTask(task: NetworkTask) {
        firestore.collection("tasks")
            .document(task.id)
            .set(task, SetOptions.merge())
            .await()
    }

    /**
     * Deletes a task from the remote database.
     *
     * @param taskId The unique identifier of the task to delete
     */
    suspend fun deleteTask(taskId: String) {
        firestore.collection("tasks")
            .document(taskId)
            .delete().await()
    }

    /**
     * Retrieves tasks that have been updated after a specific date.
     *
     * @param goalId The unique identifier of the goal
     * @param lastUpdate The date to compare against
     * @return A list of NetworkTask objects that have been updated after the specified date
     */
    suspend fun getTasksUpdatedAfter(goalId: String, lastUpdate: Date): List<NetworkTask> {
        return firestore.collection("tasks")
            .whereEqualTo("goalId", goalId)
            .whereGreaterThan("updatedAt", lastUpdate)
            .get().await().toObjects(NetworkTask::class.java)
    }
}