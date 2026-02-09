package com.skrpld.goalion.domain.usecases

import com.skrpld.goalion.domain.entities.User
import com.skrpld.goalion.domain.repositories.AuthRepository
import com.skrpld.goalion.domain.repositories.UserRepository

data class UserInteractors(
    val getUser: GetUserUseCase,
    val updateUser: UpdateUserUseCase,
    val deleteUser: DeleteUserUseCase
)

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