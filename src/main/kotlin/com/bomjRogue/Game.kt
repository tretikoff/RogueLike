package com.bomjRogue

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.assets.getAsset
import ktx.assets.load
import ktx.graphics.use


data class Coordinates(val xCoordinate: Float, val yCoordinate: Float)
enum class CharacteristicType {
    Health,
    Force,
    Armor
}
typealias CharacteristicsMap = MutableMap<CharacteristicType, Int>

class Characteristics(private val defaults: CharacteristicsMap) {
    private var characteristics: MutableMap<CharacteristicType, Int> = defaults
    fun reset() {
        characteristics = defaults
    }

    fun updateCharacteristic(type: CharacteristicType, value: Int) {
        characteristics[type] = characteristics[type]!! + value
    }

    fun getCharacteristic(type: CharacteristicType): Int {
        return characteristics[type]!!
    }
}

abstract class Item(val name: String) {

}

class Weapon(name: String, val damage: Int) : Item(name)


interface GameObject {
    fun update()
}

class Game : KtxApplicationAdapter {
    private lateinit var renderer: ShapeRenderer
    private val manager = AssetManager()
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var playerSprite: Texture
    private lateinit var npcSprite: Texture
    private val mainPlayer = Player(
        "player", Characteristics(
            mutableMapOf(
                CharacteristicType.Health to 100,
                CharacteristicType.Armor to 10,
                CharacteristicType.Force to 20
            )
        )
    )
    private val npcCount = 5
    private val map = Map()
    private var npcs = mutableListOf<Player>()

    override fun create() {
        manager.load<Texture>("player.png").finishLoading()
        manager.load<Texture>("SteamMan.png").finishLoading()
        playerSprite = manager.getAsset("player.png")
        npcSprite = manager.getAsset("SteamMan.png")

        mainPlayer.reset()
        map.add(mainPlayer, Coordinates(5f, 5f))
        renderer = ShapeRenderer()
        spriteBatch = SpriteBatch()
        initializeNpcs()
//        texture.asset
//        assets[MusicAssets.Rain].apply { isLooping = true }.play()
    }

    override fun render() {
        handleInput()
        logic()
        draw()
    }

    private fun initializeNpcs() {
        for (i in 0 until npcCount) {
            // TODO randomize npc
            val npc = Player("", Characteristics(
                mutableMapOf(
                    CharacteristicType.Health to 100,
                    CharacteristicType.Armor to 10,
                    CharacteristicType.Force to 20
                )))
            npcs.add(npc)
            map.add(npc, Coordinates((40..1200).random().toFloat(), (40..700).random().toFloat()))
        }
    }

    private fun handleInput() {
        val x = when {
            Gdx.input.isKeyPressed(Input.Keys.A) -> -5f
            Gdx.input.isKeyPressed(Input.Keys.LEFT) -> -5f
            Gdx.input.isKeyPressed(Input.Keys.D) -> +5f
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) -> +5f
            else -> 0f
        }
        val y = when {
            Gdx.input.isKeyPressed(Input.Keys.W) -> +5f
            Gdx.input.isKeyPressed(Input.Keys.UP) -> +5f
            Gdx.input.isKeyPressed(Input.Keys.S) -> -5f
            Gdx.input.isKeyPressed(Input.Keys.DOWN) -> -5f
            else -> 0f
        }
        map.move(mainPlayer, x, y)
        if (Gdx.input.isKeyPressed(Input.Keys.F) || Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            makeDamage(mainPlayer)
        }
    }

    private fun makeDamage(hitman: Player) {
        for (pl in npcs + mainPlayer) {
            if (pl != hitman && map.objectsConnect(hitman, pl)) {
                pl.takeDamage(hitman.getForce())
                if (pl.getHealth() < 0) {
                    map.remove(pl)
                    npcs.remove(pl)
                }
                if (pl == mainPlayer) {
                    // TODO GAME OVER
                }
            }
        }
    }

    private fun logic() {
        // TODO npc's move
    }


    private fun draw() {
        clearScreen(0f, 0f, 0f, 0f)
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.GRAY
                renderer.rect(0f, 0f, 1280f, 720f)
        }

        spriteBatch.begin()
        for (npc in npcs) {
            renderNpc(npc)
        }
        val coordinates = map.getPosition(mainPlayer)
        spriteBatch.draw(playerSprite, coordinates.xCoordinate, coordinates.yCoordinate)
        spriteBatch.end()
    }

    private fun renderNpc(npc: Player) {
        val coordinates = map.getPosition(npc)
        spriteBatch.draw(npcSprite, coordinates.xCoordinate, coordinates.yCoordinate)
    }
}