package io.github.kroha22.tempo.model

enum class LessonStage { Learn, Activity, Summary }
enum class CardCollection { Lesson, Verbs }

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
        val lessonDeckSize = state.savedCardIds.size.takeIf { it > 0 } ?: minOf(6, demoCards.size)
        val deckSize = if (state.cardCollection == CardCollection.Verbs) verbDeck.size else lessonDeckSize
        state.copy(cardIndex = (state.cardIndex + 1) % deckSize, cardRevealed = false)
    }
    is TempoAction.ToggleSavedCard -> state.copy(
        savedCardIds = if (action.cardId in state.savedCardIds) state.savedCardIds - action.cardId else state.savedCardIds + action.cardId,
    )
}

fun encodeTempoState(state: TempoState): String = listOf(
    "v1",
    state.presentation.name,
    state.completedLessonIds.sorted().joinToString(","),
    state.demonstratedLessonIds.sorted().joinToString(","),
    state.savedCardIds.sorted().joinToString(","),
).joinToString("\n")

fun decodeTempoState(value: String?): TempoState {
    val lines = value?.lines() ?: return TempoState()
    if (lines.firstOrNull() != "v1") return TempoState()
    val validLessonIds = demoLessons.mapTo(mutableSetOf()) { it.id }
    val validCardIds = demoCards.mapTo(mutableSetOf()) { it.id }
    fun ids(index: Int, valid: Set<String>) = lines.getOrNull(index).orEmpty().split(',').filterTo(mutableSetOf()) { it.isNotBlank() && it in valid }
    return TempoState(
        presentation = runCatching { PresentationMode.valueOf(lines.getOrNull(1).orEmpty()) }.getOrDefault(PresentationMode.Adult),
        completedLessonIds = ids(2, validLessonIds),
        demonstratedLessonIds = ids(3, validLessonIds),
        savedCardIds = ids(4, validCardIds),
    )
}
