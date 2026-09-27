package io.github.portfolio.tempo.ui

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import io.github.portfolio.tempo.model.Lesson
import io.github.portfolio.tempo.model.PresentationMode
import io.github.portfolio.tempo.model.RootArea
import io.github.portfolio.tempo.model.TempoAction
import io.github.portfolio.tempo.model.TempoState
import io.github.portfolio.tempo.model.demoCards
import io.github.portfolio.tempo.model.demoLessons
import io.github.portfolio.tempo.model.reduce

@Composable
fun TempoApp() {
    var state by remember { mutableStateOf(TempoState()) }
    val dispatch: (TempoAction) -> Unit = { state = reduce(state, it) }
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
                        child = child,
                        completed = state.selectedLessonId in state.completedLessonIds,
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
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TEMPO", fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Green)
                Spacer(Modifier.weight(1f))
                ModeChip(
                    label = "Aa",
                    selected = state.presentation == PresentationMode.Adult,
                    onClick = { dispatch(TempoAction.SetPresentation(PresentationMode.Adult)) },
                )
                Spacer(Modifier.size(6.dp))
                ModeChip(
                    label = "✦",
                    selected = state.presentation == PresentationMode.Child,
                    onClick = { dispatch(TempoAction.SetPresentation(PresentationMode.Child)) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AreaButton("Обучение", state.area == RootArea.Learning) {
                    dispatch(TempoAction.SelectArea(RootArea.Learning))
                }
                AreaButton("Карточки", state.area == RootArea.Cards) {
                    dispatch(TempoAction.SelectArea(RootArea.Cards))
                }
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(42.dp).clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) Green else GreenSoft,
    ) {
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
            Text(label, color = if (selected) Color.White else Muted, fontWeight = FontWeight.Bold)
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
            Text("ЕВРОПЕЙСКИЙ ПОРТУГАЛЬСКИЙ · A1", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text(
                if (child) "Выбери маленький урок" else "Что изучим сегодня?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                if (child) "Те же задания — в более игровом оформлении." else "Небольшой демонстрационный маршрут для мобильного портфолио.",
                color = Muted,
            )
        }
        item {
            val progress = state.completedLessonIds.size.toFloat() / demoLessons.size
            Card(
                colors = CardDefaults.cardColors(containerColor = Sun),
                shape = RoundedCornerShape(26.dp),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("МАРШРУТ", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Я и пространство", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("${state.completedLessonIds.size} из ${demoLessons.size} уроков завершено")
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = Green,
                        trackColor = Color.White.copy(alpha = .65f),
                    )
                }
            }
        }
        item {
            Text("Уроки", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
        items(demoLessons, key = { it.id }) { lesson ->
            LessonRow(
                lesson = lesson,
                completed = lesson.id in state.completedLessonIds,
                onClick = { dispatch(TempoAction.OpenLesson(lesson.id)) },
            )
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, completed: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = CircleShape, color = if (completed) Green else GreenSoft) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (completed) "✓" else lesson.number,
                        color = if (completed) Color.White else Green,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(lesson.title, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(lesson.summary, color = Muted, fontSize = 13.sp)
            }
            Text("›", color = Green, fontSize = 26.sp)
        }
    }
}

@Composable
private fun LessonScreen(
    lesson: Lesson,
    child: Boolean,
    completed: Boolean,
    dispatch: (TempoAction) -> Unit,
) {
    var selectedOption by remember(lesson.id) { mutableIntStateOf(-1) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            TextButton(onClick = { dispatch(TempoAction.CloseLesson) }) { Text("← К маршруту") }
            Text(
                if (child) "ДЕТСКИЙ УРОК ${lesson.number}" else "УРОК ${lesson.number}",
                color = Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
            Text(lesson.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Text(lesson.canDo, color = Muted, fontSize = 16.sp)
        }
        items(lesson.examples) { example ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(example.portuguese, color = Green, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(example.russian, color = Muted)
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF173F31)), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ПРОВЕРЬ СЕБЯ", color = Sun, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text(lesson.question, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    lesson.options.forEachIndexed { index, option ->
                        FilterChip(
                            selected = selectedOption == index,
                            onClick = { selectedOption = index },
                            label = { Text(option) },
                        )
                    }
                    if (selectedOption >= 0) {
                        Text(
                            if (selectedOption == lesson.correctOption) "Верно — можно завершить урок." else "Попробуй ещё раз.",
                            color = if (selectedOption == lesson.correctOption) Color(0xFFA9E4B8) else Color(0xFFFFC2B6),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = selectedOption == lesson.correctOption,
                onClick = { dispatch(TempoAction.CompleteLesson(lesson.id)) },
            ) {
                Text(if (completed) "Вернуться к маршруту" else "Завершить урок")
            }
        }
    }
}

@Composable
private fun CardsScreen(state: TempoState, dispatch: (TempoAction) -> Unit) {
    val card = demoCards[state.cardIndex]
    val saved = card.id in state.savedCardIds
    Column(
        modifier = Modifier.fillMaxSize().padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("КАРТОЧКИ", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text("Повторим слова", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text("${state.cardIndex + 1} из ${demoCards.size} · сохранено ${state.savedCardIds.size}", color = Muted)
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp).clickable { dispatch(TempoAction.FlipCard) },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
        ) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(card.label.uppercase(), color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text(
                        if (state.cardRevealed) card.russian else card.portuguese,
                        textAlign = TextAlign.Center,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (state.cardRevealed) "Нажми, чтобы увидеть португальский" else "Нажми, чтобы открыть перевод",
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                modifier = Modifier.weight(1f).height(50.dp),
                onClick = { dispatch(TempoAction.ToggleSavedCard(card.id)) },
            ) { Text(if (saved) "Сохранено ✓" else "Сохранить") }
            Button(
                modifier = Modifier.weight(1f).height(50.dp),
                onClick = { dispatch(TempoAction.NextCard) },
            ) { Text("Следующая →") }
        }
    }
}
