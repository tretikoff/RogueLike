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
import kotlin.random.Random


class Coordinates(val xCoordinate: Float, val yCoordinate: Float) {
    fun valid(): Boolean {
        return xCoordinate > 0 && yCoordinate > 0 && xCoordinate < 1280 && yCoordinate < 650
    }
}

interface GameObject {
    fun update()
}

class Game : KtxApplicationAdapter {
    private lateinit var renderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch
    private var textLayout = Pools.obtain(GlyphLayout::class.java)!!

    private val manager = AssetManager()
    private lateinit var playerSprite: Texture
    private lateinit var npcSprite: Texture
    private lateinit var swordSprite: Texture
    private lateinit var healthSprite: Texture

    // TODO change sound on f keys
    private lateinit var hitBodySound: Sound
    private lateinit var swordGetSound: Sound
    private lateinit var hitSound: Sound
    private val mainPlayer = Player(
        "Player", Characteristics(
            mutableMapOf(
                CharacteristicType.Health to 100,
                CharacteristicType.Armor to 10,
                CharacteristicType.Force to 20
            )
        )
    )
    private val npcCount = 5
    private val map = Map()
    private var npcs = mutableListOf<Npc>()
    private var items = mutableListOf<Item>()

    override fun create() {
        loadAssets()
        // TODO reinitialize properly
        initialize()
    }

    private inline fun <reified T : Any>load(path: String): T {
        manager.load<T>(path).finishLoading()
        return manager.getAsset(path)
    }

    private fun loadAssets() {
        playerSprite = load("player.png")
        playerSprite = load("player.png")
        npcSprite = load("SteamMan.png")
        swordSprite = load("sword.png")
        healthSprite = load("health.png")
        load<Sound>("sound.mp3").loop()
        hitBodySound = load("hit_body.mp3")
        swordGetSound = load("sword_get.mp3")
        hitSound = load("hit.mp3")
    }

    private fun initialize() {
        map.reset()
        mainPlayer.reset()
        map.add(mainPlayer, Coordinates(5f, 5f))
        renderer = ShapeRenderer()
        spriteBatch = SpriteBatch()
        initializeNpcs()
        initializeItems()
    }

    override fun render() {
        handleInput()
        logic()
        draw()
    }

    private fun initializeItems() {
        items.clear()
        items.add(Weapon("Slayer of dragons", 5))
        items.add(Health(50))
        items.forEach { map.addRandomPlace(it) }
    }

    private fun initializeNpcs() {
        npcs.clear()
        for (i in 0 until npcCount) {
            // TODO randomize npc
            npcs.add(
                Npc(
                    "Npc_$i", Characteristics(
                        mutableMapOf(
                            CharacteristicType.Health to 100,
                            CharacteristicType.Armor to 10,
                            CharacteristicType.Force to 20
                        )
                    )
                )
            )
        }
        npcs.forEach { map.addRandomPlace(it) }
    }

    private fun handleInput() {
        val step = 3f
        val x = when {
            Gdx.input.isKeyPressed(Input.Keys.A) -> -step
            Gdx.input.isKeyPressed(Input.Keys.LEFT) -> -step
            Gdx.input.isKeyPressed(Input.Keys.D) -> +step
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> +step
            else -> 0f
        }
        val y = when {
            Gdx.input.isKeyPressed(Input.Keys.W) -> +step
            Gdx.input.isKeyPressed(Input.Keys.UP) -> +step
            Gdx.input.isKeyPressed(Input.Keys.S) -> -step
            Gdx.input.isKeyPressed(Input.Keys.DOWN) -> -step
            else -> 0f
        }
        map.move(mainPlayer, x, y)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            makeDamage(mainPlayer)
        }
    }

    private fun makeDamage(hitman: Player) {
        var noDamage = true
        for (pl in npcs + mainPlayer) {
            if (pl != hitman && map.objectsConnect(hitman, pl)) {
                noDamage = false
                hitBodySound.play()
                pl.takeDamage(hitman.getForce())
                if (pl.getHealth() < 0) {
                    map.remove(pl)
                    npcs.remove(pl)
                    if (pl == mainPlayer) {
                        initialize()
                    }
                }
            }
        }
        if (noDamage) {
            hitSound.play()
        }
    }

    private fun logic() {
        if (npcs.isEmpty()) {
            initializeNpcs()
        }
        for (npc in npcs) {
            if (map.objectsConnect(npc, mainPlayer)) {
                if (Random.nextInt(100) < 5) {
                    makeDamage(npc)
                }
            }
            if (Random.nextInt(100) < 5) {
                npc.direction = Direction.values()[Random.nextInt(4)]
            } else {
                val x = when (npc.direction) {
                    Direction.Left -> -1f
                    Direction.Right -> +1f
                    else -> 0f
                }
                val y = when (npc.direction) {
                    Direction.Down -> +1f
                    Direction.Up -> -1f
                    else -> 0f
                }
                map.move(npc, x, y)
            }
        }
        pickItems()
    }

    private fun draw() {
        clearScreen(0f, 0f, 0f, 0f)
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.GRAY
            renderer.rect(0f, 0f, 1280f, 720f)
        }
        drawHealth()
        spriteBatch.begin()
        drawInfo()
        for (npc in npcs) {
            renderNpc(npc)
        }
        for (item in items) {
            renderItem(item)
        }
        val coordinates = map.getPosition(mainPlayer)
        spriteBatch.draw(playerSprite, coordinates.xCoordinate, coordinates.yCoordinate)
        spriteBatch.end()
    }

    private fun drawInfo() {
        val font = BitmapFont()
        textLayout.setText(font, "ASDW or arrow keys to move, ENTER or F to hit")
        font.draw(spriteBatch, textLayout, 950f, 710f)
    }

    private fun drawHealth() {
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.RED
            val health = 28f * mainPlayer.getHealth() / 10
            val damaged = 28f * (100 - mainPlayer.getHealth()) / 10
            renderer.rect(20f, 680f, health, 20f)
            renderer.color = Color.DARK_GRAY
            renderer.rect(20f + health, 680f, damaged, 20f)
        }
    }

    private fun pickItems() {
        val toRemove = mutableListOf<Item>()
        for (item in items) {
            if (map.objectsConnect(item, mainPlayer)) {
                if (item is Health) {
                    mainPlayer.addHealth(item.healthPoints)
                } else if (item is Weapon) {
                    swordGetSound.play()
                    mainPlayer.addForce(item.damage)
                }
                toRemove.add(item)
            }
        }
        toRemove.forEach {
            items.remove(it)
            map.remove(it)
        }
    }

    private fun renderNpc(npc: Player) {
        val coordinates = map.getPosition(npc)
        spriteBatch.draw(npcSprite, coordinates.xCoordinate, coordinates.yCoordinate)
    }

    private fun renderItem(item: Item) {
        val coordinates = map.getPosition(item)
        // TODO store game object information, such as size and sprite, inside object itself
        if (item is Health) {
            spriteBatch.draw(healthSprite, coordinates.xCoordinate, coordinates.yCoordinate, 25f, 25f)
        } else if (item is Weapon) {
            spriteBatch.draw(swordSprite, coordinates.xCoordinate, coordinates.yCoordinate, 40f, 40f)
        }
        // TODO render health for a player
    }
}