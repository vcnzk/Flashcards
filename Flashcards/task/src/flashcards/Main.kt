package flashcards

import java.io.File

fun main(args: Array<String>) {
    val deck = Deck()
    val log = Log
    val import = if (args.isNotEmpty() && args.contains("-import")) args[args.indexOf("-import") + 1] else ""
    val export = if (args.isNotEmpty() && args.contains("-export")) args[args.indexOf("-export") + 1] else ""
    if (import.isNotEmpty()) {
        deck.import(import)
    }

    while (true) {
        log.logPrint("")
        log.logPrint("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        when (log.logRead()) {
            "add" -> deck.add()
            "remove" -> deck.remove()
            "import" -> deck.import()
            "export" -> deck.export()
            "ask" -> deck.ask()
            "log" -> deck.log()
            "hardest card" -> deck.hardest()
            "reset stats" -> deck.reset()
            "exit" -> {
                log.logPrint("Bye bye!")
                if (export.isNotEmpty()) deck.export(export)
                return
            }
        }
    }
}

class Deck {
    private val cards = mutableMapOf<String, String>()
    private val mistakes = mutableMapOf<String, Int>()
    private val log = Log
    fun add() {
        log.logPrint("The card:")
        val term = log.logRead()
        if (cards.keys.contains(term)) {
            log.logPrint("The card \"$term\" already exists.")
            return
        }

        log.logPrint("The definition of the card:")
        val definition = log.logRead()
        if (cards.values.contains(definition)) {
            log.logPrint("The definition \"$definition\" already exists.")
            return
        }

        cards[term] = definition
        mistakes[term] = 0
        log.logPrint("The pair (\"$term\":\"$definition\") has been added")
    }

    fun remove() {
        log.logPrint("Which card?")
        val key = log.logRead()
        if (cards.keys.contains(key)) {
            cards.remove(key)
            mistakes.remove(key)
            log.logPrint("The card has been removed.")
        } else {
            log.logPrint("Can't remove \"$key\": there is no such card.")
        }
    }

    fun ask() {
        log.logPrint("How many times to ask?")
        var counter = log.logRead().toInt()
        for ((key, value) in cards) {
            log.logPrint("Print the definition of \"$key\":")
            val definition = log.logRead()
            if (definition != value && cards.values.contains(definition)) {
                val term = cards.filter { (key, value) -> value == definition }.keys.joinToString()
                mistakes[key] = (mistakes[key] ?: 0) + 1
                log.logPrint("Wrong. The right answer is \"$value\", but your definition is correct for \"$term\".")
            } else if (definition == value) {
                log.logPrint("Correct!")
            } else {
                mistakes[key] = (mistakes[key] ?: 0) + 1
                log.logPrint("Wrong. The right answer is \"$value\".")
            }
            counter -= 1
            if (counter < 1) return
        }
    }

    fun export(text: String = "") {
        if (text.isEmpty()) log.logPrint("File name:")
        val fileName = if (text.isEmpty()) log.logRead() else text
        val file = File(fileName)
        var emptyFile = true
        for ((key, value) in cards) {
            if (emptyFile) {
                file.writeText("$key,.$value,.${mistakes[key]}")
                emptyFile = false
                continue
            }
            file.appendText("\n")
            file.appendText("$key,.$value,.${mistakes[key]}")
        }
        log.logPrint("${cards.size} cards have been saved.")
    }

    fun import(text: String = "") {
        if (text.isEmpty()) log.logPrint("File name:")
        val fileName = if (text.isEmpty()) log.logRead() else text
        val file = File(fileName)
        if (!file.exists()) {
            log.logPrint("File not found.")
            return
        }
        val cardsInFile = file.readLines()
        for (item in cardsInFile) {
            val card = item.split(",.")
            val key = card[0]
            val value = card[1]
            val mistake = card[2].toInt()
            cards[key] = value
            mistakes[key] = mistake
        }
        log.logPrint("${cardsInFile.size} cards have been loaded.")
    }

    fun log() {
        log.logPrint("File name:")
        val fileName = log.logRead()
        val file = File(fileName)
        for (item in log.log) {
            file.appendText(item + "\n")
        }
        log.logPrint("The log has been saved.")
    }

    fun hardest() {
        val maxMistakes = mistakes.values.maxOrNull()
        val termsMaxMistakes = mutableListOf<String>()
        mistakes.forEach {
            if (it.value == maxMistakes) termsMaxMistakes.add(it.key)
        }
        if (termsMaxMistakes.size == 0 || maxMistakes == 0) {
            log.logPrint("There are no cards with errors.")
        } else if (termsMaxMistakes.size == 1) {
            log.logPrint("The hardest card is \"${termsMaxMistakes[0]}\". You have $maxMistakes errors answering it.")
        } else log.logPrint("The hardest cards are \"${termsMaxMistakes.joinToString("\", \"")}\". You have $maxMistakes errors answering them.")
    }

    fun reset() {
        for (key in mistakes.keys) {
            mistakes[key] = 0
        }
        log.logPrint("Card statistics have been reset.")
    }
}

object Log {
    val log = mutableListOf<String>()

    fun logPrint(text: String) {
        log.add(text)
        println(text)
    }

    fun logRead(): String {
        val text = readln()
        log.add(text)
        return text
    }
}