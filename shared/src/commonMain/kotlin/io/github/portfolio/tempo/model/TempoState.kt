package io.github.portfolio.tempo.model

data class TempoState(
    val area: RootArea = RootArea.Learning,
    val presentation: PresentationMode = PresentationMode.Adult,
    val selectedLessonId: String? = null,
    val completedLessonIds: Set<String> = emptySet(),
    val cardIndex: Int = 0,
    val cardRevealed: Boolean = false,
    val savedCardIds: Set<String> = emptySet(),
)

sealed interface TempoAction {
    data class SelectArea(val area: RootArea) : TempoAction
    data class SetPresentation(val mode: PresentationMode) : TempoAction
    data class OpenLesson(val lessonId: String) : TempoAction
    data object CloseLesson : TempoAction
    data class CompleteLesson(val lessonId: String) : TempoAction
    data object FlipCard : TempoAction
    data object NextCard : TempoAction
    data class ToggleSavedCard(val cardId: String) : TempoAction
}

fun reduce(state: TempoState, action: TempoAction): TempoState = when (action) {
    is TempoAction.SelectArea -> state.copy(area = action.area, selectedLessonId = null)
    is TempoAction.SetPresentation -> state.copy(presentation = action.mode)
    is TempoAction.OpenLesson -> state.copy(selectedLessonId = action.lessonId)
    TempoAction.CloseLesson -> state.copy(selectedLessonId = null)
    is TempoAction.CompleteLesson -> state.copy(
        selectedLessonId = null,
        completedLessonIds = state.completedLessonIds + action.lessonId,
    )
    TempoAction.FlipCard -> state.copy(cardRevealed = !state.cardRevealed)
    TempoAction.NextCard -> state.copy(
        cardIndex = (state.cardIndex + 1) % demoCards.size,
        cardRevealed = false,
    )
    is TempoAction.ToggleSavedCard -> state.copy(
        savedCardIds = if (action.cardId in state.savedCardIds) {
            state.savedCardIds - action.cardId
        } else {
            state.savedCardIds + action.cardId
        },
    )
}
