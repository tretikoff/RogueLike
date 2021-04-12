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
import kotlinx.serialization.Serializable

enum class UpdateType {
    ItemsUpdate,
    PlayerUpdate,
//    MusicPlay,
}

@Serializable
open class Update(val type: UpdateType)

data class MapUpdate(val items: GameItems) : Update(UpdateType.ItemsUpdate)
data class PlayerUpdate(val player: Player) : Update(UpdateType.PlayerUpdate)

fun main() {
    val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()
    embeddedServer(Netty, host = "localhost", port = 8080) {
        val game = Game()

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
                    val data = gson.toJson(MapUpdate(game.getGameItems()))
                    send(Frame.Text(TextContent(data, ContentType.Any).text))
                    for (update in game.getUpdates()) {
                    send(Frame.Text(TextContent(data, ContentType.Any).text))
                        send(Frame.Text(TextContent(gson.toJson(update), ContentType.Any).text))
                    }
                    delay(100)
                }
            }
            post("/move") {
                val data = call.receive<MoveCommand>()
                game.makeMove(data.playerName, data.x, data.y)
                call.respond(HttpStatusCode.Accepted)
            }
            post("/hit") {
                val data = call.receive<HitCommand>()
                game.hit(data.playerName)
                call.respond(HttpStatusCode.Accepted)
            }
            get("/join") {
                try {

                val name = call.request.queryParameters["name"]
                if (name != null) {
                    call.respond(HttpStatusCode.Accepted, game.join(name))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Could not join the game")
                }
                }catch (e: Exception) {
                    println(e)
                }
            }
        }
    }.start(wait = true)
}