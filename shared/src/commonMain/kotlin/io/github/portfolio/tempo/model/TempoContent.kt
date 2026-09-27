package io.github.portfolio.tempo.model

enum class PresentationMode { Adult, Child }

enum class RootArea { Learning, Cards }

data class Example(val portuguese: String, val russian: String)

data class Lesson(
    val id: String,
    val number: String,
    val title: String,
    val summary: String,
    val canDo: String,
    val examples: List<Example>,
    val question: String,
    val options: List<String>,
    val correctOption: Int,
)

data class StudyCard(
    val id: String,
    val portuguese: String,
    val russian: String,
    val label: String,
)

val demoLessons = listOf(
    Lesson(
        id = "space-basics",
        number = "01",
        title = "Я и пространство",
        summary = "Предлоги места и короткие фразы о предметах вокруг.",
        canDo = "Смогу сказать, где находится знакомый предмет.",
        examples = listOf(
            Example("O livro está na mesa.", "Книга лежит на столе."),
            Example("A mochila está debaixo da cadeira.", "Рюкзак находится под стулом."),
        ),
        question = "A chave está ___ caixa.",
        options = listOf("na", "sou", "tem"),
        correctOption = 0,
    ),
    Lesson(
        id = "ser-estar",
        number = "02",
        title = "SER или ESTAR?",
        summary = "Кто это и где он сейчас — два разных вопроса.",
        canDo = "Различу постоянную характеристику и текущее состояние.",
        examples = listOf(
            Example("A Inês é arquiteta.", "Инеш — архитектор."),
            Example("A Inês está em Lisboa.", "Инеш сейчас в Лиссабоне."),
        ),
        question = "Hoje o Rui ___ cansado.",
        options = listOf("é", "está", "há"),
        correctOption = 1,
    ),
    Lesson(
        id = "future-plan",
        number = "03",
        title = "Будущее: ir + infinitivo",
        summary = "Простой способ рассказать о ближайшем плане.",
        canDo = "Соберу короткую фразу о будущем событии.",
        examples = listOf(
            Example("Vou estudar amanhã.", "Я буду учиться завтра."),
            Example("Vamos visitar o Porto.", "Мы посетим Порту."),
        ),
        question = "Nós ___ cozinhar esta noite.",
        options = listOf("vamos", "somos", "estamos"),
        correctOption = 0,
    ),
)

val demoCards = listOf(
    StudyCard("card-casa", "a casa", "дом", "Существительное"),
    StudyCard("card-livro", "o livro", "книга", "Существительное"),
    StudyCard("card-estar", "estar", "быть, находиться", "Глагол"),
    StudyCard("card-fazer", "fazer", "делать", "Глагол"),
    StudyCard("card-amanha", "amanhã", "завтра", "Слово"),
    StudyCard("card-debaixo", "debaixo de", "под", "Выражение"),
)
