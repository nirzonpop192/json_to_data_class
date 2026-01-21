package utils

/* ---------- Helpers ---------- */

fun String.toCamelCase(): String {
    val parts = split("_")
    return parts.first() + parts.drop(1).joinToString("") {
        it.replaceFirstChar { c -> c.uppercase() }
    }
}

fun String.toClassName(): String =
    split("_").joinToString("") {
        it.replaceFirstChar { c -> c.uppercase() }
    }