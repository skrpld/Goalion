package com.skrpld.goalion.data.sources.remote

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

private const val TAG = "GoalionLog_RemoteSources"

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
        val user = firebaseAuth.currentUser
        Log.d(TAG, "getCurrentUser() called. Found: ${user?.uid}")
        return user
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
        Log.d(TAG, "[UPLOAD] Firebase Auth SignUp requested for: $email")
        val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        Log.d(TAG, "[UPLOAD] Firebase Auth SignUp success. UID: ${result.user?.uid}")
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
        Log.d(TAG, "[DOWNLOAD] Firebase Auth SignIn requested for: $email")
        val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
        Log.d(TAG, "[DOWNLOAD] Firebase Auth SignIn success. UID: ${result.user?.uid}")
        return result.user
    }

    /**
     * Logs out the currently authenticated user.
     */
    fun logout() {
        Log.d(TAG, "Firebase Auth Logout executed")
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
        Log.d(TAG, "[UPLOAD] Requesting email update to: $newEmail")
        val user = firebaseAuth.currentUser ?: throw Exception("No user logged in")
        user.verifyBeforeUpdateEmail(newEmail).await()
        Log.d(TAG, "[UPLOAD] Verification email sent for new address.")
    }

    /**
     * Re-authenticates the current user with the provided credentials.
     *
     * @param email The user's email address
     * @param pass The user's password
     */
    suspend fun reauthenticate(email: String, pass: String) {
        Log.d(TAG, "[UPLOAD] Re-authenticating user: $email")
        val user = firebaseAuth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(email, pass)
        user.reauthenticate(credential).await()
        Log.d(TAG, "[UPLOAD] Re-authentication successful.")
    }

    /**
     * Updates the password of the currently authenticated user.
     *
     * @param newPass The new password for the user
     * @throws Exception if no user is logged in
     */
    suspend fun updatePassword(newPass: String) {
        Log.d(TAG, "[UPLOAD] Requesting password update")
        val user = firebaseAuth.currentUser ?: throw Exception("No user logged in")
        user.updatePassword(newPass).await()
        Log.d(TAG, "[UPLOAD] Password updated successfully.")
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
        Log.d(TAG, "[UPLOAD] Upserting User in Firestore. ID: ${user.id}, Email: ${user.email}")
        firestore.collection("users")
            .document(user.id)
            .set(user, SetOptions.merge())
            .await()
        Log.d(TAG, "[UPLOAD] User upserted to Firestore successfully.")
    }

    /**
     * Deletes a user from the remote database.
     *
     * @param userId The unique identifier of the user to delete
     */
    suspend fun deleteUser(userId: String) {
        Log.w(TAG, "[UPLOAD] Deleting User from Firestore. ID: $userId")
        firestore.collection("users")
            .document(userId)
            .delete().await()
        Log.d(TAG, "[UPLOAD] User deleted from Firestore successfully.")
    }

    /**
     * Retrieves a user from the remote database.
     *
     * @param userId The unique identifier of the user to retrieve
     * @return The NetworkUser object if found, null otherwise
     */
    suspend fun getUser(userId: String): NetworkUser? {
        Log.d(TAG, "[DOWNLOAD] Fetching User from Firestore. ID: $userId")
        val user = firestore.collection("users")
            .document(userId)
            .get().await().toObject(NetworkUser::class.java)
        Log.d(TAG, "[DOWNLOAD] User fetched: ${user?.email ?: "Not Found"}")
        return user
    }

    /**
     * Checks if a username is already taken by another user.
     *
     * @param name The username to check
     * @return True if the name is already taken, false otherwise
     */
    suspend fun isNameTaken(name: String): Boolean {
        Log.d(TAG, "[DOWNLOAD] Checking if username is taken in Firestore: $name")
        val snapshot = firestore.collection("users")
            .whereEqualTo("name", name)
            .limit(1)
            .get()
            .await()
        val isTaken = !snapshot.isEmpty
        Log.d(TAG, "[DOWNLOAD] Username '$name' taken: $isTaken")
        return isTaken
    }

    /**
     * Checks if an email is already taken by another user.
     *
     * @param email The email to check
     * @return True if the email is already taken, false otherwise
     */
    suspend fun isEmailTaken(email: String): Boolean {
        Log.d(TAG, "[DOWNLOAD] Checking if email is taken in Firestore: $email")
        val snapshot = firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        val isTaken = !snapshot.isEmpty
        Log.d(TAG, "[DOWNLOAD] Email '$email' taken: $isTaken")
        return isTaken
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
        Log.d(TAG, "[UPLOAD] Upserting Profile in Firestore. ID: ${profile.id}, UserID: ${profile.userId}")
        firestore.collection("profiles")
            .document(profile.id)
            .set(profile, SetOptions.merge())
            .await()
        Log.d(TAG, "[UPLOAD] Profile upserted to Firestore successfully.")
    }

    /**
     * Deletes a profile from the remote database.
     *
     * @param profileId The unique identifier of the profile to delete
     */
    suspend fun deleteProfile(profileId: String) {
        Log.w(TAG, "[UPLOAD] Deleting Profile from Firestore. ID: $profileId")
        firestore.collection("profiles")
            .document(profileId)
            .delete().await()
        Log.d(TAG, "[UPLOAD] Profile deleted from Firestore successfully.")
    }

    /**
     * Retrieves profiles that have been updated after a specific timestamp.
     *
     * @param userId The unique identifier of the user
     * @param lastUpdate The timestamp to compare against (in milliseconds)
     * @return A list of NetworkProfile objects that have been updated after the specified time
     */
    suspend fun getProfilesUpdatedAfter(userId: String, lastUpdate: Long): List<NetworkProfile> {
        Log.d(TAG, "[DOWNLOAD] Fetching profiles for User: $userId updated after: $lastUpdate")
        val profiles = firestore.collection("profiles")
            .whereEqualTo("userId", userId)
            .whereGreaterThan("updatedAt", lastUpdate)
            .get().await().toObjects(NetworkProfile::class.java)
        Log.d(TAG, "[DOWNLOAD] Fetched ${profiles.size} updated profiles from Firestore.")
        return profiles
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
        Log.d(TAG, "[UPLOAD] Upserting Goal in Firestore. ID: ${goal.id}, ProfileID: ${goal.profileId}")
        firestore.collection("goals")
            .document(goal.id)
            .set(goal, SetOptions.merge())
            .await()
        Log.d(TAG, "[UPLOAD] Goal upserted to Firestore successfully.")
    }

    /**
     * Deletes a goal from the remote database.
     *
     * @param goalId The unique identifier of the goal to delete
     */
    suspend fun deleteGoal(goalId: String) {
        Log.w(TAG, "[UPLOAD] Deleting Goal from Firestore. ID: $goalId")
        firestore.collection("goals")
            .document(goalId)
            .delete().await()
        Log.d(TAG, "[UPLOAD] Goal deleted from Firestore successfully.")
    }

    /**
     * Retrieves goals that have been updated after a specific date.
     *
     * @param profileId The unique identifier of the profile
     * @param lastUpdate The date to compare against
     * @return A list of NetworkGoal objects that have been updated after the specified date
     */
    suspend fun getGoalsUpdatedAfter(profileId: String, lastUpdate: Date): List<NetworkGoal> {
        Log.d(TAG, "[DOWNLOAD] Fetching goals for Profile: $profileId updated after: $lastUpdate")
        val goals = firestore.collection("goals")
            .whereEqualTo("profileId", profileId)
            .whereGreaterThan("updatedAt", lastUpdate)
            .get().await().toObjects(NetworkGoal::class.java)
        Log.d(TAG, "[DOWNLOAD] Fetched ${goals.size} updated goals from Firestore.")
        return goals
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
        Log.d(TAG, "[UPLOAD] Upserting Task in Firestore. ID: ${task.id}, GoalID: ${task.goalId}")
        firestore.collection("tasks")
            .document(task.id)
            .set(task, SetOptions.merge())
            .await()
        Log.d(TAG, "[UPLOAD] Task upserted to Firestore successfully.")
    }

    /**
     * Deletes a task from the remote database.
     *
     * @param taskId The unique identifier of the task to delete
     */
    suspend fun deleteTask(taskId: String) {
        Log.w(TAG, "[UPLOAD] Deleting Task from Firestore. ID: $taskId")
        firestore.collection("tasks")
            .document(taskId)
            .delete().await()
        Log.d(TAG, "[UPLOAD] Task deleted from Firestore successfully.")
    }

    /**
     * Retrieves tasks that have been updated after a specific date.
     *
     * @param goalId The unique identifier of the goal
     * @param lastUpdate The date to compare against
     * @return A list of NetworkTask objects that have been updated after the specified date
     */
    suspend fun getTasksUpdatedAfter(goalId: String, lastUpdate: Date): List<NetworkTask> {
        Log.d(TAG, "[DOWNLOAD] Fetching tasks for Goal: $goalId updated after: $lastUpdate")
        val tasks = firestore.collection("tasks")
            .whereEqualTo("goalId", goalId)
            .whereGreaterThan("updatedAt", lastUpdate)
            .get().await().toObjects(NetworkTask::class.java)
        Log.d(TAG, "[DOWNLOAD] Fetched ${tasks.size} updated tasks from Firestore.")
        return tasks
    }
}