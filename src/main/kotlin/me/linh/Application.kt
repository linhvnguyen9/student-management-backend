package me.linh

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.linh.domain.User
import java.io.FileInputStream

fun main() {
    initFirebase()

    println(FirebaseDatabase.getInstance().reference.child("chats").child("MlmPryFEX5N0TrYjZ14xOexygtM2").addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            println("========================================================")
            println(snapshot)
            println("========================================================")
        }

        override fun onCancelled(error: DatabaseError?) {
            println("db error")
        }
    }))

    println(FirebaseDatabase.getInstance().reference.child("chats").child("").addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            println("========================================================")
            println(snapshot)
            println("========================================================")
        }

        override fun onCancelled(error: DatabaseError?) {
            println("db error")
        }
    }))

    embeddedServer(Netty, port = 8000) {
        install(ContentNegotiation) {
            gson()
        }
        install(CallLogging)

        routing {
            get("/") {
                call.respondText("Hello, world!")
            }
            route("/admin") {
                post("/deregister/{uid}") {
                    val uid = call.parameters["uid"]
                    println(uid)
                    try {
                        deregisterAdmin(uid ?: "")
                        call.respondText { "Okay" }
                    } catch (e: Exception) {
                        println(e.printStackTrace())
                        call.response.status(HttpStatusCode.BadRequest)
                        call.respondText { "Invalid uid" }
                    }
                }
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
            route("/users") {
                get("/") {
                    val users = getAllUsers()
                    println(users)
                    call.respond(status = HttpStatusCode.OK, users)
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

private fun deregisterAdmin(uid: String) {
    val claims: MutableMap<String, Any> = HashMap()
    claims["admin"] = false
    FirebaseAuth.getInstance().setCustomUserClaims(uid, claims)
}

private fun getUserClaims(uid: String) : Set<*> {
    val user = FirebaseAuth.getInstance().getUser(uid)
    return user.customClaims.entries
}

private fun getAllUsers(): List<User> {
    val users = mutableListOf<User>()
    var page = FirebaseAuth.getInstance().listUsers(null)
    while (page != null) {
        val parsedUser =
            page.values.map { User(it.uid, it.email, it.photoUrl, it.customClaims["admin"] as? Boolean ?: false) }
        users.addAll(parsedUser)
        page = page.nextPage
    }
    return users
}