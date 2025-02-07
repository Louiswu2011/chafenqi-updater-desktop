package util

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import user.CFQUserInfo

object CFQServer {
    val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

    fun onExit() {
        client.close()
    }

    private val decoder = Json { ignoreUnknownKeys = true }

    private const val SERVER_ADDRESS = "http://43.139.107.206:8998"

    private suspend fun fetchFromServer(
        method: String,
        path: String,
        payload: HashMap<String, Any>? = null,
        queries: Map<String, String>? = null,
        token: String? = null,
    ): HttpResponse {
        val response: HttpResponse
        when (method) {
            "GET" -> {
                response =
                    client.get("$SERVER_ADDRESS/$path") {
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
                response =
                    client.post("$SERVER_ADDRESS/$path") {
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

            "DELETE" -> {
                response =
                    client.delete("$SERVER_ADDRESS/$path") {
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

            else -> {
                throw Exception("Method not supported.")
            }
        }
        return response
    }

    suspend fun authenticate(
        username: String,
        password: String,
    ): Pair<String, HttpStatusCode> {
        val response =
            fetchFromServer(
                "POST",
                "api/auth/login",
                payload =
                    hashMapOf(
                        "username" to username,
                        "password" to password,
                    ),
            )
        return response.bodyAsText() to response.status
    }

    suspend fun apiIsPremium(authToken: String): Boolean {
        val response =
            fetchFromServer(
                "GET",
                "api/user/info",
                token = authToken,
            )
        val info = decoder.decodeFromString<CFQUserInfo>(response.bodyAsText())
        return info.premiumUntil >= Clock.System.now().epochSeconds
    }

    suspend fun apiCheckPremiumTime(authToken: String): Long =
        try {
            val response =
                fetchFromServer(
                    "POST",
                    "api/user/info",
                    token = authToken,
                )
            val info = decoder.decodeFromString<CFQUserInfo>(response.bodyAsText())
            info.premiumUntil
        } catch (e: Exception) {
            0L
        }

    suspend fun apiFetchUserOption(
        authToken: String,
        param: String,
        type: String = "string",
    ): String =
        try {
            val response =
                fetchFromServer(
                    "GET",
                    "api/user/properties",
                    token = authToken,
                    queries = mapOf("param" to param, "type" to type),
                )
            response.bodyAsText()
        } catch (e: Exception) {
            ""
        }

    suspend fun apiUploadUserOption(
        token: String,
        param: String,
        value: String,
    ): Boolean =
        try {
            val response =
                fetchFromServer(
                    "POST",
                    "api/user/properties",
                    payload =
                        hashMapOf(
                            "property" to param,
                            "value" to value,
                        ),
                    token = token,
                )
            response.status.value == 200
        } catch (e: Exception) {
            false
        }

    suspend fun fishFetchToken(authToken: String) = apiFetchUserOption(authToken, "fish_token")

    suspend fun fishUploadToken(
        authToken: String,
        fishToken: String,
    ): Boolean = apiUploadUserOption(authToken, "fish_token", fishToken)

    suspend fun apiFetchBindQQ(authToken: String): String {
        try {
            val response =
                fetchFromServer(
                    "GET",
                    "api/user/bind",
                    token = authToken,
                )
            return response.bodyAsText()
        } catch (_: Exception) {
            return ""
        }
    }

    suspend fun apiUpdateBindQQ(
        authToken: String,
        qq: String,
    ): Boolean {
        try {
            val response =
                fetchFromServer(
                    "POST",
                    "api/user/bind",
                    payload = hashMapOf("qq" to qq),
                    token = authToken,
                )
            return response.status == HttpStatusCode.OK
        } catch (_: Exception) {
            return false
        }
    }
}
