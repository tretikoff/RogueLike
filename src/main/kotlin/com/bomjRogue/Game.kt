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
import kotlin.random.Random


data class Coordinates(val xCoordinate: Float, val yCoordinate: Float)



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
    private var npcs = mutableListOf<Npc>()

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
            val npc = Npc(
                "", Characteristics(
                    mutableMapOf(
                        CharacteristicType.Health to 100,
                        CharacteristicType.Armor to 10,
                        CharacteristicType.Force to 20
                    )
                )
            )
            npcs.add(npc)
            map.add(npc, Coordinates((40..1200).random().toFloat(), (40..700).random().toFloat()))
        }
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
    }


    private fun draw() {
        clearScreen(0f, 0f, 0f, 0f)
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.GRAY
            renderer.rect(0f, 0f, 1280f, 720f)
        }

        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.RED
            val health = 28f * mainPlayer.getHealth() / 10
            val damaged = 28f * (100 - mainPlayer.getHealth()) / 10
            renderer.rect(20f, 680f, health, 20f)
            renderer.color = Color.DARK_GRAY
            renderer.rect(20f + health, 680f, damaged, 20f)
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