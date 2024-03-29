package me.linh

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.linh.data.AnnouncementLocal
import me.linh.data.Announcements
import me.linh.domain.Announcement
import me.linh.domain.User
import me.linh.remote.AddAnnouncementRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileInputStream
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

fun main() {
    initFirebase()
    initDb()

    embeddedServer(Netty, port = 8080) {
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
            route("/announcements") {
                get("") {
                    val announcements = getAllAnnouncements()
                    call.respond(announcements.map { it.toDomain() })
                }
                post("") {
                    val announcementRequest = call.receive<AddAnnouncementRequest>()
                    saveAnnouncement(announcementRequest.toAnnouncement())
                }
            }
        }
    }.start(wait = true)
}

private fun initFirebase() {
    val serviceAccount =
        FileInputStream(System.getProperty("user.dir") + "/" + "src/main/resources/student-management-5792a-firebase-adminsdk-pgu7w-7e07c84619.json")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://student-management-5792a-default-rtdb.asia-southeast1.firebasedatabase.app")
        .build()

    FirebaseApp.initializeApp(options)
}

private fun initDb() {
    val db = Database.connect(
        "jdbc:mysql://localhost:3306/${System.getenv("DB_NAME")}", driver = "com.mysql.jdbc.Driver",
        user = System.getenv("DB_USER"), password = System.getenv("DB_PASSWORD")
    )

    transaction {
        SchemaUtils.create(Announcements)
    }
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

private fun getUserClaims(uid: String): Set<*> {
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

private fun getAllAnnouncements(): List<AnnouncementLocal> =
    transaction {
        addLogger(StdOutSqlLogger)

        AnnouncementLocal.all().toList()
    }

private fun saveAnnouncement(announcement: Announcement) {
    transaction {
        addLogger(StdOutSqlLogger)

        AnnouncementLocal.new {
            imageUrl = announcement.imageUrl
            title = announcement.title
            content = announcement.content
            timestamp = LocalDateTime.ofInstant(Calendar.getInstance().apply { timeInMillis = announcement.timestamp }.toInstant(), Calendar.getInstance().timeZone.toZoneId())
        }
    }
}