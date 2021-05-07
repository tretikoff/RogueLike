package com.bomjRogue.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.bomjRogue.character.*
import com.bomjRogue.world.interactive.ExitDoor
import com.bomjRogue.world.interactive.Health
import com.bomjRogue.world.interactive.Sword
import ktx.assets.getAsset
import ktx.assets.load

class AssetsManager {
    private val manager = AssetManager()
    private val sprites = mutableMapOf<String?, Texture>()

    private inline fun <reified T : Any> load(path: String): T {
        manager.load<T>(path).finishLoading()
        return manager.getAsset(path)
    }

    fun loadAssets() {
        sprites[Player::class.qualifiedName] = load("player.png")
        sprites[ExitDoor::class.qualifiedName] = load("door.png")
        sprites[RandomNpc::class.qualifiedName] = load("SteamMan.png")
        sprites[AggressiveNpc::class.qualifiedName] = load("aggressive.png")
        sprites[CowardNpc::class.qualifiedName] = load("woodcutter.png")
        sprites[Sword::class.qualifiedName] = load("sword.png")
        sprites[Health::class.qualifiedName] = load("health.png")
    }

    fun getSprite(obj: String?): Texture {
        return sprites[obj]!!
    }
}