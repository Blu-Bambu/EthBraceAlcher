import java.util.logging.Logger
import org.powbot.api.Condition
import org.powbot.api.Notifications
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.mobile.script.ScriptManager
import utils.Constants.Items
import utils.Task
import java.lang.System.Logger.Level

fun main(args: Array<String>){
    EthBraceAlcher().startScript()
}

@ScriptManifest(
    name = "Blu Ethereum Bracelet Alcher",
    description = "Creates charged ethereum bracelets and alchs them",
)
class EthBraceAlcher: AbstractScript() {
    val tasks = arrayOf(
        GetEthBraceMaterialFromBank(log),
        CheckOnlyOneEtherInInv(log),
        CreateChargedEthBrace(log),
        AlchEthBrace(log),
    )

    var currentTaskName = "N/A"

    override fun onStart() {
        val paint = PaintBuilder.newBuilder()
            .trackSkill(Skill.Magic)
            .addString("Action", { currentTaskName })

        addPaint(paint.build())
    }

    override fun poll() {
        for (task in tasks) {
            if (task.check()) {
                currentTaskName = task.getName()
                task.exec()
                break
            }
        }
    }
}

class GetEthBraceMaterialFromBank(logger: Logger): Task(logger) {
    override fun check(): Boolean {
        return !Items.ETHER.isPresentInInv() ||
                !Items.UNCHARGED_ETH_BRACE_ID.isPresentInInv()
    }

    override fun exec() {
        if (!Bank.opened()) {
            if (!Bank.open() || !Condition.wait{ Bank.opened() }) {
                logger.warning("Failed to open bank")
                return
            }
        }

        if (!Items.ETHER.isPresentInInv()) {
            if (!Items.ETHER.isPresentInBank()) {
                logger.warning("Out of ether")
                Notifications.showNotification("Out of ether")
                ScriptManager.stop()
                return
            }

            logger.info("Getting ether from bank")
            if (!Bank.withdraw(Items.ETHER.id, 1)) {
                logger.warning("Failed to withdraw ether from bank")
            }
        }

        if (!Items.UNCHARGED_ETH_BRACE_ID.isPresentInInv()) {
            if (!Items.UNCHARGED_ETH_BRACE_ID.isPresentInBank()) {
                logger.warning("Out of uncharged eth brace")
                Notifications.showNotification("Out of uncharged eth brace")
                ScriptManager.stop()
                return
            }

            logger.info("Getting uncharged eth brace from bank")
            if (!Bank.withdraw(Items.UNCHARGED_ETH_BRACE_ID.id, 1)) {
                logger.warning("Failed to withdraw uncharged eth brace from bank")
            }
        }
    }
}

/**
 * Checks if there is more than one ether in the inventory and deposits all but one. This class is
 * necessary since uncharged bracelets will consume all ether in the inventory but doesn't change
 * the alch value.
 */
class CheckOnlyOneEtherInInv(log: Logger): Task(log) {
    override fun check(): Boolean {
        return Items.ETHER.countFromInv() > 1
    }

    override fun exec() {
        if (!Bank.opened()) {
            if (!Bank.open() || !Condition.wait{ Bank.opened() }) {
                logger.warning("Failed to open bank")
                return
            }
        }

        if (Bank.deposit(Items.ETHER.id, Bank.Amount.ALL_BUT_ONE)) {
            Condition.wait{ Items.ETHER.countFromInv() == 1 }
        } else {
            logger.warning("Failed to deposit all but one ether")
        }
    }
}

class CreateChargedEthBrace(log: Logger): Task(log) {
    override fun check(): Boolean {
        return Items.ETHER.isPresentInInv() && Items.UNCHARGED_ETH_BRACE_ID.isPresentInInv()
    }

    override fun exec() {
        if (Bank.opened()) {
            if (Bank.close()) {
                if (Condition.wait{ !Bank.opened() }) {
                    logger.warning("Failed to close bank")
                    return
                }
            }
        }

        logger.info("Highlighting ether")
        if (!Items.ETHER.interact("Use") ||
            !Condition.wait{ Inventory.selectedItem().id == Items.ETHER.id }) {
            logger.info("Combining ether and uncharged eth brace")
            if (!Items.UNCHARGED_ETH_BRACE_ID.interact("Use") || !
                Condition.wait{ Items.CHARGED_ETH_BRACE_ID.isPresentInInv() }) {
                logger.warning("Failed to combine ether and uncharged eth brace")
            }
        } else {
            logger.warning("Failed to highlight ether")
        }
    }
}

class AlchEthBrace(log: Logger): Task(log) {
    override fun check(): Boolean {
        return Items.CHARGED_ETH_BRACE_ID.isPresentInInv()
    }

    override fun exec() {
        if (Bank.opened()) {
            if (!Bank.close() || !Condition.wait { !Bank.opened() }) {
                logger.warning("Failed to close bank")
                return
            }
        }

        logger.info("Casting high alch")
        if (!Magic.Spell.HIGH_ALCHEMY.canCast()) {
            logger.warning("Can't cast high alch, potentially out of runes")
            Notifications.showNotification("Can't cast high alch, potentially out of runes")
            ScriptManager.stop()
            return
        }

        logger.info("Alching eth brace")
        if (Magic.Spell.HIGH_ALCHEMY.cast()) {
            // Casting high alch will automatically open the inventory tab
            if (Condition.wait { Game.tab() == Game.Tab.INVENTORY }) {
                if (Items.CHARGED_ETH_BRACE_ID.interact("Cast")) {
                    if (Condition.wait { !Items.CHARGED_ETH_BRACE_ID.isPresentInInv() }) {
                        logger.info("Successfully alched eth brace")
                    }
                }
            }
        } else {
            logger.warning("Failed to cast high alch")
        }
    }
}