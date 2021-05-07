package com.bomjRogue.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.bomjRogue.config.Utils.fleshHitSoundName
import com.bomjRogue.config.Utils.healthPickUpSoundName
import com.bomjRogue.config.Utils.itemPickUpSoundName
import com.bomjRogue.config.Utils.swordHitSoundName
import ktx.assets.getAsset
import ktx.assets.load
import kotlin.math.max
import kotlin.math.min

class MusicManger {
    private val manager = AssetManager()

    private lateinit var music: Sound
    private var step = 0.05f
    private var volume = step
    private lateinit var knownSounds: Map<String, Sound>

    private inline fun <reified T : Any> load(path: String): T {
        manager.load<T>(path).finishLoading()
        return manager.getAsset(path)
    }

    fun loadMusic() {
// Replace if your PC is strong enough
//        load<Sound>("sound.mp3").loop()
        music = load("sound_light.mp3")
        knownSounds = mapOf(
            fleshHitSoundName to load(fleshHitSoundName),
            itemPickUpSoundName to load(itemPickUpSoundName),
            swordHitSoundName to load(swordHitSoundName),
            healthPickUpSoundName to load(healthPickUpSoundName)
        )
        music.loop(volume)
    }

    fun play(soundName: String) {
        knownSounds[soundName]?.play()
    }

    fun increaseVolume() {
        volume = min(volume + step, 1f)
        music.setVolume(0, volume)
    }

    fun decreaseVolume() {
        volume = max(volume - step, 0f)
        music.setVolume(0, volume)
    }
}