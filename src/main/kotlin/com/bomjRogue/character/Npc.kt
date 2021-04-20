package com.bomjRogue.character

import com.bomjRogue.game.strategy.StrategyType
import com.bomjRogue.world.interactive.ObjectType

class Npc(val strategyType: StrategyType, name: String, characteristics: Characteristics) : GameCharacter(
    name, characteristics, when (strategyType) {
        StrategyType.Passive -> ObjectType.Npc
        StrategyType.Aggressive -> ObjectType.AggressiveNpc
        StrategyType.Coward -> ObjectType.CowardNpc
    }
)