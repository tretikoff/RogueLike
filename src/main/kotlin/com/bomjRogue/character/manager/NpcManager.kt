package com.bomjRogue.character.manager

import com.bomjRogue.character.CharacteristicType
import com.bomjRogue.character.Characteristics
import com.bomjRogue.character.CharacteristicsMap
import com.bomjRogue.character.Npc
import com.bomjRogue.config.SettingsManager.Companion.defaultArmor
import com.bomjRogue.config.SettingsManager.Companion.defaultForce
import com.bomjRogue.config.SettingsManager.Companion.defaultHealth
import com.bomjRogue.config.SettingsManager.Companion.defaultNpcCount
import com.bomjRogue.game.strategy.Strategy
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
        private fun getRandName() = "NPC_${Random.nextInt()+ Random.nextInt(1, 9)}"

        fun getRandomNpc(): Npc {
            val stats : CharacteristicsMap = when (Modifier.values().toList().shuffled().first()) {
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

            return Npc(getRandName(), Characteristics(stats))
        }

        fun getStrongNpc(): Npc {
            val stats = getStrongStats()
            return Npc(getRandName(), Characteristics(stats))
        }

        fun getThickNpc(): Npc {
            val stats = getThickStats()
            return Npc(getRandName(), Characteristics(stats))
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
                CharacteristicType.Armor to (defaultArmor + (Random.nextDouble(1.0, 2.0)*defaultArmor).toInt()),
                CharacteristicType.Force to (defaultForce + (Random.nextDouble(1.0, 1.5)*defaultForce).toInt()),
            )
        }

        private fun getThickStats(): MutableMap<CharacteristicType, Int> {
            return mutableMapOf(
                CharacteristicType.Health to (defaultHealth+ (Random.nextDouble(1.0, 1.5)*defaultForce).toInt()),
                CharacteristicType.Armor to (defaultArmor + (Random.nextDouble(1.0, 1.3)*defaultArmor).toInt()),
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
            strategies.replaceAll { t, _ -> StrategyFactory.INSTANCE.getRandomMoveStrategy() }
        }

        fun setHunt() = strategies.put(currentNpc, StrategyFactory.INSTANCE.getHuntStrategy())

        fun setAllHunt() {
            strategies.replaceAll { t, _ -> StrategyFactory.INSTANCE.getHuntStrategy() }
        }

        fun getCurrent() = getCurrentStrategy(currentNpc)
    }

    private var strategies = mutableMapOf<Npc, Strategy>()
    private val defaultStrategy = StrategyFactory.INSTANCE.getStrategy()
    private var npcs = mutableListOf<Npc>()
    private val npcConfigurator = NpcConfigurer()
    private val npcCreator = NpcCreator()

    fun initWith(npcList: MutableList<Npc>) {
        npcs = npcList
        initStrategies()
    }

    private fun initStrategies(params: List<out Strategy> = emptyList()) {
        val factory = StrategyFactory.INSTANCE
        if (params.isEmpty()) {
            npcs.forEach {
                strategies[it] = factory.getStrategy()
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

    fun configureNpc(npc: Npc): NpcConfigurer {
        if (!strategies.containsKey(npc)) {
            strategies[npc] = defaultStrategy
        }
        npcConfigurator.switchTo(npc)
        return npcConfigurator
    }

    fun getCurrentStrategy(npc: Npc): Strategy? {
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