package io.github.kroha22.tempo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroha22.tempo.model.*
import kotlin.time.Clock

@Composable
internal fun NativeCardPanel(word: String, revealed: Boolean, example: String, glass: Boolean = false, onFlip: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        CardArrow("Предыдущая карточка", "‹", onPrevious)
        val colors = if (glass) listOf(Color(0xCCD0EFE5), Color(0xBBD9E9F4), Color(0xCCE6DEFA)) else if (revealed) listOf(Color(0xFFA8E4C9), Color(0xFF68C7A7)) else listOf(Color(0xFFFFD064), Color(0xFFFFB937))
        Box(Modifier.weight(1f).height(175.dp).background(Brush.linearGradient(colors), RoundedCornerShape(22.dp)).border(1.dp, Color.White.copy(alpha = .7f), RoundedCornerShape(22.dp)).clickable(role = Role.Button, onClick = onFlip).semantics { contentDescription = "Перевернуть карточку" }.padding(12.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (revealed) "Русский" else "Português", color = Muted, fontSize = 11.sp)
                Text(word, textAlign = TextAlign.Center, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(if (revealed && example.isNotEmpty()) example else "☝ Нажми, чтобы перевернуть", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
        CardArrow("Следующая карточка", "›", onNext)
    }
}

@Composable
internal fun CardArrow(label: String, symbol: String, action: () -> Unit) {
    Surface(Modifier.width(44.dp).height(175.dp).clickable(role = Role.Button, onClick = action).semantics { contentDescription = label }, shape = RoundedCornerShape(16.dp), color = Color(0x80D9EEEA)) {
        Box(contentAlignment = Alignment.Center) { Text(symbol, color = Color(0xFF008B99), fontSize = 32.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
internal fun RatingButtons(intervals: List<String> = emptyList(), onRate: (Int) -> Unit) {
    Text("Насколько хорошо вспомнилось?", Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp), color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
    val colors = listOf(Color(0xFFFFD7CF), Color(0xFFFFE8B0), Color(0xFFCCEBDC), Color(0xFFCFE8F5))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("Не знаю", "Сложно", "Помню", "Легко").forEachIndexed { grade, label ->
            Button(onClick = { onRate(grade) }, modifier = Modifier.weight(1f).heightIn(min = 44.dp), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(4.dp), colors = ButtonDefaults.buttonColors(containerColor = colors[grade], contentColor = Ink)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); intervals.getOrNull(grade)?.let { Text(it, fontSize = 10.sp) } }
            }
        }
    }
}

@Composable
internal fun CardsScreen(state: TempoState, dispatch: (TempoAction) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val titles = mapOf(CardCollection.Lesson to "Мои карточки", CardCollection.Verbs to "1 000 глаголов", CardCollection.General to "351 базовое слово", CardCollection.School to "Инструкции на уроке")
    val ids = deckIds(state)
    val id = ids[state.cardIndex % ids.size]
    val word = when (state.cardCollection) {
        CardCollection.Verbs -> verbDeck.first { it.id == id }.let { NativeWord(it.id, it.infinitive, it.translation) }
        CardCollection.General -> nativeGeneralCards.first { it.id == id }
        CardCollection.School -> nativeSchoolCards.first { it.id == id }
        CardCollection.Lesson -> cardsById.getValue(id).let { NativeWord(it.id, it.portuguese, it.russian, it.example) }
    }
    val now = Clock.System.now().toEpochMilliseconds()
    val reviews = state.cardReviews.filterKeys { it in ids }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titles.getValue(state.cardCollection), Modifier.weight(1f), fontSize = 23.sp, fontWeight = FontWeight.Black)
                Box { TextButton(onClick = { menu = true }) { Text("Колода ▾") }; DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { titles.forEach { (collection, title) -> DropdownMenuItem(text = { Text(title) }, onClick = { menu = false; dispatch(TempoAction.SelectCardCollection(collection)) }) } } }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("повторить" to reviews.values.count { it.due <= now }, "встречалось" to reviews.size, "запомнено" to reviews.values.count { it.grade >= 2 }).forEach { (label, count) -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(count.toString(), color = Green, fontWeight = FontWeight.Black, fontSize = 18.sp); Text(label, fontSize = 10.sp, color = Muted) } }
            }
        }
        item {
            Text("${state.cardIndex % ids.size + 1} / ${ids.size}", color = Muted, fontSize = 12.sp)
            NativeCardPanel(if (state.cardRevealed) word.russian else word.portuguese, state.cardRevealed, word.example.ifEmpty { word.portuguese }, onFlip = { dispatch(TempoAction.FlipCard) }, onPrevious = { dispatch(TempoAction.PreviousCard) }, onNext = { dispatch(TempoAction.NextCard) })
            if (state.cardRevealed) {
                val verb = verbDeck.find { it.id == id }
                val intervals = if (state.cardCollection == CardCollection.Verbs) (0..3).map { intervalText(reviewInterval(reviews[id], it, verb?.let { v -> v.basic || v.rank <= 40 } == true)) } else emptyList()
                RatingButtons(intervals) { dispatch(TempoAction.RateCard(id, it, Clock.System.now().toEpochMilliseconds())) }
            }
            if (state.cardCollection == CardCollection.Lesson) TextButton(onClick = { dispatch(TempoAction.ToggleSavedCard(id)) }) { Text(if (id in state.savedCardIds) "Сохранено ✓" else "Сохранить") }
        }
    }
}
