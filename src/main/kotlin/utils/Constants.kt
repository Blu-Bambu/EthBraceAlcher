package utils

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory

class Constants {
    enum class Items(val id: Int) {
        UNCHARGED_ETH_BRACE_ID(21817),
        CHARGED_ETH_BRACE_ID(21816),
        ETHER(21820),
        ;

        fun isPresentInInv(): Boolean {
            return Inventory.stream().id(id).isNotEmpty()
        }

        fun countFromInv(): Int {
            val itemStream = Inventory.stream().id(id)
            if (itemStream.first().stackable()) {
                return itemStream.first().stackSize()
            }

            return itemStream.count().toInt()
        }

        fun isPresentInBank(): Boolean {
            return Bank.stream().id(id).isNotEmpty()
        }

        fun interact(action: String): Boolean {
            return Inventory.stream().id(id).first().interact(action)
        }
    }
}