package io.github.portfolio.tempo

import io.github.portfolio.tempo.model.PresentationMode
import io.github.portfolio.tempo.model.RootArea
import io.github.portfolio.tempo.model.TempoAction
import io.github.portfolio.tempo.model.TempoState
import io.github.portfolio.tempo.model.demoCards
import io.github.portfolio.tempo.model.reduce
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TempoStateTest {
    @Test
    fun adultAndChildUseTheSameLearningState() {
        val started = TempoState(completedLessonIds = setOf("space-basics"))
        val child = reduce(started, TempoAction.SetPresentation(PresentationMode.Child))

        assertEquals(PresentationMode.Child, child.presentation)
        assertEquals(started.completedLessonIds, child.completedLessonIds)
    }

    @Test
    fun switchingAreasClosesAnOpenLesson() {
        val lesson = reduce(TempoState(), TempoAction.OpenLesson("space-basics"))
        val cards = reduce(lesson, TempoAction.SelectArea(RootArea.Cards))

        assertEquals(RootArea.Cards, cards.area)
        assertEquals(null, cards.selectedLessonId)
    }

    @Test
    fun cardSessionWrapsAndResetsTheRevealState() {
        var state = TempoState(cardIndex = demoCards.lastIndex, cardRevealed = true)
        state = reduce(state, TempoAction.NextCard)

        assertEquals(0, state.cardIndex)
        assertFalse(state.cardRevealed)
    }

    @Test
    fun savedCardsToggleWithoutChangingStableIds() {
        val id = demoCards.first().id
        val saved = reduce(TempoState(), TempoAction.ToggleSavedCard(id))
        val removed = reduce(saved, TempoAction.ToggleSavedCard(id))

        assertTrue(id in saved.savedCardIds)
        assertFalse(id in removed.savedCardIds)
    }
}
