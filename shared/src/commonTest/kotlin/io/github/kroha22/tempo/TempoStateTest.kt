package io.github.kroha22.tempo

import io.github.kroha22.tempo.model.PresentationMode
import io.github.kroha22.tempo.model.CardCollection
import io.github.kroha22.tempo.model.RootArea
import io.github.kroha22.tempo.model.TempoAction
import io.github.kroha22.tempo.model.TempoState
import io.github.kroha22.tempo.model.decodeTempoState
import io.github.kroha22.tempo.model.demoCards
import io.github.kroha22.tempo.model.demoLessons
import io.github.kroha22.tempo.model.encodeTempoState
import io.github.kroha22.tempo.model.reduce
import io.github.kroha22.tempo.model.verbDeck
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TempoStateTest {
    @Test
    fun adultAndChildUseTheSameLearningState() {
        val started = TempoState(completedLessonIds = setOf("lesson:a1-1:introduce-yourself"))
        val child = reduce(started, TempoAction.SetPresentation(PresentationMode.Child))

        assertEquals(PresentationMode.Child, child.presentation)
        assertEquals(started.completedLessonIds, child.completedLessonIds)
    }

    @Test
    fun switchingAreasClosesAnOpenLesson() {
        val lesson = reduce(TempoState(), TempoAction.OpenLesson("lesson:a1-1:introduce-yourself"))
        val cards = reduce(lesson, TempoAction.SelectArea(RootArea.Cards))

        assertEquals(RootArea.Cards, cards.area)
        assertEquals(null, cards.selectedLessonId)
    }

    @Test
    fun cardSessionWrapsAndResetsTheRevealState() {
        var state = TempoState(cardIndex = 5, cardRevealed = true)
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

    @Test
    fun canonicalFirstSliceHasSixLessonsAndCheckpoint() {
        assertEquals(7, demoLessons.size)
        assertEquals(6, demoLessons.count { !it.isCheckpoint })
        assertTrue(demoLessons.last().isCheckpoint)
    }

    @Test
    fun verbDeckPreservesAllLegacyIdsAndUniqueInfinitives() {
        assertEquals(1000, verbDeck.size)
        assertEquals("v0001", verbDeck.first().id)
        assertEquals("v1000", verbDeck.last().id)
        assertEquals(1000, verbDeck.map { it.id }.toSet().size)
        assertEquals(1000, verbDeck.map { it.infinitive }.toSet().size)
    }

    @Test
    fun switchingToVerbDeckResetsCardSession() {
        val state = TempoState(cardIndex = 4, cardRevealed = true)
        val verbs = reduce(state, TempoAction.SelectCardCollection(CardCollection.Verbs))

        assertEquals(CardCollection.Verbs, verbs.cardCollection)
        assertEquals(0, verbs.cardIndex)
        assertFalse(verbs.cardRevealed)
    }

    @Test
    fun persistentProgressRoundTripsAndDropsTransientScreenState() {
        val source = TempoState(
            presentation = PresentationMode.Child,
            selectedLessonId = demoLessons.first().id,
            completedLessonIds = setOf(demoLessons.first().id),
            demonstratedLessonIds = setOf(demoLessons.first().id),
            savedCardIds = setOf(demoCards.first().id),
        )

        val restored = decodeTempoState(encodeTempoState(source))

        assertEquals(PresentationMode.Child, restored.presentation)
        assertEquals(source.completedLessonIds, restored.completedLessonIds)
        assertEquals(source.demonstratedLessonIds, restored.demonstratedLessonIds)
        assertEquals(source.savedCardIds, restored.savedCardIds)
        assertEquals(null, restored.selectedLessonId)
    }
}
