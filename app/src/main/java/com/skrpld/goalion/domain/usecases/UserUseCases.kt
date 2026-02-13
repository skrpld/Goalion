package com.skrpld.goalion.domain.usecases

import com.skrpld.goalion.domain.model.User
import com.skrpld.goalion.domain.repositories.AuthRepository
import com.skrpld.goalion.domain.repositories.UserRepository

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
        if (!name.matches(nameRegex)) {
            return Result.failure(IllegalArgumentException("Name must contain only English letters and digits, and be at least 6 characters long"))
        }

        if (!email.matches(emailRegex)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (pass.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long"))
        }

        return try {
            if (userRepository.isNameTaken(name)) {
                return Result.failure(IllegalArgumentException("Username '$name' is already taken"))
            }

            if (userRepository.isEmailTaken(email)) {
                return Result.failure(IllegalArgumentException("Email '$email' is already in use"))
            }

            authRepository.signUp(name, email, pass)
        } catch (e: Exception) {
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
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }

        if (!email.matches(emailRegex)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }

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
        if (currentPass.isBlank()) {
            return Result.failure(IllegalArgumentException("Current password cannot be empty"))
        }
        if (newPass.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters long"))
        }
        if (newPass != confirmNewPass) {
            return Result.failure(IllegalArgumentException("New passwords do not match"))
        }
        if (currentPass == newPass) {
            return Result.failure(IllegalArgumentException("New password cannot be the same as the old one"))
        }

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
        val userId = authRepository.getCurrentUser() ?: return null
        return userRepository.getUser(userId)
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
        if (id.isBlank()) {
            throw IllegalArgumentException("User ID cannot be empty")
        }
        if (!name.matches(nameRegex)) {
            throw IllegalArgumentException("Name must contain only English letters and digits, and be at least 6 characters long")
        }
        if (!email.matches(emailRegex)) {
            throw IllegalArgumentException("Invalid email format")
        }

        val currentUser = userRepository.getUser(id)
            ?: throw IllegalStateException("User not found locally")

        if (currentUser.name != name) {
            val isNameTaken = userRepository.isNameTaken(name)
            if (isNameTaken) {
                throw IllegalArgumentException("Username '$name' is already taken")
            }
        }

        if (currentUser.email != email) {
            val isEmailTaken = userRepository.isEmailTaken(email)
            if (isEmailTaken) {
                throw IllegalArgumentException("Email '$email' is already in use")
            }
        }

        val updatedUser = currentUser.copy(
            name = name,
            email = email
        )

        authRepository.upsertUser(updatedUser)
    }
}

class DeleteUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String) {
        userRepository.delete(userId)
    }
}