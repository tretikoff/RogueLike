package com.bomjRogue.character.manager

import com.bomjRogue.character.Npc
import com.bomjRogue.game.strategy.Strategy
import com.bomjRogue.game.strategy.StrategyFactory

class NpcManager {

    inner class NpcConfigurer {
        private lateinit var currentNpc: Npc

        fun lockOn(npc: Npc) {
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


    fun init(npcList: MutableList<Npc>) {
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

    fun makeMoveNpc(npc: Npc) {
        strategies[npc]!!.makeMove(npc)
    }

    fun configureNpc(npc: Npc): NpcConfigurer {
        if (!strategies.containsKey(npc)) {
            strategies[npc] = defaultStrategy
        }
        npcConfigurator.lockOn(npc)
        return npcConfigurator
    }

    fun getCurrentStrategy(npc: Npc): Strategy? {
        if (!strategies.containsKey(npc)) {
            return null
        }
        return strategies[npc]
    }


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