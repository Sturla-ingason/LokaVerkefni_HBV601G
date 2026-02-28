package main.app.serviceModel

import main.app.repository.AuthRepository
import main.app.repository.UserRepository

class AuthModel {

    val authRepo = AuthRepository()
    val userRepo = UserRepository()

    fun logInn(email: String, password: String) : Boolean{

        if(email.isEmpty() || password.isEmpty()){
            return false
        }else{
            return true
        }

    }


}