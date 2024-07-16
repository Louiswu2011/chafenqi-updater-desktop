package util

import io.ktor.client.request.*
import io.ktor.http.*

class FishServer {
    companion object {
        suspend fun getUserToken(username: String, password: String): String {
            try {
                val response =
                    CFQServer.client.post {
                        url("https://www.diving-fish.com/api/maimaidxprober/login")
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                        setBody(
                            hashMapOf(
                                "username" to username,
                                "password" to password
                            )
                        )
                        contentLength()
                    }

                if (response.status.value == 401) {
                    return ""
                }

                val cookies = response.headers["Set-Cookie"] ?: ""
                if (cookies.isEmpty()) {
                    println("Set-Cookie is empty :(")
                    return ""
                }
                val tokenComponent = cookies.split(";")[0]
                val token = tokenComponent.substringAfter("=", "")
                if (token.isEmpty()) {
                    println("Cannot parse token component $tokenComponent")
                    return ""
                }

                return token
            } catch (e: Exception) {
                println("Cannot get fish token, error: $e")
                return ""
            }
        }

        suspend fun checkTokenValidity(fishToken: String): Boolean {
            val response =
                CFQServer.client.get {
                    url("https://www.diving-fish.com/api/maimaidxprober/player/profile")
                    cookie("jwt_token", fishToken)
                    accept(ContentType.Application.Json)
                }
            return response.status.value == 200
        }
    }
}