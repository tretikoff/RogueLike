package com.bomjRogue.game

import com.bomjRogue.MusicUpdate
import com.bomjRogue.PlayerUpdate
import com.bomjRogue.Update
import com.bomjRogue.character.*
import com.bomjRogue.character.manager.NpcManager
import com.bomjRogue.config.Utils.Companion.fleshHitSoundName
import com.bomjRogue.config.Utils.Companion.itemPickUpSoundName
import com.bomjRogue.game.strategy.StrategyFactory
import com.bomjRogue.world.*
import com.bomjRogue.world.Map.PredefinedCoords.doorSpawn
import com.bomjRogue.world.Map.PredefinedCoords.playerSpawn
import com.bomjRogue.world.interactive.ExitDoor
import com.bomjRogue.world.interactive.Health
import com.bomjRogue.world.interactive.Item
import com.bomjRogue.world.interactive.Sword
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class Game {
    fun run() {
        while (true) {
            runBlocking { delay(10) }
            logic()
        }
    }

    private val players = mutableListOf<Player>()
    private val exitDoor = ExitDoor()
    private val defaultNpcCount = 5
    private var npcCount = defaultNpcCount
    private val map = LevelGenerator.generateMap()
    private var npcs = mutableListOf<Npc>()
    private var items = mutableListOf<Item>()
    private var updates = mutableListOf<Update>()
    private val updatesMutex = Mutex()
    private val strategyFactory = StrategyFactory.init(map)
    private val npcManager = NpcManager()


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

    fun join(playerName: String): Character {
        val player = Player(
            playerName, Characteristics(
                mutableMapOf(
                    CharacteristicType.Health to 100,
                    CharacteristicType.Armor to 10,
                    CharacteristicType.Force to 20
                )
            )
        )
        players.add(player)
        map.add(player, Position(Coordinates(20f, 20f), Size(34f, 19f)))
        return player
    }

    fun makeMove(name: String, x: Float, y: Float) {
        val player = players.firstOrNull { it.name == name } ?: return
        map.move(player, x, y)
        if (map.objectsConnect(player, exitDoor)) {
            if (npcs.isNotEmpty()) {
                npcCount++
            }
            initialize(false)
        }
    }

    fun respawn(name: String) {
        val player = players.firstOrNull { it.name == name } ?: return
        if (players.size < 2) {
            initialize(true)
            return
        }
        player.reset()
        map.remove(player)
        map.add(player, playerSpawn)
    }

    fun hit(name: String) {
        val player = players.firstOrNull { it.name == name } ?: return
        makeDamage(player)
    }

    fun initialize(reset: Boolean = true) {
        map.reset()
        strategyFactory.updateMap(map)
        initializeNpcs()
        initializeItems()
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
        val weapon = Sword(5)
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
        npcs.clear()
        for (i in 0 until npcCount) {
            npcs.add(
                Npc(
                    "Npc_$i", Characteristics(
                        mutableMapOf(
                            CharacteristicType.Health to 100,
                            CharacteristicType.Armor to 10,
                            CharacteristicType.Force to 20
                        )
                    ),
                )
            )
        }
        npcs.forEach { map.addRandomPlace(it, Size(36f, 20f)) }
        npcManager.init(npcs)
//        npcManager.configureNpc(npcs.first()).setAllHunt() // just for test
    }

    fun makeDamage(hitman: Character): Boolean {
        var noDamage = true
        for (pl in npcs + players) { // todo: sync state
            if (pl != hitman && map.objectsConnect(hitman, pl)) {
                if (pl is Player) {
                    addUpdate(PlayerUpdate(pl))
                    addUpdate(MusicUpdate(fleshHitSoundName))
                }
                noDamage = false
                pl.takeDamage(hitman.getForce())
                if (pl.getHealth() <= 0 ) {
                    map.remove(pl)
                    if (pl is Npc) {
                        npcs.remove(pl)
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
        for (npc in npcs) {
            for (player in players) {
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
            for (player in players) {
                if (map.objectsConnect(item, player)) {
                    if (item is Health) {
                        player.addHealth(item.healthPoints)
                    } else if (item is Sword) {
                        player.addForce(item.damage)
                    }
                    addUpdate(PlayerUpdate(player))
                    addUpdate(MusicUpdate(itemPickUpSoundName))
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
        var ind = 0
        var foundPlayer: Player? = null
        for (player in players) {
            if (player.name == playerToRemove) {
                foundPlayer = player
                break
            }
            ind ++
        }

        if (foundPlayer != null) {
            map.remove(foundPlayer)
            players.removeAt(ind)
         }
    }
}