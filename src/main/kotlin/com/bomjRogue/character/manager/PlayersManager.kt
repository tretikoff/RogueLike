package com.bomjRogue.character.manager

import com.bomjRogue.character.Player
import com.bomjRogue.world.interactive.Item

class PlayersManager {
    private var playerSet = mutableSetOf<Player>()
    private var mapper = mutableMapOf<Player, HeroInventoryManager>()

    fun addPlayer(player: Player): Boolean {
        if (!playerSet.contains(player)) {
            playerSet.add(player)
            mapper[player] = HeroInventoryManager()
            return true
        }
        return false
    }

    fun removePlayer(player: Player) : Boolean {
        if (!playerSet.contains(player)) {
            return false
        }
        mapper.remove(player)
        playerSet.remove(player)
        return true
    }

    fun getPlayers() = playerSet

    fun canPickUp(player: Player): Boolean = mapper[player]!!.isNotFull()

    fun takeItem(player: Player,item: Item) {
        mapper[player]!!.takeItem(item)
    }

    fun itemDamage(player: Player) = mapper[player]!!.getActiveItemDamage()

    fun switchActiveItem(player: Player) {
        mapper[player]!!.switchActiveItem()
    }

    fun resetInventory(player: Player) {
        mapper[player]!!.dropAll()
    }

}