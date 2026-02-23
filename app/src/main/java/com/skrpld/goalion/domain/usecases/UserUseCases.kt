package com.skrpld.goalion.domain.usecases

import android.util.Log
import com.skrpld.goalion.domain.model.User
import com.skrpld.goalion.domain.repositories.AuthRepository
import com.skrpld.goalion.domain.repositories.UserRepository

private const val TAG = "GoalionLog_UserUC"

data class UserInteractors(
    val signUp: SignUpUseCase,
    val signIn: SignInUseCase,
    val logout: LogoutUseCase,
    val reauthenticate: ReauthenticateAndSaveUseCase,
    val changePassword: ChangePasswordUseCase,

    val getUser: GetUserUseCase,
    val updateUser: UpdateUserUseCase,
    val deleteUser: DeleteUserUseCase
)

/**
 * Signs up a new user with validation.
 * Validates credentials and creates user in both auth and user repositories.
 */
class SignUpUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    private val nameRegex = Regex("^[a-zA-Z0-9]{6,}$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(name: String, email: String, pass: String): Result<User> {
        Log.d(TAG, "Attempting SignUp for email: $email, name: $name")
        if (!name.matches(nameRegex)) {
            Log.w(TAG, "SignUp Validation failed: Invalid Name")
            return Result.failure(IllegalArgumentException("Name must contain only English letters and digits, and be at least 6 characters long"))
        }
        if (!email.matches(emailRegex)) {
            Log.w(TAG, "SignUp Validation failed: Invalid Email")
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        if (pass.length < 6) {
            Log.w(TAG, "SignUp Validation failed: Password too short")
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long"))
        }

        return try {
            if (userRepository.isNameTaken(name)) {
                Log.w(TAG, "SignUp failed: Username '$name' is already taken")
                return Result.failure(IllegalArgumentException("Username '$name' is already taken"))
            }

            if (userRepository.isEmailTaken(email)) {
                Log.w(TAG, "SignUp failed: Email '$email' is already in use")
                return Result.failure(IllegalArgumentException("Email '$email' is already in use"))
            }

            Log.d(TAG, "Validation passed, calling authRepository.signUp")
            authRepository.signUp(name, email, pass)
        } catch (e: Exception) {
            Log.e(TAG, "SignUp Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}

/**
 * Signs in an existing user with validation.
 * Authenticates user and initiates sync after successful login.
 */
class SignInUseCase(
    private val authRepository: AuthRepository
) {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(email: String, pass: String): Result<User> {
        Log.d(TAG, "Attempting SignIn for email: $email")
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be empty"))
        if (!email.matches(emailRegex)) return Result.failure(IllegalArgumentException("Invalid email format"))
        if (pass.isBlank()) return Result.failure(IllegalArgumentException("Password cannot be empty"))

        Log.d(TAG, "Validation passed, calling authRepository.signIn")
        return authRepository.signIn(email, pass)
    }
}

/**
 * Logs out the current user.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        Log.d(TAG, "Calling authRepository.logout")
        authRepository.logout()
    }
}

/**
 * Re-authenticates user and saves data.
 * Used when operations require recent authentication.
 */
class ReauthenticateAndSaveUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String, userToSave: User) {
        Log.d(TAG, "Reauthenticating and saving for user: ${userToSave.id}")
        authRepository.reLoginAndRetry(email, pass, userToSave)
    }
}

/**
 * Changes user password with validation.
 * Re-authenticates before changing password.
 */
class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(currentPass: String, newPass: String, confirmNewPass: String): Result<Unit> {
        Log.d(TAG, "Attempting to change password")
        if (currentPass.isBlank()) return Result.failure(IllegalArgumentException("Current password cannot be empty"))
        if (newPass.length < 6) return Result.failure(IllegalArgumentException("New password must be at least 6 characters long"))
        if (newPass != confirmNewPass) return Result.failure(IllegalArgumentException("New passwords do not match"))
        if (currentPass == newPass) return Result.failure(IllegalArgumentException("New password cannot be the same as the old one"))

        return authRepository.changePassword(currentPass, newPass)
    }
}

/**
 * Gets the currently authenticated user.
 * Retrieves user from local storage using auth token.
 */
class GetUserUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User? {
        Log.d(TAG, "GetUserUseCase invoked")
        val userId = authRepository.getCurrentUser() ?: run {
            Log.d(TAG, "No current user ID found in Auth")
            return null
        }

        val localUser = userRepository.getUser(userId)
        if (localUser != null) {
            Log.d(TAG, "[LOCAL_DB] User found locally: ${localUser.id}")
            return localUser
        }

        Log.d(TAG, "[DOWNLOAD] User not found locally, fetching from remote for ID: $userId")
        return userRepository.fetchUserFromRemote(userId)
    }
}

/**
 * Updates user information with validation.
 * Checks for duplicate names/emails before updating and handles Firebase Auth updates.
 */
class UpdateUserUseCase(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {
    private val nameRegex = Regex("^[a-zA-Z0-9]{6,}$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(id: String, name: String, email: String) {
        Log.d(TAG, "Updating User ID: $id. New Name: $name, New Email: $email")
        if (id.isBlank()) throw IllegalArgumentException("User ID cannot be empty")
        if (!name.matches(nameRegex)) throw IllegalArgumentException("Name must contain only English letters and digits, and be at least 6 characters long")
        if (!email.matches(emailRegex)) throw IllegalArgumentException("Invalid email format")

        val currentUser = userRepository.getUser(id)
            ?: throw IllegalStateException("User not found locally")

        if (currentUser.name != name) {
            val isNameTaken = userRepository.isNameTaken(name)
            if (isNameTaken) throw IllegalArgumentException("Username '$name' is already taken")
        }

        if (currentUser.email != email) {
            val isEmailTaken = userRepository.isEmailTaken(email)
            if (isEmailTaken) throw IllegalArgumentException("Email '$email' is already in use")
        }

        val updatedUser = currentUser.copy(name = name, email = email)
        Log.d(TAG, "[UPLOAD] Validation passed, passing updated user to authRepository.upsertUser")
        authRepository.upsertUser(updatedUser)
    }
}

class DeleteUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String) {
        Log.w(TAG, "[UPLOAD] Deleting user ID: $userId")
        userRepository.delete(userId)
    }
}