package com.bomjRogue

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Pools
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.getAsset
import ktx.assets.load
import ktx.graphics.use
import java.util.*
import kotlin.math.max
import kotlin.math.min
import com.badlogic.gdx.net.HttpRequestBuilder.json


class GameClient : KtxApplicationAdapter {
    private lateinit var renderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch
    private var textLayout = Pools.obtain(GlyphLayout::class.java)!!
    private val playerName = UUID.randomUUID().toString()
    private var player: Character? = null
    private val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()

    private val manager = AssetManager()
    private var gameItems = mutableMapOf<GameObject, Position>()
    private val sprites = mutableMapOf<ObjectType, Texture>()

    private lateinit var music: Sound
    private var volume = 0.0f
    private lateinit var hitBodySound: Sound
    private lateinit var swordGetSound: Sound
    private lateinit var hitSound: Sound
    private val client = HttpClient(CIO) {
        url { host = "127.0.0.1"; port = 8080 }
        install(JsonFeature)
        install(WebSockets)
    }

    private suspend fun join() {
        player = client.get<Character>("http://localhost:8080/join") {
            accept(ContentType.Any)
            parameter("name", playerName)
        }
    }

    suspend fun receive() {
//        client.ws(host = "127.0.0.1", port = 8080, path = "/items") {
        client.webSocket(method = HttpMethod.Get, host = "localhost", port = 8080, path = "/items") {
            println("starting to connect")
            while (true) {
                val frame = incoming.receive()
                try {
                    if (frame is Frame.Text) gameItems =
                        gson.fromJson(
                            frame.readText(), object : TypeToken<MutableMap<GameObject, Position>>() {}.type
                        )
                } catch (e: Exception) {
                    println(e)

                }
            }
        }
    }

    override fun create() {
        loadAssets()
        initialize()
    }

    private inline fun <reified T : Any> load(path: String): T {
        manager.load<T>(path).finishLoading()
        return manager.getAsset(path)
    }

    private fun loadAssets() {
        sprites[ObjectType.Player] = load("player.png")
        sprites[ObjectType.ExitDoor] = load("door.png")
        sprites[ObjectType.Npc] = load("SteamMan.png")
        sprites[ObjectType.Sword] = load("sword.png")
        sprites[ObjectType.Health] = load("health.png")
// Replace if your PC is strong enough
//        load<Sound>("sound.mp3").loop()
        music = load("sound_light.mp3")
        hitBodySound = load("hit_body.mp3")
        swordGetSound = load("sword_get.mp3")
        hitSound = load("hit.mp3")
        music.loop()
    }

    private fun initialize() {
        renderer = ShapeRenderer()
        spriteBatch = SpriteBatch()
        runBlocking {
            join()
        }
        GlobalScope.async {
            receive()
        }
    }

    override fun render() {
        handleInput()
        draw()
    }

    @Serializable
    class MoveRequest(val playerName: String, val x: Float, val y: Float)

    private fun makeMove(x: Float, y: Float) {
        runBlocking {
            val request = MoveRequest(playerName, x, y)
            client.post<MoveRequest>(port = 8080, path = "/move") {
                body = request
            }
        }
    }

    private fun handleInput() {
        val step = 3f
        val x = when {
            Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT) -> -step
            Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> +step
            else -> 0f
        }
        val y = when {
            Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP) -> +step
            Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN) -> -step
            else -> 0f
        }
        if (x != 0f || y != 0f) {
            makeMove(x, y)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
//            if (server.makeDamage(player)) hitBodySound.play() else hitSound.play()
        } else if (Gdx.input.isKeyPressed(Input.Keys.F2)) {
            volume = max(volume - 0.05f, 0f)
        } else if (Gdx.input.isKeyPressed(Input.Keys.F3)) {
            volume = min(volume + 0.05f, 1f)
        }
        music.setVolume(0, volume)
    }

    private fun draw() {
        clearScreen(0f, 0f, 0f, 0f)
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.GRAY
            renderer.rect(0f, 0f, 1280f, 720f)
        }

        spriteBatch.begin()
        drawHealth()
        drawInfo()
        spriteBatch.end()
        for (obj in gameItems) {
            render(obj.key, obj.value)
        }
    }

    private fun render(obj: GameObject, pos: Position) {
        val (x, y) = pos.coordinates
        val (h, w) = pos.size
        if (obj.type == ObjectType.Wall) {
            renderer.use(ShapeRenderer.ShapeType.Filled) {
                renderer.color = Color.BLACK
                renderer.rect(x, y, w, h)
            }
        } else {
            spriteBatch.begin()
            spriteBatch.draw(sprites[obj.type], x, y, w, h)
            spriteBatch.end()
        }
    }

    private fun drawInfo() {
        val font = BitmapFont()
        textLayout.setText(
            font,
            "ASDW or arrow keys to move, ENTER or F to hit\nF2 to decrease and F3 to increase music volume"
        )
        font.draw(spriteBatch, textLayout, 950f, 710f)
    }

    private fun drawHealth() {
        if (player == null) return
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.RED
            val health = 28f * player!!.getHealth() / 10
            val damaged = 28f * (100 - player!!.getHealth()) / 10
            renderer.rect(20f, 680f, health, 20f)
            renderer.color = Color.DARK_GRAY
            renderer.rect(20f + health, 680f, damaged, 20f)
        }
    }
}