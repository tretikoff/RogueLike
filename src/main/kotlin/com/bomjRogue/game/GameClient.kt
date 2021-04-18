package com.bomjRogue.game

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
import com.bomjRogue.*
import com.bomjRogue.character.Character
import com.bomjRogue.config.Utils.Companion.fleshHitSoundName
import com.bomjRogue.config.Utils.Companion.itemPickUpSoundName
import com.bomjRogue.config.Utils.Companion.swordHitSoundName
import com.bomjRogue.game.command.DeathCommand
import com.bomjRogue.game.command.HitCommand
import com.bomjRogue.game.command.MoveCommand
import com.bomjRogue.world.Position
import com.bomjRogue.world.interactive.GameObject
import com.bomjRogue.world.interactive.ObjectType
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
import ktx.assets.getAsset
import ktx.assets.load
import ktx.graphics.use
import java.util.*
import kotlin.math.max
import kotlin.math.min

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
    private var volume = 0.05f
    private lateinit var hitBodySound: Sound
    private lateinit var swordGetSound: Sound
    private lateinit var hitSound: Sound
    private lateinit var knownSounds: Map<String, Sound>
    private val client = HttpClient(CIO) {
        install(JsonFeature)
        install(WebSockets)
        defaultRequest {
            host = "localhost"
            port = 8084
        }
    }

    private fun exit() {
        runBlocking {
            try {
                client.post<Character>("/disconnect") {
                    parameter("player", playerName)
                    accept(ContentType.Any)
                }
            } catch (e: NoTransformationFoundException) {
                //https://stackoverflow.com/questions/65105118/no-transformation-found-class-io-ktor-utils-io-bytechannelnative-error-using
            }
        }
    }

    private suspend fun join() {
        player = client.get<Character>("/join") {
            accept(ContentType.Any)
            parameter("name", playerName)
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
                            if (update.player.myName == playerName) {
                                player = update.player
                            }
                        }
                        UpdateType.MusicPlay -> {
                            val update: MusicUpdate = gson.fromJson(text, object : TypeToken<MusicUpdate>() {}.type)
                            knownSounds[update.soundName]?.play()
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
        hitBodySound = load(fleshHitSoundName)
        swordGetSound = load(itemPickUpSoundName)
        hitSound = load(swordHitSoundName)
        knownSounds = mapOf(fleshHitSoundName to hitBodySound,
        itemPickUpSoundName to swordGetSound, swordHitSoundName to hitSound)
        music.loop(volume)
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
        update()
    }

    private fun hit() {
        runBlocking {
            val request = HitCommand(playerName)
            hitSound.play() // anyway
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

    private fun deathRequest() {
        runBlocking {
            val request = DeathCommand(playerName)
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

    private fun makeMove(x: Float, y: Float) {
        runBlocking {
            val request = MoveCommand(playerName, x, y)
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
        if (player!!.isDead()) {
            deathRequest()
            return
        }
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
            hit()
        } else if (Gdx.input.isKeyPressed(Input.Keys.F2)) {
            volume = max(volume - 0.05f, 0f)
        } else if (Gdx.input.isKeyPressed(Input.Keys.F3)) {
            volume = min(volume + 0.05f, 1f)
        }
        music.setVolume(0, volume)
    }

    private fun update() {
        clearScreen(0f, 0f, 0f, 0f)
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.GRAY
            renderer.rect(0f, 0f, 1280f, 720f)
        }

        for (obj in gameItems) {
            val item = obj.key
            val position = obj.value
//            if (item.type == ObjectType.Player) {
//                val playerItem = item as Player
//                if (playerItem.name == playerName) {
//                    player = playerItem
//                }
//            }
            render(item, position)
        }
        drawHealth()
        drawInfo()
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

    private fun drawInfo(eventInfo: String? = null) {
        spriteBatch.begin()
        val font = BitmapFont()
        val toDraw = eventInfo ?: "ASDW or arrow keys to move, ENTER or F to hit\nF2 to decrease and F3 to increase music volume"
        textLayout.setText(
            font,
            toDraw
        )
        font.draw(spriteBatch, textLayout, 950f, 710f)
        spriteBatch.end()
    }

    private fun drawHealth() {
        spriteBatch.begin()
        if (player == null) return
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.RED
            val health = 28f * player!!.getHealth() / 10
            val damaged = 28f * (100 - player!!.getHealth()) / 10
            renderer.rect(20f, 680f, health, 20f)
            renderer.color = Color.DARK_GRAY
            renderer.rect(20f + health, 680f, damaged, 20f)
        }
        spriteBatch.end()
    }
}