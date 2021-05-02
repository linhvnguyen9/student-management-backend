package me.linh

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.FileInputStream


fun main() {
    initFirebase()

    embeddedServer(Netty, port = 8000) {
        routing {
            get("/") {
                call.respondText("Hello, world!")
            }
            route("/admin") {
                post("/register/{uid}") {
                    val uid = call.parameters["uid"]
                    println(uid)
                    try {
                        registerAdmin(uid ?: "")
                        call.respondText { "Okay" }
                    } catch (e: Exception) {
                        println(e.printStackTrace())
                        call.response.status(HttpStatusCode.BadRequest)
                        call.respondText { "Invalid uid" }
                    }
                }
                get("/{uid}") {
                    val uid = call.parameters["uid"]
                    println(uid)

                    call.respondText(status = HttpStatusCode.OK) {
                        getUserClaims(uid ?: "").toString()
                    }
                }
            }
        }
    }.start(wait = true)
}

private fun initFirebase() {
    val serviceAccount = FileInputStream(System.getProperty("user.dir") + "/" + "src/main/resources/student-management-5792a-firebase-adminsdk-pgu7w-7e07c84619.json")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://student-management-5792a-default-rtdb.asia-southeast1.firebasedatabase.app")
        .build()

    FirebaseApp.initializeApp(options)
}

private fun registerAdmin(uid: String) {
    val claims: MutableMap<String, Any> = HashMap()
    claims["admin"] = true
    FirebaseAuth.getInstance().setCustomUserClaims(uid, claims)
}

private fun getUserClaims(uid: String) : Set<*> {
    val user = FirebaseAuth.getInstance().getUser(uid)
    return user.customClaims.entries
}