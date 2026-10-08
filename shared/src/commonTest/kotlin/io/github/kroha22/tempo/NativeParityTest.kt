package io.github.kroha22.tempo

import io.github.kroha22.tempo.model.*
import kotlin.test.*

class NativeParityTest {
    @Test fun importedCatalogPreservesIdsAndCollectionReferences() {
        assertEquals(351, nativeGeneralCards.size)
        assertEquals(351, nativeGeneralCards.map { it.id }.toSet().size)
        assertEquals(8, nativeSchoolCards.size)
        assertTrue(nativeSchoolCards.all { card -> verbDeck.any { it.id == card.id } })
        val ids = nativeWords.map { it.id }.toSet()
        assertTrue(nativeTopics.flatMap { it.sets }.flatMap { it.wordIds }.all { it in ids })
        assertEquals(14, nativeTopics.size)
    }
    @Test fun phraseAnswersAndRelatedDestinationsAreCanonical() {
        val usage = nativeUsages.single()
        assertEquals("Para ler, abro o livro.", usage.exercises.first().let { it.before + it.options.first { option -> option.id == it.correctOptionId }.label + it.after })
        assertTrue(usage.exercises.all { exercise -> exercise.options.count { it.id == exercise.correctOptionId } == 1 })
        assertEquals(setOf("lesson:a1-1:locate-object", "lesson:a1-1:possession-and-presence"), nativeRelatedLessons.map { it.id }.toSet())
        assertEquals(listOf("estou", "estás", "está", "estamos", "estão", "estão"), nativeVerbForms.first { it.key == "estar" }.forms)
    }
    @Test fun gradingChangesTheNextIntervalAndBasicPriority() {
        assertEquals(1.0 / 1440, reviewInterval(null, 0, false))
        assertEquals(4.0, reviewInterval(null, 3, false))
        assertEquals(7.2, reviewInterval(null, 3, true))
        val again = scheduleCard("v0001", null, 0, 1000)
        val easy = scheduleCard("v0001", null, 3, 1000)
        assertTrue(again.due < easy.due)
        assertEquals(1, again.lapses)
    }
    @Test fun ratingsRoundTripWithoutLosingExistingLearningProgress() {
        val card = nativeGeneralCards.first()
        val source = TempoState(cardCollection = CardCollection.General, cardRevealed = true, completedLessonIds = setOf(demoLessons.first().id), savedCardIds = setOf(demoCards.first().id))
        val rated = reduce(source, TempoAction.RateCard(card.id, 2, 1000))
        assertFalse(rated.cardRevealed)
        assertEquals(1, rated.cardIndex)
        val restored = decodeTempoState(encodeTempoState(rated))
        assertEquals(rated.cardReviews, restored.cardReviews)
        assertEquals(source.completedLessonIds, restored.completedLessonIds)
        assertEquals(source.savedCardIds, restored.savedCardIds)
        assertEquals(TempoState(), decodeTempoState("v1\nAdult\n\n\n"))
    }
    @Test fun gradesRejectHiddenOrMismatchedCardsAndNavigationWraps() {
        val state = TempoState(cardCollection = CardCollection.School)
        assertEquals(state, reduce(state, TempoAction.RateCard(nativeSchoolCards.first().id, 3, 1000)))
        assertEquals(state.copy(cardIndex = 7), reduce(state, TempoAction.PreviousCard))
        assertEquals(state, reduce(state.copy(cardIndex = 7, cardRevealed = true), TempoAction.NextCard))
    }
}
