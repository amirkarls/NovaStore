package com.novastore.app.data

data class AppInfo(
    val name: String,
    val packageName: String,
    val description: String,
    val descriptionRu: String = "",
    val descriptionUk: String = "",
    val descriptionKk: String = "",
    val descriptionEs: String = "",
    val author: String = "",
    val github: String? = null,
    val category: String,
    val iconUrl: String,
    val apkUrl: String? = null,
    val fdroid: Boolean = false,
    val rating: Float = 0f
) {
    fun description(lang: String): String = when (lang) {
        "ru" -> descriptionRu.ifBlank { description }
        "uk" -> descriptionUk.ifBlank { description }
        "kk" -> descriptionKk.ifBlank { description }
        "es" -> descriptionEs.ifBlank { description }
        else -> description
    }
}

fun categoryLabel(lang: String, cat: String): String = when (cat) {
    "all" -> when (lang) {
        "ru" -> "Все"
        "uk" -> "Усі"
        "kk" -> "Барлығы"
        "es" -> "Todas"
        else -> "All"
    }
    "Shizuku" -> "Shizuku"
    "Games" -> when (lang) {
        "ru" -> "Игры"
        "uk" -> "Ігри"
        "kk" -> "Ойындар"
        "es" -> "Juegos"
        else -> "Games"
    }
    "Media" -> when (lang) {
        "ru" -> "Медиа"
        "uk" -> "Медіа"
        "kk" -> "Медиа"
        "es" -> "Multimedia"
        else -> "Media"
    }
    "Security" -> when (lang) {
        "ru" -> "Безопасность"
        "uk" -> "Безпека"
        "kk" -> "Қауіпсіздік"
        "es" -> "Seguridad"
        else -> "Security"
    }
    "Development" -> when (lang) {
        "ru" -> "Разработка"
        "uk" -> "Розробка"
        "kk" -> "Әзірлеу"
        "es" -> "Desarrollo"
        else -> "Development"
    }
    "Stores" -> when (lang) {
        "ru" -> "Магазины"
        "uk" -> "Магазини"
        "kk" -> "Дүкендер"
        "es" -> "Tiendas"
        else -> "Stores"
    }
    "Communication" -> when (lang) {
        "ru" -> "Связь"
        "uk" -> "Зв'язок"
        "kk" -> "Байланыс"
        "es" -> "Comunicación"
        else -> "Communication"
    }
    "Navigation" -> when (lang) {
        "ru" -> "Навигация"
        "uk" -> "Навігація"
        "kk" -> "Навигация"
        "es" -> "Navegación"
        else -> "Navigation"
    }
    "Tools" -> when (lang) {
        "ru" -> "Утилиты"
        "uk" -> "Утиліти"
        "kk" -> "Құралдар"
        "es" -> "Herramientas"
        else -> "Tools"
    }
    "Browsers" -> when (lang) {
        "ru" -> "Браузеры"
        "uk" -> "Браузери"
        "kk" -> "Браузерлер"
        "es" -> "Navegadores"
        else -> "Browsers"
    }
    "Education" -> when (lang) {
        "ru" -> "Обучение"
        "uk" -> "Навчання"
        "kk" -> "Оқу"
        "es" -> "Educación"
        else -> "Education"
    }
    "Productivity" -> when (lang) {
        "ru" -> "Продуктивность"
        "uk" -> "Продуктивність"
        "kk" -> "Өнімділік"
        "es" -> "Productividad"
        else -> "Productivity"
    }
    else -> cat
}
