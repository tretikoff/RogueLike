package com.bomjRogue.game

import com.bomjRogue.MusicUpdate
import com.bomjRogue.PlayerUpdate
import com.bomjRogue.Update
import com.bomjRogue.character.GameCharacter
import com.bomjRogue.character.Npc
import com.bomjRogue.character.Player
import com.bomjRogue.character.manager.NpcManager
import com.bomjRogue.character.manager.PlayersManager
import com.bomjRogue.config.SettingsManager.Companion.defaultNpcCount
import com.bomjRogue.config.SettingsManager.Companion.defaultSword
import com.bomjRogue.config.Utils.Companion.fleshHitSoundName
import com.bomjRogue.config.Utils.Companion.healthPickUpSoundName
import com.bomjRogue.config.Utils.Companion.itemPickUpSoundName
import com.bomjRogue.game.strategy.StrategyFactory
import com.bomjRogue.world.*
import com.bomjRogue.world.Map.PredefinedCoords.doorSpawn
import com.bomjRogue.world.Map.PredefinedCoords.playerSpawn
import com.bomjRogue.world.interactive.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.IllegalStateException
import kotlin.random.Random

class Game {
    fun run() {
        while (true) {
            runBlocking { delay(10) }
            logic()
        }
    }

    private val exitDoor = ExitDoor()
    private var npcCount = defaultNpcCount
    private val map = LevelGenerator.generateMap()
    private var items = mutableListOf<Item>()
    private var updates = mutableListOf<Update>()
    private val updatesMutex = Mutex()
    private val strategyFactory = StrategyFactory.init(map)
    private val npcManager = NpcManager()
    private val playersManager = PlayersManager() // i know it might not be the best

    private fun addUpdate(update: Update) {
        runBlocking {
            updatesMutex.withLock {
                updates.add(update)
            }
        }
    }

    suspend fun getUpdates(): List<Update> {
        updatesMutex.withLock {
            val upd = ArrayList(updates)
            updates = mutableListOf()
            return upd
        }
    }

    fun join(playerName: String): GameCharacter {
        val player = Player(
            playerName, npcManager.getDefaultStats(), // todo : we can store it inside and fix bug with transportation
//            HeroInventoryManager()
        )
        playersManager.addPlayer(player)
        playersManager.takeItem(player, defaultSword)
        map.add(player, Position(Coordinates(20f, 20f), Size(34f, 19f)))
        return player
    }

    fun makeMove(name: String, x: Float, y: Float) {
        val player = playersManager.getPlayers().firstOrNull { it.myName == name } ?: return
        map.move(player, x, y)
        if (map.objectsConnect(player, exitDoor)) {
            if (npcManager.getNpcList().isNotEmpty()) {
                npcCount++
            }
            initialize(false)
        }
    }

    fun respawn(name: String) {
        val players = playersManager.getPlayers()
        val player = players.firstOrNull { it.myName == name } ?: return
        if (players.size < 2) {
            initialize(true)
            return
        }
        player.reset()
        playersManager.resetInventory(player)
        playersManager.takeItem(player, defaultSword)
        map.remove(player)
        map.add(player, playerSpawn)
    }

    fun hit(name: String) {
        val player = playersManager.getPlayers().firstOrNull { it.myName == name } ?: return
        makeDamage(player)
    }

    fun initialize(reset: Boolean = true) {
        map.reset()
        strategyFactory.updateMap(map)
        initializeNpcs()
        initializeItems()
        val players = playersManager.getPlayers()
        if (reset) {
            players.forEach { it.reset() }
        }
        players.forEach {
            map.add(it, Position(Coordinates(20f, 20f), Size(34f, 19f)))
        }
        map.add(exitDoor, doorSpawn)
    }

    private fun initializeItems() {
        items.clear()
        val weapon = Sword(10)
        val health = Health(50)
        items.add(weapon)
        items.add(health)
        map.addRandomPlace(weapon, Size(40f, 40f))
        map.addRandomPlace(health, Size(25f, 25f))
    }

    fun getGameItems(): GameItems {
        return HashMap(map.location)
    }

    private fun initializeNpcs() {
        val newNpc = npcManager.getRandomNpcForCount(defaultNpcCount)
        newNpc.forEach {
            val size = when (it.type) {
                ObjectType.Npc -> Size(36f, 20f)
                ObjectType.CowardNpc -> Size(32f, 25f)
                ObjectType.AggressiveNpc -> Size(25f, 25f)
                else -> throw IllegalStateException()
            }
            map.addRandomPlace(it, size)
        }
        npcManager.initWith(newNpc)
    }

    fun makeDamage(hitman: GameCharacter): Boolean {
        var noDamage = true
        for (pl in npcManager.getNpcList() + playersManager.getPlayers()) { // todo: sync state
            if (pl != hitman && map.objectsConnect(hitman, pl)) {
                if (pl is Player) {
                    addUpdate(PlayerUpdate(pl))
                    addUpdate(MusicUpdate(fleshHitSoundName))
                }
                noDamage = false
                var damage = hitman.getForce()
                if (hitman is Player) {
                    damage += playersManager.itemDamage(hitman)
                }
                pl.takeDamage(hitman.getForce())
                if (pl.getHealth() <= 0) {
                    map.remove(pl)
                    if (pl is Npc) {
                        npcManager.remove(pl)
                        continue
                    }
                    if (pl is Player) {
                        respawn(pl.name)
                        run()
                    }
                }
            }
        }
        return !noDamage
    }

    private fun logic() {
        moveNpcs()
        pickItems()
    }

    private fun moveNpcs() {
        for (npc in npcManager.getNpcList()) {
            for (player in playersManager.getPlayers()) {
                if (player.isDead()) {
                    continue
                }
                if (map.objectsConnect(npc, player)) {
                    if (Random.nextInt(100) < 5) {
                        makeDamage(npc)
                    }
                }
            }
            npcManager.makeMoveNpc(npc)
        }
    }


    private fun pickItems() {
        val toRemove = mutableListOf<Item>()
        for (item in items) {
            for (player in playersManager.getPlayers()) {
                if (map.objectsConnect(item, player)) {
                    if (item is Health) {
                        player.addHealth(item.healthPoints)
                        addUpdate(MusicUpdate(healthPickUpSoundName))
                    } else if (item is Sword) {
                        if (!playersManager.canPickUp(player)) {
                            continue
                        }
                        playersManager.takeItem(player, item)
                        addUpdate(MusicUpdate(itemPickUpSoundName))
                    }
                    addUpdate(PlayerUpdate(player))
                    toRemove.add(item)
                }
            }
        }
        toRemove.forEach {
            items.remove(it)
            map.remove(it)
        }
    }

    fun processDisconnect(playerToRemove: String) {
        val foundPlayer: Player?
        val players = playersManager.getPlayers()
        foundPlayer = players.find {
            it.myName == playerToRemove
        }
        if (foundPlayer != null) {
            map.remove(foundPlayer)
            playersManager.removePlayer(foundPlayer)
        }

    }
}