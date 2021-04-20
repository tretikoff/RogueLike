package com.bomjRogue.character.manager

import com.bomjRogue.world.interactive.Item
import com.bomjRogue.world.interactive.Sword

abstract class InventoryManager {
    private val maxCapacity = 3
    protected val items = LinkedHashSet<Item>()
    protected var setIter = items.iterator()

    protected lateinit var activeItem: Item

    fun takeItem(item: Item) {
        if (items.size < maxCapacity) {
            if (!this::activeItem.isInitialized || items.isEmpty()) {
                activeItem = item
            }
            items.add(item)

            setIter = items.iterator()
        }
    }

    fun dropItem(item: Item) {
        items.remove(item)
        setIter = items.iterator()
    }

    fun dropAll() {
        items.clear()
        setIter = items.iterator()
    }

    fun isNotFull() = items.size < maxCapacity

    abstract fun switchActiveItem()
}

class HeroInventoryManager : InventoryManager() {

    fun getActiveItemDamage(): Int {
        return if (activeItem is Sword) {
            (activeItem as Sword).damage
        } else 0
    }

    override fun switchActiveItem() {
        if (items.isNotEmpty()) {
            if (!setIter.hasNext()) {
                setIter = items.iterator()
                activeItem = setIter.next()
            } else {
                activeItem = setIter.next()
            }
        }
    }
}