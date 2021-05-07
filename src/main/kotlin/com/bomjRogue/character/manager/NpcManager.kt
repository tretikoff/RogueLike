package com.bomjRogue.character.manager

import com.bomjRogue.character.*
import com.bomjRogue.config.SettingsManager.defaultArmor
import com.bomjRogue.config.SettingsManager.defaultForce
import com.bomjRogue.config.SettingsManager.defaultHealth
import com.bomjRogue.config.SettingsManager.defaultNpcCount
import com.bomjRogue.game.strategy.MovementStrategy
import com.bomjRogue.game.strategy.StrategyFactory
import kotlin.random.Random

enum class Modifier {
    REGULAR,
    STRONG,
    THICK
}

class NpcManager {

    // perhaps might be separate class
    private inner class NpcCreator {
        private fun getRandName() = "NPC_${Random.nextInt() + Random.nextInt(1, 9)}"

        fun getRandomNpc(): Npc {
            val stats = when (Modifier.values().toList().shuffled().first()) {
                Modifier.REGULAR -> {
                    getRegularStats()
                }
                Modifier.STRONG -> {
                    getStrongStats()
                }
                else -> {
                    getThickStats()
                }
            }

            return when (Random.nextInt(3)) {
                0 -> AggressiveNpc(getRandName(), Characteristics(stats))
                1 -> CowardNpc(getRandName(), Characteristics(stats))
                else -> RandomNpc(getRandName(), Characteristics(stats))
            }
        }

        fun getRegularStats(): MutableMap<CharacteristicType, Int> {
            return mutableMapOf(
                CharacteristicType.Health to defaultHealth,
                CharacteristicType.Armor to defaultArmor,
                CharacteristicType.Force to defaultForce,
            )
        }

        private fun getStrongStats(): MutableMap<CharacteristicType, Int> {
            return mutableMapOf(
                CharacteristicType.Health to defaultHealth,
                CharacteristicType.Armor to (defaultArmor + (Random.nextDouble(1.0, 2.0) * defaultArmor).toInt()),
                CharacteristicType.Force to (defaultForce + (Random.nextDouble(1.0, 1.5) * defaultForce).toInt()),
            )
        }

        private fun getThickStats(): MutableMap<CharacteristicType, Int> {
            return mutableMapOf(
                CharacteristicType.Health to (defaultHealth + (Random.nextDouble(1.0, 1.5) * defaultForce).toInt()),
                CharacteristicType.Armor to (defaultArmor + (Random.nextDouble(1.0, 1.3) * defaultArmor).toInt()),
                CharacteristicType.Force to defaultForce,
            )
        }
    }


    inner class NpcConfigurer {
        private lateinit var currentNpc: Npc

        fun switchTo(npc: Npc) {
            currentNpc = npc
        }

        fun setRandom() = strategies.put(currentNpc, StrategyFactory.INSTANCE.getRandomMoveStrategy())

        fun setAllRandom() {
            strategies.replaceAll { _, _ -> StrategyFactory.INSTANCE.getRandomMoveStrategy() }
        }

        fun setHunt() = strategies.put(currentNpc, StrategyFactory.INSTANCE.getAggressiveStrategy())

        fun setAllHunt() {
            strategies.replaceAll { _, _ -> StrategyFactory.INSTANCE.getAggressiveStrategy() }
        }

        fun getCurrent() = getCurrentStrategy(currentNpc)
    }

    private var strategies = mutableMapOf<Npc, MovementStrategy>()
    private var npcs = mutableListOf<Npc>()
    private val npcCreator = NpcCreator()

    fun initWith(npcList: MutableList<Npc>) {
        npcs = npcList
        initStrategies()
    }

    private fun initStrategies(params: List<Class<out MovementStrategy>> = emptyList()) {
        val factory = StrategyFactory.INSTANCE
        if (params.isEmpty()) {
            npcs.forEach {
                strategies[it] = factory.getStrategy(it::class)
            }
        }
    }

    fun getDefaultStats(): Characteristics = Characteristics(npcCreator.getRegularStats())

    fun getRandomNpc(): Npc {
        return npcCreator.getRandomNpc()
    }

    fun getRandomNpcForCount(count: Int = defaultNpcCount): MutableList<Npc> {
        val ans = mutableListOf<Npc>()
        for (i in 0 until count) {
            ans.add(getRandomNpc())
        }
        return ans
    }

    fun makeMoveNpc(npc: Npc) {
        strategies[npc]!!.makeMove(npc)
    }

    fun getCurrentStrategy(npc: Npc): MovementStrategy? {
        if (!strategies.containsKey(npc)) {
            return null
        }
        return strategies[npc]
    }

    fun getNpcList() = npcs

    fun remove(obj: Npc) {
        strategies.remove(obj)
        npcs.remove(obj)
    }

    fun removeAt(ind: Int) {
        val npc = npcs[ind]
        strategies.remove(npc)
        npcs.removeAt(ind)
    }

}