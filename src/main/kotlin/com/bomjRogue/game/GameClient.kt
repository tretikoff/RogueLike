package com.bomjRogue.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Pools
import com.bomjRogue.*
import com.bomjRogue.character.GameCharacter
import com.bomjRogue.config.ConfigManager
import com.bomjRogue.config.Utils.swordHitSoundName
import com.bomjRogue.game.command.DeathCommand
import com.bomjRogue.game.command.HitCommand
import com.bomjRogue.game.command.MoveCommand
import com.bomjRogue.world.Position
import com.bomjRogue.world.Wall
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.graphics.use
import java.util.*

class GameClient(private val gameHost: String, private val gamePort: Int) : KtxApplicationAdapter {
    private lateinit var renderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch
    private var textLayout = Pools.obtain(GlyphLayout::class.java)!!
    private lateinit var firstPlayer: GameCharacter
    private var secondPlayer: GameCharacter? = null
    private val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()

    private val assetsManager = AssetsManager()
    private val musicManager = MusicManger()
    private var gameItems = mutableMapOf<String, Position>()

    private val client = HttpClient(CIO) {
        install(JsonFeature)
        install(WebSockets)
        defaultRequest {
            host = gameHost
            port = gamePort
        }
    }

    private fun exit() {
        runBlocking {
            try {
                client.post<GameCharacter>("/disconnect") {
                    parameter("player", firstPlayer.name)
                    accept(ContentType.Any)
                }
            } catch (e: NoTransformationFoundException) {
                //https://stackoverflow.com/questions/65105118/no-transformation-found-class-io-ktor-utils-io-bytechannelnative-error-using
            }
        }
    }

    private suspend fun join(): GameCharacter {
        return client.get("/join") {
            accept(ContentType.Any)
            parameter("name", UUID.randomUUID().toString())
        }
    }

    private suspend fun receive() {
        client.webSocket("/items") {
            while (true) {
                val frame = incoming.receive()
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val updType = object : TypeToken<Update>() {}.type
                    val upd: Update = gson.fromJson(text, updType)
                    when (upd.type) {
                        UpdateType.ItemsUpdate -> {
                            val update: MapUpdate = gson.fromJson(text, object : TypeToken<MapUpdate>() {}.type)
                            gameItems = update.items
                        }
                        UpdateType.PlayerUpdate -> {
                            val update: PlayerUpdate = gson.fromJson(text, object : TypeToken<PlayerUpdate>() {}.type)
                            if (update.player.myName == firstPlayer.name) {
                                firstPlayer = update.player
                            }
                            if (secondPlayer != null && update.player.myName == secondPlayer!!.name) {
                                secondPlayer = update.player
                            }
                        }
                        UpdateType.MusicPlay -> {
                            val update: MusicUpdate = gson.fromJson(text, object : TypeToken<MusicUpdate>() {}.type)
                            musicManager.play(update.soundName)
                        }
                    }
                }
            }
        }
    }

    override fun dispose() {
        exit()
        super.dispose()
    }

    override fun create() {
        renderer = ShapeRenderer()
        spriteBatch = SpriteBatch()
        runBlocking {
            firstPlayer = join()
        }
        assetsManager.loadAssets()
        musicManager.loadMusic()
        GlobalScope.async {
            receive()
        }
    }

    override fun render() {
        handleInput()
        update()
    }

    private fun hit(player: GameCharacter) {
        runBlocking {
            val request = HitCommand(player.name)
            musicManager.play(swordHitSoundName)
            try {
                client.post<MoveCommand>("/hit") {
                    body = request
                    contentType(ContentType.Application.Json)
                }
            } catch (e: NoTransformationFoundException) {
                //https://stackoverflow.com/questions/65105118/no-transformation-found-class-io-ktor-utils-io-bytechannelnative-error-using
            }
        }
    }

    private fun deathRequest(player: GameCharacter) {
        runBlocking {
            val request = DeathCommand(player.name)
            try {
                client.post<DeathCommand>("/respawn") {
                    body = request
                    contentType(ContentType.Application.Json)
                }
            } catch (e: NoTransformationFoundException) {
                //https://stackoverflow.com/questions/65105118/no-transformation-found-class-io-ktor-utils-io-bytechannelnative-error-using
            }
        }
    }

    private fun makeMove(player: GameCharacter, x: Float, y: Float) {
        runBlocking {
            val request = MoveCommand(player.name, x, y)
            try {
                client.post<MoveCommand>("/move") {
                    body = request
                    contentType(ContentType.Application.Json)
                }
            } catch (e: NoTransformationFoundException) {
                //https://stackoverflow.com/questions/65105118/no-transformation-found-class-io-ktor-utils-io-bytechannelnative-error-using
            }
        }
    }

    private fun handleInput() {
        if (!this::firstPlayer.isInitialized) return
        if (firstPlayer.isDead()) {
            deathRequest(firstPlayer)
            return
        }
        if (secondPlayer != null && firstPlayer.isDead()) {
            deathRequest(secondPlayer!!)
            return
        }

        val moved = movePlayer(firstPlayer, MovePattern(up=Input.Keys.W, down=Input.Keys.S, left=Input.Keys.A, right=Input.Keys.D, hit=Input.Keys.F))
        val secondPattern = MovePattern(up=Input.Keys.UP, down=Input.Keys.DOWN, left=Input.Keys.LEFT, right=Input.Keys.RIGHT, hit=Input.Keys.ENTER)
        if (secondPlayer != null) {
            movePlayer(secondPlayer!!, secondPattern)
        } else if (!moved){
            movePlayer(firstPlayer, secondPattern)
        }
        when {
            Gdx.input.isKeyPressed(Input.Keys.F2) -> {
                musicManager.increaseVolume()
            }
            Gdx.input.isKeyPressed(Input.Keys.F3) -> {
                musicManager.decreaseVolume()
            }
            Gdx.input.isKeyJustPressed(Input.Keys.F10) -> {
                joinSecondPlayer()
            }
        }
    }

    data class MovePattern(val up: Int, val down: Int, val left: Int, val right: Int, val hit: Int)

    private fun movePlayer(player: GameCharacter, move: MovePattern): Boolean {
        val step = 3f
        val x = when {
            Gdx.input.isKeyPressed(move.left) -> -step
            Gdx.input.isKeyPressed(move.right) -> +step
            else -> 0f
        }
        val y = when {
            Gdx.input.isKeyPressed(move.up) -> +step
            Gdx.input.isKeyPressed(move.down) -> -step
            else -> 0f
        }
        val moved = x != 0f || y != 0f
        if (moved) {
            makeMove(player, x, y)
        }
        if (Gdx.input.isKeyJustPressed(move.hit)) {
            hit(player)
        }
        return moved
    }

    private fun joinSecondPlayer() {
        if (secondPlayer == null) {
            GlobalScope.async {
                secondPlayer = join()
            }
        }
    }

    private fun update() {
        clearScreen(0f, 0f, 0f, 0f)
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.GRAY
            renderer.rect(0f, 0f, ConfigManager.width, ConfigManager.height)
        }

        for (obj in gameItems) {
            val item = obj.key
            val position = obj.value
            render(item, position)
        }
        drawHealth(firstPlayer)
        drawHealth(secondPlayer, 1)
        drawInfo()
    }

    private fun render(obj: String, pos: Position) {
        val (x, y) = pos.coordinates
        val (h, w) = pos.size
        val name = obj.split("_")[0]
        if (name == Wall::class.qualifiedName) {
            renderer.use(ShapeRenderer.ShapeType.Filled) {
                renderer.color = Color.BLACK
                renderer.rect(x, y, w, h)
            }
        } else {
            spriteBatch.begin()
            spriteBatch.draw(assetsManager.getSprite(name), x, y, w, h)
            spriteBatch.end()
        }
    }

    private fun drawInfo(eventInfo: String? = null) {
        spriteBatch.begin()
        val font = BitmapFont()
        val toDraw =
            eventInfo ?: "ASDW or arrow keys to move, ENTER or F to hit\nF2 to decrease and F3 to increase music volume\nF10 to add second player"
        textLayout.setText(
            font,
            toDraw
        )
        font.draw(spriteBatch, textLayout, 950f, 710f)
        spriteBatch.end()
    }

    private fun drawHealth(player: GameCharacter?, bias: Int = 0) {
        if (player == null) {
            return
        }
        spriteBatch.begin()
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.RED
            val health = 28f * player.getHealth() / 10
            val damaged = 28f * (100 - player.getHealth()) / 10
            renderer.rect(bias * 300f + 20f, 680f, health, 20f)
            renderer.color = Color.DARK_GRAY
            renderer.rect(bias * 300f + 20f + health, 680f, damaged, 20f)
        }
        spriteBatch.end()
    }
}