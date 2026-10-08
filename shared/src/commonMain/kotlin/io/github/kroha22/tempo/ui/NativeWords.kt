package io.github.kroha22.tempo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kroha22.tempo.model.*

@Composable
internal fun WordsScreen() {
    var topicId by remember { mutableStateOf<String?>(null) }
    var setId by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf("Карточки") }
    var index by remember { mutableStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(0) }
    var answer by remember { mutableStateOf<String?>(null) }
    var checked by remember { mutableStateOf(false) }
    var related by remember { mutableStateOf<String?>(null) }
    var setMenu by remember { mutableStateOf(false) }
    val topic = nativeTopics.find { it.id == topicId }
    if (related != null) {
        if (related == "forms") NativeFormsScreen { related = null }
        else RelatedNativeLesson(nativeRelatedLessons.first { it.id == related }) { related = null }
        return
    }
    if (topic == null) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Слова", fontSize = 26.sp, fontWeight = FontWeight.Black); Text("Выбери тему", color = Muted) }
            items(nativeTopics, key = { it.id }) { t ->
                OutlinedButton(onClick = { topicId = t.id; setId = null; index = 0; flipped = false; mode = "Карточки"; step = 0; answer = null; checked = false }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                    Column(Modifier.fillMaxWidth()) { Text(t.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("${t.sets.flatMap { it.wordIds }.distinct().size} слов · ${t.sets.first().title}", color = Muted, fontSize = 12.sp) }
                }
            }
        }
        return
    }
    val sets = topic.sets.filter { setId == null || it.id == setId }
    val ids = sets.flatMap { it.wordIds }.toSet()
    val words = nativeWords.filter { it.id in ids }
    val usage = nativeUsages.firstOrNull { it.collectionIds.any { id -> sets.any { it.id == id } } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { topicId = null }) { Text("‹", fontSize = 30.sp, color = androidx.compose.ui.graphics.Color(0xFF008B99)) }
                Column(Modifier.weight(1f)) { Text("Слова", fontSize = 11.sp, color = Green); Text(topic.title, fontSize = 24.sp, fontWeight = FontWeight.Black) }
                Text("${words.size} слов", fontSize = 12.sp, color = Green)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Набор слов", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box(Modifier.weight(1f)) {
                    TextButton(onClick = { setMenu = true }) { Text((topic.sets.find { it.id == setId }?.title ?: "Все наборы темы") + " ▾") }
                    DropdownMenu(expanded = setMenu, onDismissRequest = { setMenu = false }) {
                        DropdownMenuItem(text = { Text("Все наборы темы") }, onClick = { setId = null; setMenu = false; index = 0; flipped = false; mode = "Карточки" })
                        topic.sets.forEach { s -> DropdownMenuItem(text = { Text("${s.title} · ${s.wordIds.size}") }, onClick = { setId = s.id; setMenu = false; index = 0; flipped = false; mode = "Карточки"; step = 0; answer = null; checked = false }) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOfNotNull("Карточки", "Пары", if (usage != null) "Фразы" else null).forEach { value -> FilterChip(selected = mode == value, onClick = { mode = value }, label = { Text(value) }) }
            }
        }
        if (mode == "Карточки" && words.isNotEmpty()) item {
            val word = words[index % words.size]
            Text("${index + 1} / ${words.size}", color = Muted, fontSize = 12.sp)
            NativeCardPanel(if (flipped) word.russian else word.portuguese, flipped, "", glass = true, onFlip = { flipped = !flipped }, onPrevious = { index = (index - 1 + words.size) % words.size; flipped = false }, onNext = { index = (index + 1) % words.size; flipped = false })
        }
        if (mode == "Пары") item { NativePairs(words, setId ?: topic.id) }
        if (mode == "Фразы" && usage != null) {
            item { Text(usage.title, fontSize = 22.sp, fontWeight = FontWeight.Black); Text(usage.situation, color = Muted, fontSize = 14.sp) }
            items(usage.examples) { example -> ExamplePanel(example) }
            item {
                if (step >= usage.exercises.size) {
                    Text("Все примеры пройдены", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    TextButton(onClick = { step = 0; checked = false; answer = null }) { Text("Повторить фразы") }
                } else {
                    val exercise = usage.exercises[step]
                    Text("Попробуй сам · ${step + 1} / ${usage.exercises.size}", fontWeight = FontWeight.Bold)
                    Text(exercise.prompt)
                    Text(exercise.before + (exercise.options.find { it.id == answer }?.label ?: "…") + exercise.after, color = Green, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    exercise.options.forEach { option -> FilterChip(selected = answer == option.id, enabled = !checked, onClick = { answer = option.id }, label = { Text(option.label) }, modifier = Modifier.fillMaxWidth()) }
                    if (!checked) Button(enabled = answer != null, onClick = { checked = true }) { Text("Проверить") }
                    else {
                        Text(if (answer == exercise.correctOptionId) "Верно" else "Посмотри на правильный вариант", color = Green, fontWeight = FontWeight.Bold)
                        Text(exercise.feedback)
                        Button(onClick = { step++; answer = null; checked = false }) { Text(if (step + 1 == usage.exercises.size) "Завершить" else "Следующая фраза →") }
                    }
                }
            }
            item {
                Text("Связано с этой темой", fontWeight = FontWeight.Bold)
                nativeRelatedLessons.forEach { lesson -> TextButton(onClick = { related = lesson.id }) { Text(if (lesson.id.endsWith("possession-and-presence")) "У меня есть / здесь есть →" else "Где находится предмет? →") } }
                TextButton(onClick = { related = "forms" }) { Text("Формы настоящего времени →") }
            }
        }
    }
}

@Composable
private fun ExamplePanel(example: Example) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = .8f))) {
        Column(Modifier.padding(12.dp)) { Text(example.portuguese, fontWeight = FontWeight.Bold, color = Green, fontSize = 17.sp); Text(example.russian, fontSize = 13.sp, color = Muted) }
    }
}

@Composable
private fun RelatedNativeLesson(lesson: NativeRelatedLesson, onBack: () -> Unit) {
    var answer by remember(lesson.id) { mutableStateOf<String?>(null) }
    var checked by remember(lesson.id) { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { TextButton(onClick = onBack) { Text("‹ Вернуться к фразам") }; Text(lesson.title, fontSize = 26.sp, fontWeight = FontWeight.Black); Text(lesson.cue) }
        items(lesson.examples) { ExamplePanel(it) }
        items(lesson.vocabulary) { ExamplePanel(it) }
        item {
            Text("Попробуй сам", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(lesson.activity.prompt)
            lesson.activity.options.forEach { option -> FilterChip(selected = answer == option.id, enabled = !checked, onClick = { answer = option.id }, label = { Text(option.label) }, modifier = Modifier.fillMaxWidth()) }
            if (!checked) Button(enabled = answer != null, onClick = { checked = true }) { Text("Проверить") }
            else { Text(if (answer == lesson.activity.correctOptionId) "Верно" else "Правильный вариант: ${lesson.activity.options.first { it.id == lesson.activity.correctOptionId }.label}", color = Green, fontWeight = FontWeight.Bold); TextButton(onClick = { checked = false; answer = null }) { Text("Попробовать ещё раз") } }
        }
    }
}

@Composable
private fun NativeFormsScreen(onBack: () -> Unit) {
    var verbId by remember { mutableStateOf("estar") }
    var menu by remember { mutableStateOf(false) }
    var practice by remember { mutableStateOf(false) }
    var person by remember { mutableStateOf(0) }
    var answer by remember { mutableStateOf<String?>(null) }
    val verb = nativeVerbForms.first { it.key == verbId }
    val people = listOf("Eu", "Tu", "Ele / ela", "Nós", "Vocês", "Eles / elas")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            TextButton(onClick = onBack) { Text("‹ Вернуться к фразам") }
            Text("Формы настоящего времени", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Box { TextButton(onClick = { menu = true }) { Text("${verb.title} · ${verb.translation} ▾") }; DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { nativeVerbForms.forEach { v -> DropdownMenuItem(text = { Text(v.title) }, onClick = { verbId = v.key; menu = false; answer = null; person = 0 }) } } }
            Row { FilterChip(selected = !practice, onClick = { practice = false }, label = { Text("Изучать") }); FilterChip(selected = practice, onClick = { practice = true; answer = null }, label = { Text("Практика") }) }
        }
        if (!practice) items(people.indices.toList()) { index -> Text("${people[index]} · ${verb.forms[index]}", fontSize = 22.sp, color = Green, fontWeight = FontWeight.Bold) }
        else item {
            Text("${people[person]} …", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            verb.forms.distinct().forEach { form -> OutlinedButton(onClick = { answer = form }, enabled = answer == null, modifier = Modifier.fillMaxWidth()) { Text(form) } }
            if (answer != null) { Text(if (answer == verb.forms[person]) "Верно" else "Правильно: ${verb.forms[person]}", color = Green, fontWeight = FontWeight.Bold); Button(onClick = { person = (person + 1) % people.size; answer = null }) { Text("Следующая форма →") } }
        }
    }
}

@Composable
private fun NativePairs(words: List<NativeWord>, sessionId: String) {
    var left by remember(sessionId) { mutableStateOf<String?>(null) }
    var matched by remember(sessionId) { mutableStateOf(emptySet<String>()) }
    var feedback by remember(sessionId) { mutableStateOf("") }
    var offset by remember(sessionId) { mutableStateOf(0) }
    val page = words.drop(offset).take(6)
    Column {
        Text("Соедини слово и перевод", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(Modifier.weight(1f)) { page.forEach { word -> OutlinedButton(onClick = { left = word.id; feedback = "" }, enabled = word.id !in matched, modifier = Modifier.fillMaxWidth()) { Text(word.portuguese) } } }
            Column(Modifier.weight(1f)) { page.reversed().forEach { word -> OutlinedButton(onClick = { if (left == word.id) { matched = matched + word.id; left = null; feedback = "Верно" } else feedback = "Попробуй другую пару" }, enabled = left != null && word.id !in matched, modifier = Modifier.fillMaxWidth()) { Text(word.russian) } } }
        }
        Text(feedback, color = Green)
        if (matched.size == page.size) TextButton(onClick = { offset = if (offset + 6 < words.size) offset + 6 else 0; matched = emptySet(); left = null; feedback = "" }) { Text(if (offset + 6 < words.size) "Следующие пары →" else "Повторить пары") }
    }
}
