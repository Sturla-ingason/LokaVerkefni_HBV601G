package main.app.repository

import io.ktor.client.call.body
import io.ktor.client.request.get
import main.app.apiConnections.HttpRoutes
import main.app.apiConnections.KtorClient
import main.app.dataModel.User

class UserRepository {

    suspend fun getFollowerCount(): Int {
        return KtorClient.httpClient.get(HttpRoutes.FOLLOWER_COUNT).body()
    }


    suspend fun getFollowingCount(): Int {
        return KtorClient.httpClient.get(HttpRoutes.FOLLOWING_COUNT).body()
    }


    suspend fun getUser(): User {
        return KtorClient.httpClient.get(HttpRoutes.GET_USER).body()
    }

}