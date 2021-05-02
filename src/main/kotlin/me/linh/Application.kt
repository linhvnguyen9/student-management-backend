package me.linh

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.application.*
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
        }
    }.start(wait = true)
}

private fun initFirebase() {
    val serviceAccount = FileInputStream("path/to/serviceAccountKey.json")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://student-management-5792a-default-rtdb.asia-southeast1.firebasedatabase.app")
        .build()

    FirebaseApp.initializeApp(options)
}