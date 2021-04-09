package com.bomjRogue

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken


fun main() {
    val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()
    embeddedServer(Netty, host = "localhost", port = 8080) {
        val game = GameServer()

        install(WebSockets)
        install(ContentNegotiation) {
            json()
        }
        game.initialize()
        GlobalScope.async {
            game.run()
        }
        routing {
            webSocket("/items") {
                while (true) {
                    val data = gson.toJson(game.getGameItems())
                    send(Frame.Text(TextContent(data, ContentType.Any).text))
                    delay(100)
                }
            }
            post("/move") {
                try {
//                    val text = call.receiveText().replace("\"", "")
//
//                    val data: GameClient.MoveRequest = gson.fromJson(
//                        text, object : TypeToken<GameClient.MoveRequest>() {}.type
//                    )
                    val data = call.receive<GameClient.MoveRequest>()
                    game.makeMove(data.playerName, data.x, data.y)
                } catch (e: Exception) {
                    println(e)
                }
                call.respond(HttpStatusCode.Accepted)
            }
            get("/join") {
                val name = call.request.queryParameters["name"]
                if (name != null) {
                    println(name)
                    try {
                        call.respond(game.join(name))
                    } catch (e: Exception) {
                        println(e)
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Could not join the game")
                }
            }
        }
    }.start(wait = true)
}


fun Application.module() {

    routing {
        get("hello") {
            call.respond("world")
        }
        get {
            call.respondText("world default")
        }
    }
}