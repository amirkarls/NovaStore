package com.novastore.app.data

data class AppInfo(
    val name: String,
    val packageName: String,
    val description: String,
    val descriptionRu: String,
    val descriptionUk: String = description,
    val descriptionKk: String = description,
    val descriptionEs: String = description,
    val github: String? = null,
    val category: String,
    val iconUrl: String,
    val apkUrl: String? = null,
    val fdroid: Boolean = false
) {
    fun description(lang: String): String = when (lang) {
        "ru" -> descriptionRu.ifBlank { description }
        "uk" -> descriptionUk.ifBlank { description }
        "kk" -> descriptionKk.ifBlank { description }
        "es" -> descriptionEs.ifBlank { description }
        else -> description
    }

    fun category(lang: String): String = when (lang) {
        "ru" -> mapCategoryRu(category)
        "uk" -> mapCategoryUk(category)
        "kk" -> mapCategoryKk(category)
        "es" -> mapCategoryEs(category)
        else -> category
    }
}

private fun mapCategoryRu(cat: String): String = when (cat) {
    "Shizuku" -> "Shizuku"
    "Games" -> "Игры"
    "Media" -> "Медиа"
    "Security" -> "Безопасность"
    "Development" -> "Разработка"
    "Stores" -> "Магазины"
    "Communication" -> "Связь"
    "Navigation" -> "Навигация"
    "Tools" -> "Утилиты"
    "Browsers" -> "Браузеры"
    "Education" -> "Обучение"
    "Productivity" -> "Продуктивность"
    else -> cat
}

private fun mapCategoryUk(cat: String): String = when (cat) {
    "Games" -> "Ігри"
    "Media" -> "Медіа"
    "Security" -> "Безпека"
    "Development" -> "Розробка"
    "Stores" -> "Магазини"
    "Communication" -> "Зв'язок"
    "Navigation" -> "Навігація"
    "Tools" -> "Утиліти"
    "Browsers" -> "Браузери"
    "Education" -> "Навчання"
    "Productivity" -> "Продуктивність"
    else -> mapCategoryRu(cat)
}

private fun mapCategoryKk(cat: String): String = when (cat) {
    "Games" -> "Ойындар"
    "Media" -> "Медиа"
    "Security" -> "Қауіпсіздік"
    "Development" -> "Әзірлеу"
    "Stores" -> "Дүкендер"
    "Communication" -> "Байланыс"
    "Navigation" -> "Навигация"
    "Tools" -> "Құралдар"
    "Browsers" -> "Браузерлер"
    "Education" -> "Оқу"
    "Productivity" -> "Өнімділік"
    else -> mapCategoryRu(cat)
}

private fun mapCategoryEs(cat: String): String = when (cat) {
    "Games" -> "Juegos"
    "Media" -> "Multimedia"
    "Security" -> "Seguridad"
    "Development" -> "Desarrollo"
    "Stores" -> "Tiendas"
    "Communication" -> "Comunicación"
    "Navigation" -> "Navegación"
    "Tools" -> "Herramientas"
    "Browsers" -> "Navegadores"
    "Education" -> "Educación"
    "Productivity" -> "Productividad"
    else -> cat
}
