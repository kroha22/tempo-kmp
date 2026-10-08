package io.github.kroha22.tempo.model

enum class LessonStage { Learn, Activity, Summary }
enum class CardCollection { Lesson, Verbs, General, School }

data class TempoState(
    val area: RootArea = RootArea.Learning,
    val presentation: PresentationMode = PresentationMode.Adult,
    val selectedLessonId: String? = null,
    val lessonStage: LessonStage = LessonStage.Learn,
    val selectedAnswerId: String? = null,
    val completedLessonIds: Set<String> = emptySet(),
    val demonstratedLessonIds: Set<String> = emptySet(),
    val cardCollection: CardCollection = CardCollection.Lesson,
    val cardIndex: Int = 0,
    val cardRevealed: Boolean = false,
    val savedCardIds: Set<String> = emptySet(),
    val cardReviews: Map<String, CardReview> = emptyMap(),
)

sealed interface TempoAction {
    data class SelectArea(val area: RootArea) : TempoAction
    data class SetPresentation(val mode: PresentationMode) : TempoAction
    data class OpenLesson(val lessonId: String) : TempoAction
    data object CloseLesson : TempoAction
    data object ContinueToActivity : TempoAction
    data class SelectAnswer(val answerId: String) : TempoAction
    data class CompleteLesson(val lessonId: String, val demonstrated: Boolean) : TempoAction
    data object RestartLesson : TempoAction
    data class SelectCardCollection(val collection: CardCollection) : TempoAction
    data object FlipCard : TempoAction
    data object NextCard : TempoAction
    data object PreviousCard : TempoAction
    data class RateCard(val cardId: String, val grade: Int, val now: Long) : TempoAction
    data class ToggleSavedCard(val cardId: String) : TempoAction
}

fun reduce(state: TempoState, action: TempoAction): TempoState = when (action) {
    is TempoAction.SelectArea -> state.copy(area = action.area, selectedLessonId = null, lessonStage = LessonStage.Learn, selectedAnswerId = null)
    is TempoAction.SetPresentation -> state.copy(presentation = action.mode)
    is TempoAction.OpenLesson -> state.copy(selectedLessonId = action.lessonId, lessonStage = LessonStage.Learn, selectedAnswerId = null)
    TempoAction.CloseLesson -> state.copy(selectedLessonId = null, lessonStage = LessonStage.Learn, selectedAnswerId = null)
    TempoAction.ContinueToActivity -> state.copy(lessonStage = LessonStage.Activity, selectedAnswerId = null)
    is TempoAction.SelectAnswer -> state.copy(selectedAnswerId = action.answerId)
    is TempoAction.CompleteLesson -> state.copy(
        lessonStage = LessonStage.Summary,
        completedLessonIds = state.completedLessonIds + action.lessonId,
        demonstratedLessonIds = if (action.demonstrated) state.demonstratedLessonIds + action.lessonId else state.demonstratedLessonIds - action.lessonId,
    )
    TempoAction.RestartLesson -> state.copy(lessonStage = LessonStage.Learn, selectedAnswerId = null)
    is TempoAction.SelectCardCollection -> state.copy(cardCollection = action.collection, cardIndex = 0, cardRevealed = false)
    TempoAction.FlipCard -> state.copy(cardRevealed = !state.cardRevealed)
    TempoAction.NextCard -> {
        val deckSize = deckIds(state).size
        state.copy(cardIndex = (state.cardIndex + 1) % deckSize, cardRevealed = false)
    }
    TempoAction.PreviousCard -> state.copy(cardIndex = (state.cardIndex - 1 + deckIds(state).size) % deckIds(state).size, cardRevealed = false)
    is TempoAction.RateCard -> {
        if (!state.cardRevealed || action.grade !in 0..3 || action.cardId != deckIds(state)[state.cardIndex % deckIds(state).size]) state
        else {
            val reviews = state.cardReviews + (action.cardId to scheduleCard(action.cardId, state.cardReviews[action.cardId], action.grade, action.now))
            val next = state.copy(cardReviews = reviews)
            if (state.cardCollection == CardCollection.Verbs) {
                val candidates = verbDeck.withIndex().filter { it.value.id != action.cardId }
                val available = candidates.filter { (reviews[it.value.id]?.due ?: 0L) <= action.now }
                val selected = (available.ifEmpty { candidates }).minWithOrNull(compareBy<IndexedValue<VerbDeckCard>> { if (available.isEmpty()) reviews[it.value.id]?.due ?: 0L else 0L }.thenBy { if (it.value.basic || it.value.rank <= 40) 1 else 0 }.thenBy { it.value.rank })!!
                next.copy(cardIndex = selected.index, cardRevealed = false)
            } else reduce(next, TempoAction.NextCard)
        }
    }
    is TempoAction.ToggleSavedCard -> state.copy(
        savedCardIds = if (action.cardId in state.savedCardIds) state.savedCardIds - action.cardId else state.savedCardIds + action.cardId,
    )
}

fun deckIds(state: TempoState): List<String> = when (state.cardCollection) {
    CardCollection.Verbs -> verbDeck.map { it.id }
    CardCollection.General -> nativeGeneralCards.map { it.id }
    CardCollection.School -> nativeSchoolCards.map { it.id }
    CardCollection.Lesson -> demoCards.filter { it.id in state.savedCardIds }.ifEmpty { demoCards.take(6) }.map { it.id }
}

fun encodeTempoState(state: TempoState): String = listOf(
    "v1",
    state.presentation.name,
    state.completedLessonIds.sorted().joinToString(","),
    state.demonstratedLessonIds.sorted().joinToString(","),
    state.savedCardIds.sorted().joinToString(","),
    state.cardReviews.entries.sortedBy { it.key }.joinToString(";") { (id, r) -> "$id|${r.due}|${r.interval}|${r.ease}|${r.repetitions}|${r.lapses}|${r.grade}" },
).joinToString("\n")

fun decodeTempoState(value: String?): TempoState {
    val lines = value?.lines() ?: return TempoState()
    if (lines.firstOrNull() != "v1") return TempoState()
    val validLessonIds = demoLessons.mapTo(mutableSetOf()) { it.id }
    val validCardIds = demoCards.mapTo(mutableSetOf()) { it.id }
    val reviewIds = validCardIds + nativeGeneralCards.map { it.id } + nativeSchoolCards.map { it.id } + verbDeck.map { it.id }
    val reviews = lines.getOrNull(5).orEmpty().split(';').mapNotNull { value ->
        val p = value.split('|')
        if (p.size != 7 || p[0] !in reviewIds) null else runCatching {
            p[0] to CardReview(p[1].toLong(), p[2].toDouble(), p[3].toDouble(), p[4].toInt(), p[5].toInt(), p[6].toInt()).also { require(it.interval.isFinite() && it.interval > 0 && it.ease in 1.3..3.2 && it.grade in 0..3 && it.repetitions >= 0 && it.lapses >= 0) }
        }.getOrNull()
    }.toMap()
    fun ids(index: Int, valid: Set<String>) = lines.getOrNull(index).orEmpty().split(',').filterTo(mutableSetOf()) { it.isNotBlank() && it in valid }
    return TempoState(
        presentation = runCatching { PresentationMode.valueOf(lines.getOrNull(1).orEmpty()) }.getOrDefault(PresentationMode.Adult),
        completedLessonIds = ids(2, validLessonIds),
        demonstratedLessonIds = ids(3, validLessonIds),
        savedCardIds = ids(4, validCardIds),
        cardReviews = reviews,
    )
}
