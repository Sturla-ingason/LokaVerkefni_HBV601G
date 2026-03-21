package main.app.serviceModel

import main.app.repository.AuthRepository

class AuthModel {

    private val authRepo = AuthRepository()

    suspend fun logInn(email: String, password: String): Boolean {
        return authRepo.login(email, password)
    }
}
