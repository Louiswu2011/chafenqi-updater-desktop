package util

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

object CFQServer {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    fun onExit() {
        client.close()
    }

    private suspend fun fetchFromServer(
        method: String,
        path: String,
        payload: HashMap<String, Any>? = null,
        queries: Map<String, String>? = null,
        token: String? = null,
        shouldHandleErrorCode: Boolean = true
    ): HttpResponse {
        val response: HttpResponse
        when (method) {
            "GET" -> {
                response = client.get("http://43.139.107.206:8083/$path") {
                    accept(ContentType.Any)
                    queries?.also { q ->
                        url { u ->
                            q.forEach {
                                u.parameters.append(it.key, it.value)
                            }
                        }
                    }
                    token?.also {
                        this.headers.append("Authorization", "Bearer $it")
                    }
                }
            }

            "POST" -> {
                response = client.post("http://43.139.107.206:8083/$path") {
                    accept(ContentType.Any)
                    payload?.also {
                        this.contentType(ContentType.Application.Json)
                        this.setBody(it)
                    }
                    token?.also {
                        this.headers.append("Authorization", "Bearer $it")
                    }
                }
            }

            else -> {
                throw Exception("Method not supported.")
            }
        }
        if (response.status.value != 200 && shouldHandleErrorCode) {
            handleErrorCode(response.bodyAsText())
        }
        return response
    }

    suspend fun authenticate(username: String, password: String): String {
        val response = fetchFromServer(
            "POST",
            "api/auth",
            payload = hashMapOf(
                "username" to username,
                "password" to password
            )
        )
        val errorCode = response.bodyAsText()
        val header = response.headers["Authorization"]?.substring(7)

        return header ?: ""
    }

    suspend fun apiIsPremium(username: String): Boolean {
        val response = fetchFromServer(
            "POST",
            "api/isPremium",
            payload = hashMapOf(
                "username" to username
            ),
            shouldHandleErrorCode = false
        )
        return response.status.value == 200
    }

    suspend fun apiCheckPremiumTime(username: String): Double {
        return try {
            val response = fetchFromServer(
                "POST",
                "api/premiumTime",
                payload = hashMapOf(
                    "username" to username
                )
            )
            response.bodyAsText().toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun apiFetchUserOption(token: String, param: String): String {
        return try {
            val response = fetchFromServer(
                "GET",
                "api/user/option",
                token = token,
                queries = mapOf("param" to param)
            )
            response.bodyAsText()
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun apiUploadUserOption(token: String, param: String, value: String): Boolean {
        return try {
            val response = fetchFromServer(
                "POST",
                "api/user/option",
                payload = hashMapOf(
                    "param" to param,
                    "value" to value
                ),
                token = token
            )
            response.status.value == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fishFetchToken(authToken: String) = fetchFromServer(
        "GET",
        "fish/fetch_token",
        token = authToken
    ).bodyAsText()


    suspend fun fishUploadToken(authToken: String, fishToken: String): Boolean {
        val response = fetchFromServer(
            "POST",
            "fish/upload_token",
            payload = hashMapOf(
                "token" to fishToken
            ),
            token = authToken
        )
        return response.status.value == 200
    }

    private fun handleErrorCode(errorCode: String) {
        when (errorCode) {
            "MISMATCH" -> throw CredentialsMismatchException()
            "INVALID" -> throw InvalidTokenException()
            "NOT FOUND" -> throw UserNotFoundException()
            "EMPTY" -> throw EmptyUserDataException()
            "NOT UNIQUE" -> throw UsernameOccupiedException()
            else -> throw CFQServerSideException(errorCode = errorCode)
        }
    }
}

class CredentialsMismatchException : Exception()
class InvalidTokenException : Exception()
class UserNotFoundException : Exception()
class EmptyUserDataException : Exception()
class UsernameOccupiedException : Exception()
class CFQServerSideException(errorCode: String) : Exception(errorCode)