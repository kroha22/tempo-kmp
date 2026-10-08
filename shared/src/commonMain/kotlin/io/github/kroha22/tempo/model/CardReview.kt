package io.github.kroha22.tempo.model

import kotlin.math.roundToLong

data class CardReview(val due: Long, val interval: Double, val ease: Double = 2.5, val repetitions: Int = 0, val lapses: Int = 0, val grade: Int = 0)

fun reviewInterval(previous: CardReview?, grade: Int, lowerPriority: Boolean): Double {
    val interval = previous?.interval ?: 0.0
    val ease = previous?.ease ?: 2.5
    val days = when (grade) {
        0 -> 1.0 / 1440
        1 -> if (interval > 0) maxOf(10.0 / 1440, interval * 1.2) else 10.0 / 1440
        2 -> if (interval > 0) maxOf(1.0, interval * ease) else 1.0
        else -> if (interval > 0) maxOf(4.0, interval * ease * 1.3) else 4.0
    }
    return if (lowerPriority && grade > 1) days * 1.8 else days
}

fun scheduleCard(id: String, previous: CardReview?, grade: Int, now: Long): CardReview {
    val verb = verbDeck.find { it.id == id }
    val interval = reviewInterval(previous, grade, verb != null && (verb.basic || verb.rank <= 40))
    val delta = when (grade) { 0 -> -.2; 1 -> -.12; 3 -> .12; else -> 0.0 }
    return CardReview(now + (interval * 86_400_000).roundToLong(), interval, ((previous?.ease ?: 2.5) + delta).coerceIn(1.3, 3.2), if (grade == 0) 0 else (previous?.repetitions ?: 0) + 1, (previous?.lapses ?: 0) + if (grade == 0) 1 else 0, grade)
}

fun intervalText(days: Double): String = when {
    days < 1.0 / 24 -> "${maxOf(1, (days * 1440).roundToLong())} мин"
    days < 1 -> "${(days * 24).roundToLong()} ч"
    days < 30 -> "${days.roundToLong()} дн"
    else -> "${(days / 30).roundToLong()} мес"
}
