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
import com.bomjRogue.character.GameCharacter
import com.bomjRogue.config.Utils.Companion.fleshHitSoundName
import com.bomjRogue.config.Utils.Companion.healthPickUpSoundName
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
    private var firstPlayer: GameCharacter? = null
    private var secondPlayer: GameCharacter? = null
    private val gson = GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create()

    private val manager = AssetManager()
    private var gameItems = mutableMapOf<GameObject, Position>()
    private val sprites = mutableMapOf<ObjectType, Texture>()


    private lateinit var music: Sound
    private var volume = 0.05f
    private lateinit var hitBodySound: Sound
    private lateinit var swordGetSound: Sound
    private lateinit var hitSound: Sound
    private lateinit var healthSound: Sound
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
                client.post<GameCharacter>("/disconnect") {
                    parameter("player", firstPlayer!!.name)
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
                            if (update.player.myName == firstPlayer!!.name) {
                                firstPlayer = update.player
                            }
                            if (secondPlayer != null && update.player.myName == secondPlayer!!.name) {
                                secondPlayer = update.player
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
        sprites[ObjectType.AggressiveNpc] = load("aggressive.png")
        sprites[ObjectType.CowardNpc] = load("woodcutter.png")
        sprites[ObjectType.Sword] = load("sword.png")
        sprites[ObjectType.Health] = load("health.png")
// Replace if your PC is strong enough
//        load<Sound>("sound.mp3").loop()
        music = load("sound_light.mp3")
        hitBodySound = load(fleshHitSoundName)
        swordGetSound = load(itemPickUpSoundName)
        hitSound = load(swordHitSoundName)
        healthSound = load(healthPickUpSoundName)
        knownSounds = mapOf(
            fleshHitSoundName to hitBodySound,
            itemPickUpSoundName to swordGetSound, swordHitSoundName to hitSound, healthPickUpSoundName to healthSound
        )
        music.loop(volume)
    }

    private fun initialize() {
        renderer = ShapeRenderer()
        spriteBatch = SpriteBatch()
        runBlocking {
            firstPlayer = join()
        }
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
        if (firstPlayer != null && firstPlayer!!.isDead()) {
            deathRequest(firstPlayer!!)
            return
        }
        if (secondPlayer != null && firstPlayer!!.isDead()) {
            deathRequest(secondPlayer!!)
            return
        }
        val step = 3f
        var second = false
        val x = when {
            Gdx.input.isKeyPressed(Input.Keys.A) -> -step
            Gdx.input.isKeyPressed(Input.Keys.LEFT) -> {
                second = true
                -step
            }
            Gdx.input.isKeyPressed(Input.Keys.D) -> +step
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> {
                second = true
                +step
            }
            else -> 0f
        }
        val y = when {
            Gdx.input.isKeyPressed(Input.Keys.W) -> +step
            Gdx.input.isKeyPressed(Input.Keys.UP) -> {
                second = true
                +step
            }
            Gdx.input.isKeyPressed(Input.Keys.S) -> -step
            Gdx.input.isKeyPressed(Input.Keys.DOWN) -> {
                second = true
                -step
            }
            else -> 0f
        }
        if (x != 0f || y != 0f) {
            if (second && secondPlayer != null) {
                makeMove(secondPlayer!!, x, y)
            } else {
                makeMove(firstPlayer!!, x, y)
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            hit(firstPlayer!!)
        } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            if (secondPlayer != null) hit(secondPlayer!!) else (hit(firstPlayer!!))
        } else if (Gdx.input.isKeyPressed(Input.Keys.F2)) {
            volume = max(volume - 0.05f, 0f)
        } else if (Gdx.input.isKeyPressed(Input.Keys.F3)) {
            volume = min(volume + 0.05f, 1f)
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.F10)) {
            joinSecondPlayer()
        }
        music.setVolume(0, volume)
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
            renderer.rect(0f, 0f, 1280f, 720f)
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
        val toDraw =
            eventInfo ?: "ASDW or arrow keys to move, ENTER or F to hit\nF2 to decrease and F3 to increase music volume"
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