package io.github.kroha22.tempo.model

enum class PresentationMode { Adult, Child }

enum class RootArea { Learning, Cards }

data class Example(val portuguese: String, val russian: String)

data class VocabularyItem(
    val entityId: String,
    val portuguese: String,
    val russian: String,
    val note: String? = null,
)

sealed interface LessonBlock {
    data class Meaning(val title: String, val body: String) : LessonBlock
    data class Examples(val items: List<Example>) : LessonBlock
    data class Vocabulary(val title: String, val items: List<VocabularyItem>) : LessonBlock
}

data class AnswerOption(val id: String, val label: String)

data class LessonActivity(
    val id: String,
    val prompt: String,
    val options: List<AnswerOption>,
    val correctOptionId: String,
    val targetId: String,
    val errorCode: String,
)

data class Lesson(
    val id: String,
    val number: String,
    val title: String,
    val summary: String,
    val canDo: String,
    val minutes: Int,
    val blocks: List<LessonBlock>,
    val activity: LessonActivity,
    val saveItemIds: List<String>,
    val checkpointCriteria: List<String> = emptyList(),
) {
    val isCheckpoint: Boolean get() = id.startsWith("checkpoint:")
}

data class StudyCard(
    val id: String,
    val portuguese: String,
    val russian: String,
    val label: String,
    val example: String,
)

private fun meaning(title: String = "Главная идея", body: String) = LessonBlock.Meaning(title, body)
private fun examples(vararg items: Pair<String, String>) = LessonBlock.Examples(items.map { Example(it.first, it.second) })
private fun vocabulary(vararg items: VocabularyItem) = LessonBlock.Vocabulary("Слова и выражения урока", items.toList())
private fun item(id: String, pt: String, ru: String, note: String? = null) = VocabularyItem(id, pt, ru, note)
private fun option(id: String, label: String) = AnswerOption(id, label)

val demoLessons = listOf(
    Lesson(
        id = "lesson:a1-1:introduce-yourself",
        number = "01",
        title = "Представиться",
        summary = "Поздороваться, спросить имя и коротко представиться.",
        canDo = "Я могу поздороваться, спросить имя и коротко представиться.",
        minutes = 6,
        blocks = listOf(
            meaning(body = "Запоминайте вопрос и ответ как готовые фразы. Так знакомство сразу звучит естественно."),
            examples(
                "Como te chamas?" to "Как тебя зовут?",
                "Chamo-me Rita. Muito prazer." to "Меня зовут Рита. Очень приятно.",
            ),
            vocabulary(
                item("study:chunk:ola", "Olá!", "Привет! / Здравствуйте!"),
                item("study:chunk:como-te-chamas", "Como te chamas?", "Как тебя зовут?"),
                item("study:chunk:chamo-me", "Chamo-me…", "Меня зовут…"),
                item("study:chunk:sou-name", "Sou…", "Я…"),
                item("study:chunk:muito-prazer", "Muito prazer.", "Очень приятно."),
            ),
        ),
        activity = LessonActivity(
            id = "exercise:intro:transfer-miguel",
            prompt = "Вы познакомились с Мигелом. Как начать ответ?",
            options = listOf(option("chamo-me", "Chamo-me..."), option("estou", "Estou..."), option("ha", "Há...")),
            correctOptionId = "chamo-me",
            targetId = "chunk:chamo-me",
            errorCode = "INTRO_REQUIRED_CHUNK",
        ),
        saveItemIds = listOf("study:chunk:chamo-me", "study:chunk:como-te-chamas"),
    ),
    Lesson(
        id = "lesson:a1-1:ser-estar-description-state",
        number = "02",
        title = "SER или ESTAR",
        summary = "Назвать или описать — и сказать, как кто-то чувствует себя сейчас.",
        canDo = "Я могу сказать, кто это или какой человек, и описать состояние сейчас.",
        minutes = 10,
        blocks = listOf(
            meaning(body = "SER нужен, чтобы назвать или описать. ESTAR — чтобы сказать, как кто-то себя чувствует сейчас или где находится."),
            examples(
                "O Rui é calmo." to "Руй спокойный по характеру.",
                "O Rui está calmo." to "Сейчас Руй спокоен.",
            ),
            vocabulary(
                item("lex:adjective:calmo", "calmo / calma", "спокойный / спокойная"),
                item("lex:adjective:nervoso", "nervoso / nervosa", "нервный / нервная"),
                item("lex:adjective:cansado", "cansado / cansada", "уставший / уставшая"),
                item("lex:adjective:pronto", "pronto / pronta", "готовый / готовая"),
                item("lex:adjective:pequeno", "pequeno / pequena", "маленький / маленькая"),
                item("lex:adjective:grande", "grande", "большой / большая"),
            ),
        ),
        activity = LessonActivity(
            id = "exercise:ser-estar:independent",
            prompt = "Сегодня Ана устала. Выберите естественную фразу.",
            options = listOf(option("estar", "A Ana está cansada."), option("ser", "A Ana é cansada.")),
            correctOptionId = "estar",
            targetId = "grammar:ser-estar:first-contrast",
            errorCode = "SER_ESTAR_CHOICE",
        ),
        saveItemIds = listOf("study:verb:ser:identity", "study:verb:estar:state"),
    ),
    Lesson(
        id = "lesson:a1-1:name-familiar-objects",
        number = "03",
        title = "Знакомые предметы",
        summary = "Назвать один или несколько предметов с правильным артиклем.",
        canDo = "Я могу назвать один или несколько знакомых предметов с правильным артиклем.",
        minutes = 8,
        blocks = listOf(
            meaning(body = "Учите существительное вместе с артиклем и формой множественного числа — так род и число запоминаются сразу."),
            examples("a janela — as janelas" to "окно — окна", "o livro — os livros" to "книга — книги"),
            vocabulary(
                item("study:noun:mesa", "a mesa — as mesas", "стол — столы"),
                item("study:noun:caixa", "a caixa — as caixas", "коробка — коробки"),
                item("study:noun:porta", "a porta — as portas", "дверь — двери"),
                item("study:noun:janela", "a janela — as janelas", "окно — окна"),
                item("study:noun:gato", "o gato — os gatos", "кот — коты"),
                item("study:noun:chave", "a chave — as chaves", "ключ — ключи"),
                item("study:noun:livro", "o livro — os livros", "книга — книги"),
            ),
        ),
        activity = LessonActivity(
            id = "exercise:nouns:independent",
            prompt = "Как назвать несколько окон?",
            options = listOf(option("as", "as janelas"), option("os", "os janelas"), option("a", "a janelas")),
            correctOptionId = "as",
            targetId = "grammar:noun:number-regular",
            errorCode = "NOUN_ARTICLE_NUMBER",
        ),
        saveItemIds = listOf("study:noun:mesa", "study:noun:caixa", "study:noun:porta", "study:noun:janela", "study:noun:gato", "study:noun:chave", "study:noun:livro"),
    ),
    Lesson(
        id = "lesson:a1-1:locate-object",
        number = "04",
        title = "Где находится предмет",
        summary = "Спросить и сказать, где находится знакомый предмет.",
        canDo = "Я могу спросить и сказать, где находится знакомый предмет.",
        minutes = 9,
        blocks = listOf(
            meaning(body = "Сначала назовите знакомый предмет, затем определите, где он находится: внутри, сверху, под чем-то или рядом."),
            examples("Onde está o gato?" to "Где кот?", "O gato está na caixa." to "Кот в коробке."),
            vocabulary(
                item("study:chunk:dentro-de", "dentro de", "внутри"),
                item("study:chunk:em-cima-de", "em cima de", "на / сверху"),
                item("study:chunk:debaixo-de", "debaixo de", "под"),
                item("study:chunk:ao-lado-de", "ao lado de", "рядом с"),
                item("study:chunk:no-na", "no / na", "в / на + известный предмет", "em + o/a"),
            ),
        ),
        activity = LessonActivity(
            id = "exercise:space:independent",
            prompt = "O gato está debaixo da mesa. Где должен быть кот?",
            options = listOf(option("inside", "в коробке"), option("on", "на столе"), option("under", "под столом")),
            correctOptionId = "under",
            targetId = "pattern:space:debaixo-de",
            errorCode = "SPACE_RELATION",
        ),
        saveItemIds = listOf("study:chunk:dentro-de", "study:chunk:em-cima-de", "study:chunk:debaixo-de", "study:chunk:ao-lado-de"),
    ),
    Lesson(
        id = "lesson:a1-1:possession-and-presence",
        number = "05",
        title = "TER и HÁ",
        summary = "Сказать, что есть у человека или что находится в комнате.",
        canDo = "Я могу сказать, что есть у человека или в комнате.",
        minutes = 9,
        blocks = listOf(
            meaning(body = "TER говорит, что есть у конкретного человека или предмета. HÁ — что вообще есть в комнате. HÁ не меняется во множественном числе."),
            examples("A Rita tem uma chave." to "У Риты есть ключ.", "Há três livros na mesa." to "На столе есть три книги."),
            vocabulary(
                item("study:noun:sala", "a sala — as salas", "комната / гостиная"),
                item("study:noun:cadeira", "a cadeira — as cadeiras", "стул — стулья"),
                item("study:noun:cama", "a cama — as camas", "кровать — кровати"),
                item("study:noun:quarto", "o quarto — os quartos", "спальня — спальни"),
                item("study:noun:armario", "o armário — os armários", "шкаф — шкафы"),
                item("study:noun:copo", "o copo — os copos", "стакан — стаканы"),
            ),
        ),
        activity = LessonActivity(
            id = "exercise:ter-ha:independent",
            prompt = "В комнате два стула, но мы не говорим, чьи они. Какая фраза подходит?",
            options = listOf(option("ha", "Há duas cadeiras na sala."), option("tem", "Tem duas cadeiras na sala.")),
            correctOptionId = "ha",
            targetId = "grammar:ha:existence-entry",
            errorCode = "REQUIRED_PATTERN_HA",
        ),
        saveItemIds = listOf("study:verb:ter:possession", "study:chunk:ha-presence", "study:noun:sala", "study:noun:cadeira", "study:noun:cama", "study:noun:quarto", "study:noun:armario", "study:noun:copo"),
    ),
    Lesson(
        id = "lesson:a1-1:ask-place-location",
        number = "06",
        title = "Где расположено место",
        summary = "Спросить и сказать, где находится знакомое место.",
        canDo = "Я могу спросить и сказать, где находится знакомое место.",
        minutes = 7,
        blocks = listOf(
            meaning(body = "Чтобы спросить, где находится место, используем Onde fica…? Здесь FICAR означает только расположение места."),
            examples("Onde fica a estação?" to "Где находится станция?", "A estação fica no centro." to "Станция находится в центре."),
            vocabulary(
                item("study:noun:estacao", "a estação — as estações", "станция — станции"),
                item("study:noun:hotel", "o hotel — os hotéis", "отель — отели"),
                item("study:noun:farmacia", "a farmácia — as farmácias", "аптека — аптеки"),
                item("study:noun:centro", "o centro — os centros", "центр — центры"),
            ),
        ),
        activity = LessonActivity(
            id = "exercise:ficar:independent",
            prompt = "Как спросить, где находится аптека?",
            options = listOf(option("fica", "Onde fica a farmácia?"), option("esta", "Onde está a farmácia agora?"), option("ha", "Há a farmácia?")),
            correctOptionId = "fica",
            targetId = "grammar:ficar:location-entry",
            errorCode = "FICAR_SENSE",
        ),
        saveItemIds = listOf("study:verb:ficar:location", "study:chunk:onde-fica", "study:noun:estacao", "study:noun:hotel", "study:noun:farmacia", "study:noun:centro"),
    ),
    Lesson(
        id = "checkpoint:a1-1:new-room",
        number = "✓",
        title = "Новая комната",
        summary = "Итоговая проверка без новых слов и правил.",
        canDo = "Я могу представиться и описать новую комнату: кто в ней, что в ней есть и где находятся предметы.",
        minutes = 8,
        blocks = listOf(
            meaning("Итоговая проверка", "Здесь нет новых слов и правил. Результат показывает, что уже получается и к чему стоит вернуться."),
            examples("Olá! Chamo-me Leonor. Há uma mesa na sala." to "Привет! Меня зовут Леонор. В комнате есть стол."),
        ),
        activity = LessonActivity(
            id = "exercise:checkpoint:new-room",
            prompt = "Кот сидит под столом. Какая фраза точно описывает сцену?",
            options = listOf(option("under", "O gato está debaixo da mesa."), option("inside", "O gato está na caixa."), option("place", "O gato fica no centro.")),
            correctOptionId = "under",
            targetId = "pattern:space:debaixo-de",
            errorCode = "CHECKPOINT_SPACE",
        ),
        saveItemIds = emptyList(),
        checkpointCriteria = listOf("Представиться", "Различать SER и ESTAR", "Назвать предметы", "Различать TER и HÁ", "Сказать, где предмет", "Спросить, где находится место"),
    ),
)

private val coreCards = listOf(
    StudyCard("study:chunk:ola", "Olá!", "Привет! / Здравствуйте!", "Выражение", "Olá! Chamo-me Rita."),
    StudyCard("study:chunk:como-te-chamas", "Como te chamas?", "Как тебя зовут?", "Выражение", "Como te chamas? — Chamo-me Rita."),
    StudyCard("study:chunk:chamo-me", "Chamo-me…", "Меня зовут…", "Выражение", "Chamo-me Rita."),
    StudyCard("study:chunk:sou-name", "Sou…", "Я…", "Выражение", "Sou o Miguel."),
    StudyCard("study:chunk:muito-prazer", "Muito prazer.", "Очень приятно.", "Выражение", "Muito prazer."),
    StudyCard("study:verb:ser:identity", "ser", "быть: идентичность или характеристика", "Глагол", "O Rui é calmo."),
    StudyCard("study:verb:estar:state", "estar", "быть: состояние сейчас", "Глагол", "O Rui está calmo."),
    StudyCard("study:verb:ter:possession", "ter", "иметь", "Глагол", "A Rita tem uma chave."),
    StudyCard("study:chunk:ha-presence", "Há uma…", "Есть…", "Выражение", "Há uma mesa na sala."),
    StudyCard("study:verb:ficar:location", "ficar", "находиться, быть расположенным", "Глагол", "A estação fica no centro."),
    StudyCard("study:chunk:onde-fica", "Onde fica…?", "Где расположено…?", "Выражение", "Onde fica a estação?"),
    StudyCard("study:chunk:dentro-de", "dentro de", "внутри", "Выражение", "O gato está dentro da caixa."),
    StudyCard("study:chunk:em-cima-de", "em cima de", "на / сверху", "Выражение", "O livro está em cima da mesa."),
    StudyCard("study:chunk:debaixo-de", "debaixo de", "под", "Выражение", "O gato está debaixo da mesa."),
    StudyCard("study:chunk:ao-lado-de", "ao lado de", "рядом с", "Выражение", "A cadeira está ao lado da mesa."),
)

private val nounCards = listOf(
    arrayOf("mesa", "a mesa — as mesas", "стол", "Há uma mesa na sala."),
    arrayOf("caixa", "a caixa — as caixas", "коробка", "O gato está na caixa."),
    arrayOf("porta", "a porta — as portas", "дверь", "A porta está aberta."),
    arrayOf("janela", "a janela — as janelas", "окно", "A janela está aberta."),
    arrayOf("gato", "o gato — os gatos", "кот", "O gato está debaixo da mesa."),
    arrayOf("chave", "a chave — as chaves", "ключ", "A Rita tem uma chave."),
    arrayOf("livro", "o livro — os livros", "книга", "Há três livros na mesa."),
    arrayOf("sala", "a sala — as salas", "комната / гостиная", "Há uma mesa na sala."),
    arrayOf("cadeira", "a cadeira — as cadeiras", "стул", "Há duas cadeiras na sala."),
    arrayOf("cama", "a cama — as camas", "кровать", "Há uma cama no quarto."),
    arrayOf("quarto", "o quarto — os quartos", "спальня", "Há uma cama no quarto."),
    arrayOf("armario", "o armário — os armários", "шкаф", "O armário está no quarto."),
    arrayOf("copo", "o copo — os copos", "стакан", "O copo está na mesa."),
    arrayOf("estacao", "a estação — as estações", "станция", "A estação fica no centro."),
    arrayOf("hotel", "o hotel — os hotéis", "отель", "O hotel fica no centro."),
    arrayOf("farmacia", "a farmácia — as farmácias", "аптека", "Onde fica a farmácia?"),
    arrayOf("centro", "o centro — os centros", "центр", "A estação fica no centro."),
).map { (slug, pt, ru, example) -> StudyCard("study:noun:$slug", pt, ru, "Существительное", example) }

val demoCards = coreCards + nounCards
val cardsById = demoCards.associateBy { it.id }
