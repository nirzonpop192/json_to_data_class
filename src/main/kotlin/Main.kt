import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun generateDataClasses(
    json: JSONObject,
    className: String,
    classes: MutableList<String>
) {
    val builder = StringBuilder()
    builder.append("data class $className(\n")

    val fields = mutableListOf<String>()
    val keys = json.keys()

    while (keys.hasNext()) {
        val originalKey = keys.next()
        val camelKey = originalKey.toCamelCase()
        val value = json.get(originalKey)

        val type = when (value) {
            JSONObject.NULL -> "Any?"
            is Int -> "Int?"
            is Long -> "Long?"
            is Double -> "Double?"
            is Boolean -> "Boolean?"
            is String -> "String?"

            is JSONObject -> {
                val nestedClassName = originalKey.toClassName()
                generateDataClasses(value, nestedClassName, classes)
                nestedClassName
            }

            is JSONArray -> {
                if (value.length() > 0 && value.get(0) is JSONObject) {
                    val nestedClassName = originalKey.toClassName().removeSuffix("s")
                    generateDataClasses(value.getJSONObject(0), nestedClassName, classes)
                    "List<$nestedClassName>"
                } else {
                    "List<Any>"
                }
            }

            else -> "Any"
        }

        val field = if (camelKey != originalKey) {
            """    @Expose 
    @SerializedName("$originalKey")
    val $camelKey: $type""".trimMargin()
        } else {
            """    @Expose   
    @SerializedName("$camelKey")   
    val $camelKey: $type""".trimMargin()
        }

        fields.add(field)
    }

    builder.append(fields.joinToString(",\n\n"))
    builder.append("\n)")

    classes.add(builder.toString())
}

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

fun main() {
    val jsonString = """ {
    "success": true,
    "message": null,
    "status": 200,
    "data": {
        "topics": [
            {
                "id": 1,
                "slug": "sdfdsfsd",
                "title_en": "Dhaka bus",
                "title_bn": "ঢাকা বাস",
                "banner_url": "https://smartcity.ventotech.net/assets/upload/2026/01/talk/1768979264911-920dd229.jpg",
                "is_visible": true,
                "is_comments_open": true,
                "comment_count": 0,
                "created_at": "2025-12-18T18:23:54+06:00"
            }
        ],
        "pagination": {
            "current_page": 1,
            "per_page": 5,
            "total": 1,
            "last_page": 1
        }
    },
    "extra": null
} """

    val root = JSONObject(jsonString)
    val classes = mutableListOf<String>()

    generateDataClasses(root, "ApiResponse", classes)

    val output = buildString {
        append("import com.google.gson.annotations.Expose\n")
        append("import com.google.gson.annotations.SerializedName\n\n")
        append(classes.reversed().joinToString("\n\n"))
    }

    File("output.kt").writeText(output)

    println("✅ Kotlin data classes generated with camelCase")
}
