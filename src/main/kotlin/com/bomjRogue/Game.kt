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
import kotlin.random.Random


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

    private lateinit var music: Sound
    private var volume = 0.0f
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
    private var npcCount = 5
    private val map = LevelGenerator.generateMap()
    private var npcs = mutableListOf<Npc>()
    private var items = mutableListOf<Item>()

    override fun create() {
        loadAssets()
        initialize()
    }

    private inline fun <reified T : Any> load(path: String): T {
        manager.load<T>(path).finishLoading()
        return manager.getAsset(path)
    }

    private fun loadAssets() {
        playerSprite = load("player.png")
        playerSprite = load("player.png")
        npcSprite = load("SteamMan.png")
        swordSprite = load("sword.png")
        healthSprite = load("health.png")
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
        map.reset()
        initializeNpcs()
        initializeItems()
        mainPlayer.reset()
        map.add(mainPlayer, Position(Coordinates(5f, 5f), Size(19f, 34f)))
    }

    override fun render() {
        handleInput()
        try {
            logic()
            draw()
            // TODO stop rerender when initializing
        } catch (e: ConcurrentModificationException) {
            return
        }
    }

    private fun initializeItems() {
        items.clear()
        val weapon = Weapon("Slayer of dragons", 5)
        val health = Health(50)
        items.add(weapon)
        items.add(health)
        map.addRandomPlace(weapon, Size(40f, 40f))
        map.addRandomPlace(health, Size(25f, 25f))
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
        npcs.forEach { map.addRandomPlace(it, Size(20f, 36f)) }
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
        } else if (Gdx.input.isKeyPressed(Input.Keys.F2)) {
            volume = max(volume - 0.05f, 0f)
        } else if (Gdx.input.isKeyPressed(Input.Keys.F3)) {
            volume = min(volume + 0.05f, 1f)
        }
        music.setVolume(0, volume)
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
            npcCount++
            initializeNpcs()
            initializeItems()
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
        drawWalls()

        drawHealth()
        spriteBatch.begin()
        drawInfo()
        for (npc in npcs) {
            renderNpc(npc)
        }
        for (item in items) {
            renderItem(item)
        }
        val position = map.getPosition(mainPlayer)
        val (x, y) = position.coordinates
        val (h, w) = position.size
        spriteBatch.draw(playerSprite, x, y, h, w)
        spriteBatch.end()
    }

    private fun drawWalls() {
        for (wall in map.walls) {
            renderer.use(ShapeRenderer.ShapeType.Filled) {
                renderer.color = Color.BLACK
                val (x, y) = wall.value.coordinates
                val (h, w) = wall.value.size
                renderer.rect(x, y, w, h)
            }
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
        val position = map.getPosition(npc)
        val (x, y) = position.coordinates
        val (h, w) = position.size
        spriteBatch.draw(npcSprite, x, y, h, w)
    }

    private fun renderItem(item: Item) {
        val position = map.getPosition(item)
        val (x, y) = position.coordinates
        val (h, w) = position.size
        if (item is Health) {
            spriteBatch.draw(healthSprite, x, y, w, h)
        } else if (item is Weapon) {
            spriteBatch.draw(swordSprite, x, y, w, h)
        }
    }
}