package io.github.kroha22.tempo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroha22.tempo.model.Lesson
import io.github.kroha22.tempo.model.LessonBlock
import io.github.kroha22.tempo.model.LessonStage
import io.github.kroha22.tempo.model.CardCollection
import io.github.kroha22.tempo.model.PresentationMode
import io.github.kroha22.tempo.model.RootArea
import io.github.kroha22.tempo.model.TempoAction
import io.github.kroha22.tempo.model.TempoState
import io.github.kroha22.tempo.model.cardsById
import io.github.kroha22.tempo.model.demoCards
import io.github.kroha22.tempo.model.demoLessons
import io.github.kroha22.tempo.model.reduce
import io.github.kroha22.tempo.model.verbDeck

@Composable
fun TempoApp(
    initialState: TempoState = TempoState(),
    onStateChanged: (TempoState) -> Unit = {},
) {
    var state by remember { mutableStateOf(initialState) }
    val dispatch: (TempoAction) -> Unit = { action ->
        reduce(state, action).also { next ->
            state = next
            onStateChanged(next)
        }
    }
    val child = state.presentation == PresentationMode.Child

    TempoTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = if (child) ChildPaper else Paper,
            topBar = { TempoHeader(state, dispatch) },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when {
                    state.selectedLessonId != null -> LessonScreen(
                        lesson = demoLessons.first { it.id == state.selectedLessonId },
                        state = state,
                        dispatch = dispatch,
                    )
                    state.area == RootArea.Learning -> LearningScreen(state, dispatch)
                    else -> CardsScreen(state, dispatch)
                }
            }
        }
    }
}

@Composable
private fun TempoHeader(state: TempoState, dispatch: (TempoAction) -> Unit) {
    Surface(shadowElevation = 3.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TEMPO", fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Green)
                Spacer(Modifier.weight(1f))
                ModeChip("Aa", state.presentation == PresentationMode.Adult) { dispatch(TempoAction.SetPresentation(PresentationMode.Adult)) }
                Spacer(Modifier.size(6.dp))
                ModeChip("✦", state.presentation == PresentationMode.Child) { dispatch(TempoAction.SetPresentation(PresentationMode.Child)) }
            }
            if (state.selectedLessonId == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AreaButton("Обучение", state.area == RootArea.Learning) { dispatch(TempoAction.SelectArea(RootArea.Learning)) }
                    AreaButton("Карточки", state.area == RootArea.Cards) { dispatch(TempoAction.SelectArea(RootArea.Cards)) }
                }
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.size(42.dp).clickable(onClick = onClick), shape = CircleShape, color = if (selected) Green else GreenSoft) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) Color.White else Green, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RowScope.AreaButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.weight(1f).height(42.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Green else Color(0xFFF1EEE5),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) Color.White else Ink, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LearningScreen(state: TempoState, dispatch: (TempoAction) -> Unit) {
    val child = state.presentation == PresentationMode.Child
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 22.dp, end = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("ЕВРОПЕЙСКИЙ ПОРТУГАЛЬСКИЙ · A1.1", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text(if (child) "Выбери маленький урок" else "Что изучим сегодня?", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Text(if (child) "Тот же маршрут — в более игровом оформлении." else "Первый маршрут: от знакомства до описания комнаты.", color = Muted)
        }
        item {
            val progress = state.completedLessonIds.size.toFloat() / demoLessons.size
            Card(colors = CardDefaults.cardColors(containerColor = Sun), shape = RoundedCornerShape(26.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("МАРШРУТ", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Я и пространство", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("${state.completedLessonIds.size} из ${demoLessons.size} шагов завершено")
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = Green,
                        trackColor = Color.White.copy(alpha = .65f),
                    )
                }
            }
        }
        item { Text("Уроки", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
        items(demoLessons, key = { it.id }) { lesson ->
            LessonRow(lesson, lesson.id in state.completedLessonIds) { dispatch(TempoAction.OpenLesson(lesson.id)) }
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, completed: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (lesson.isCheckpoint) Color(0xFFFFE7A5) else Color.White),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = CircleShape, color = if (completed) Green else if (lesson.isCheckpoint) Sun else GreenSoft) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(if (completed) "✓" else lesson.number, color = if (completed) Color.White else Green, fontWeight = FontWeight.Black)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(lesson.title, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("${lesson.minutes} мин · ${lesson.summary}", color = Muted, fontSize = 13.sp)
            }
            Text("›", color = Green, fontSize = 26.sp)
        }
    }
}

@Composable
private fun LessonScreen(lesson: Lesson, state: TempoState, dispatch: (TempoAction) -> Unit) {
    val child = state.presentation == PresentationMode.Child
    val step = when (state.lessonStage) { LessonStage.Learn -> 1; LessonStage.Activity -> 2; LessonStage.Summary -> 3 }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { dispatch(TempoAction.CloseLesson) }) { Text("← Маршрут") }
                Spacer(Modifier.weight(1f))
                Text("Шаг $step из 3", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Green,
                trackColor = GreenSoft,
            )
        }
        when (state.lessonStage) {
            LessonStage.Learn -> learnStage(lesson, child, dispatch)
            LessonStage.Activity -> activityStage(lesson, child, state, dispatch)
            LessonStage.Summary -> summaryStage(lesson, child, state, dispatch)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.learnStage(lesson: Lesson, child: Boolean, dispatch: (TempoAction) -> Unit) {
    item {
        Text(if (child) "✦ ДЕТСКИЙ УРОК ${lesson.number}" else if (lesson.isCheckpoint) "ИТОГ МАРШРУТА" else "УРОК ${lesson.number} · ${lesson.minutes} МИН", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text(lesson.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text(lesson.canDo, color = Muted, fontSize = 16.sp)
    }
    lesson.blocks.forEach { block ->
        when (block) {
            is LessonBlock.Meaning -> item {
                Card(colors = CardDefaults.cardColors(containerColor = if (child) Color(0xFFFFE6A3) else GreenSoft), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(block.title.uppercase(), color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(block.body, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            is LessonBlock.Examples -> items(block.items) { example ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(example.portuguese, color = Green, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(example.russian, color = Muted)
                    }
                }
            }
            is LessonBlock.Vocabulary -> item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(block.title, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        block.items.forEach { vocabulary ->
                            Column {
                                Text(vocabulary.portuguese, color = Green, fontWeight = FontWeight.Black)
                                Text(vocabulary.russian, color = Muted)
                                vocabulary.note?.let { Text(it, color = Purple, fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
    item {
        Button(modifier = Modifier.fillMaxWidth().height(52.dp), onClick = { dispatch(TempoAction.ContinueToActivity) }) {
            Text("Перейти к заданию →")
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.activityStage(
    lesson: Lesson,
    child: Boolean,
    state: TempoState,
    dispatch: (TempoAction) -> Unit,
) {
    item {
        Text(if (child) "✦ ЗАДАНИЕ" else "САМОСТОЯТЕЛЬНОЕ ЗАДАНИЕ", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text("Выберите ответ", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
    }
    item {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F2EB)), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ПРОВЕРЬ СЕБЯ", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text(lesson.activity.prompt, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                lesson.activity.options.forEach { option ->
                    FilterChip(
                        modifier = Modifier.fillMaxWidth(),
                        selected = state.selectedAnswerId == option.id,
                        onClick = { dispatch(TempoAction.SelectAnswer(option.id)) },
                        label = { Text(option.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = Ink,
                            selectedContainerColor = Green,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }
        }
    }
    item {
        val demonstrated = state.selectedAnswerId == lesson.activity.correctOptionId
        Button(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = state.selectedAnswerId != null,
            onClick = { dispatch(TempoAction.CompleteLesson(lesson.id, demonstrated)) },
        ) { Text("Проверить и завершить") }
        TextButton(modifier = Modifier.fillMaxWidth(), onClick = { dispatch(TempoAction.RestartLesson) }) { Text("Повторить объяснение") }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.summaryStage(
    lesson: Lesson,
    child: Boolean,
    state: TempoState,
    dispatch: (TempoAction) -> Unit,
) {
    val demonstrated = lesson.id in state.demonstratedLessonIds
    item {
        Text(if (child) "✦ ИТОГ УРОКА" else "ИТОГ УРОКА", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text(if (demonstrated) "Получилось!" else "Урок завершён", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text(if (demonstrated) lesson.canDo else "Сложное место можно повторить — маршрут остаётся открытым.", color = Muted, fontSize = 16.sp)
    }
    if (lesson.checkpointCriteria.isNotEmpty()) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Навыки маршрута", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    lesson.checkpointCriteria.forEach { Text("•  $it", color = Ink) }
                }
            }
        }
    }
    if (lesson.saveItemIds.isNotEmpty()) {
        item {
            Text("Добавить в карточки", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
        items(lesson.saveItemIds.mapNotNull(cardsById::get), key = { it.id }) { card ->
            val saved = card.id in state.savedCardIds
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                onClick = { dispatch(TempoAction.ToggleSavedCard(card.id)) },
            ) { Text(if (saved) "${card.portuguese} · Сохранено ✓" else "+ ${card.portuguese}") }
        }
    }
    item {
        Button(modifier = Modifier.fillMaxWidth().height(52.dp), onClick = { dispatch(TempoAction.CloseLesson) }) { Text("Вернуться к маршруту") }
        TextButton(modifier = Modifier.fillMaxWidth(), onClick = { dispatch(TempoAction.RestartLesson) }) { Text("Пройти урок ещё раз") }
    }
}

@Composable
private fun CardsScreen(state: TempoState, dispatch: (TempoAction) -> Unit) {
    val verbsSelected = state.cardCollection == CardCollection.Verbs
    val savedCards = demoCards.filter { it.id in state.savedCardIds }
    val reviewCards = if (savedCards.isEmpty()) demoCards.take(6) else savedCards
    val lessonCard = reviewCards[state.cardIndex % reviewCards.size]
    val verbCard = verbDeck[state.cardIndex % verbDeck.size]
    val saved = lessonCard.id in state.savedCardIds
    Column(
        modifier = Modifier.fillMaxSize().padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("КАРТОЧКИ", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text(if (verbsSelected) "1000 глаголов" else if (savedCards.isEmpty()) "Попробуйте карточки" else "Мои карточки", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = !verbsSelected,
                onClick = { dispatch(TempoAction.SelectCardCollection(CardCollection.Lesson)) },
                label = { Text("Из уроков") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = Ink,
                    selectedContainerColor = Green,
                    selectedLabelColor = Color.White,
                ),
            )
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = verbsSelected,
                onClick = { dispatch(TempoAction.SelectCardCollection(CardCollection.Verbs)) },
                label = { Text("1000 глаголов") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = Ink,
                    selectedContainerColor = Green,
                    selectedLabelColor = Color.White,
                ),
            )
        }
        Text(
            if (verbsSelected) "№ ${verbCard.rank} из ${verbDeck.size}" else "${state.cardIndex % reviewCards.size + 1} из ${reviewCards.size} · сохранено ${state.savedCardIds.size}",
            color = Muted,
        )
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(320.dp).clickable { dispatch(TempoAction.FlipCard) },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
        ) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (verbsSelected) {
                            if (verbCard.basic) "БАЗОВЫЙ ГЛАГОЛ · ${verbCard.id}" else "ГЛАГОЛ · ${verbCard.id}"
                        } else lessonCard.label.uppercase(),
                        color = Purple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        if (verbsSelected) {
                            if (state.cardRevealed) verbCard.translation else verbCard.infinitive
                        } else {
                            if (state.cardRevealed) lessonCard.russian else lessonCard.portuguese
                        },
                        textAlign = TextAlign.Center,
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (verbsSelected) {
                            if (state.cardRevealed) "№ ${verbCard.rank} по частоте" else "Нажмите, чтобы открыть перевод"
                        } else {
                            if (state.cardRevealed) lessonCard.example else "Нажмите, чтобы открыть перевод"
                        },
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!verbsSelected) {
                OutlinedButton(modifier = Modifier.weight(1f).height(50.dp), onClick = { dispatch(TempoAction.ToggleSavedCard(lessonCard.id)) }) {
                    Text(if (saved) "Сохранено ✓" else "Сохранить")
                }
            }
            Button(modifier = Modifier.weight(1f).height(50.dp), onClick = { dispatch(TempoAction.NextCard) }) { Text("Следующая →") }
        }
    }
}
