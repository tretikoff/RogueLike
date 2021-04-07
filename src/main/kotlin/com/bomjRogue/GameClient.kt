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
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.getAsset
import ktx.assets.load
import ktx.graphics.use
import kotlin.math.max
import kotlin.math.min

class GameClient(private val server: GameServer) : KtxApplicationAdapter {
    private lateinit var renderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch
    private var textLayout = Pools.obtain(GlyphLayout::class.java)!!
    val player = Player(
        "Player", Characteristics(
            mutableMapOf(
                CharacteristicType.Health to 100,
                CharacteristicType.Armor to 10,
                CharacteristicType.Force to 20
            )
        ), ObjectType.Player
    )

    private val manager = AssetManager()
    private val sprites = mutableMapOf<ObjectType, Texture>()

    private lateinit var music: Sound
    private var volume = 0.0f
    private lateinit var hitBodySound: Sound
    private lateinit var swordGetSound: Sound
    private lateinit var hitSound: Sound

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

    private fun initialize(reset: Boolean = true) {
        renderer = ShapeRenderer()
        spriteBatch = SpriteBatch()
        server.join(player)
    }

    override fun render() {
        handleInput()
        try {
            draw()
            // TODO stop rerender when initializing
        } catch (e: ConcurrentModificationException) {
            return
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
            server.makeMove(player, x, y)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            server.makeDamage(player)
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
        for (obj in server.getGameItems()) {
            render(obj.key, obj.value)
        }
        spriteBatch.end()
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
            spriteBatch.draw(sprites[obj.type], x, y, w, h)
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
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.RED
            val health = 28f * player.getHealth() / 10
            val damaged = 28f * (100 - player.getHealth()) / 10
            renderer.rect(20f, 680f, health, 20f)
            renderer.color = Color.DARK_GRAY
            renderer.rect(20f + health, 680f, damaged, 20f)
        }
    }
}