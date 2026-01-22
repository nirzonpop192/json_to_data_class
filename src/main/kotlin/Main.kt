import model.FieldInfo
import org.json.JSONArray
import org.json.JSONObject
import utils.FileUtils
import utils.toCamelCase
import utils.toClassName
import java.io.File
import javax.swing.JOptionPane


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
                val nestedClassName = originalKey.toClassName() + "Dto"
                generateDataClasses(value, nestedClassName, classes)
                nestedClassName
            }

            is JSONArray -> {
                if (value.length() > 0 && value.get(0) is JSONObject) {
                    val nestedClassName = originalKey.toClassName().removeSuffix("s") +"Dto"
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

fun generateDto(
    json: JSONObject,
    className: String,
    classes: MutableMap<String, List<FieldInfo>>
) {
    val fields = mutableListOf<FieldInfo>()

    json.keys().forEach { key ->
        val camel = key.toCamelCase()
        val value = json.get(key)

        val dtoType = when (value) {
            JSONObject.NULL -> "Any?"
            is Int -> "Int?"
            is Boolean -> "Boolean?"
            is String -> "String?"
            is JSONObject -> {
                val nested = key.toClassName() + "Dto"
                generateDto(value, nested, classes)
                "$nested?"
            }
            is JSONArray -> {
                if (value.length() > 0 && value.get(0) is JSONObject) {
                    val nested = key.toClassName().removeSuffix("s") + "Dto"
                    generateDto(value.getJSONObject(0), nested, classes)
                    "List<$nested>?"
                } else "List<Any>?"
            }
            else -> "Any?"
        }

        val domainType = dtoType
            .replace("?", "")
            .replace("Dto", "")

        fields.add(FieldInfo(key, camel, dtoType, domainType))
    }

    classes[className] = fields
}


fun askRootClassName(): String? {
    return JOptionPane.showInputDialog(
        null,
        "Enter Root Class Name (e.g. ApiResponse)",
        "Root Class Name",
        JOptionPane.QUESTION_MESSAGE
    )
}

fun buildDto(className: String, fields: List<FieldInfo>): String =
    buildString {

        // ---- imports ----
        appendLine("import com.google.gson.annotations.Expose")
        appendLine("import com.google.gson.annotations.SerializedName")
        appendLine()

        // ---- class ----
        append("data class $className(\n")
        append(
            fields.joinToString(",\n") {
                """         @Expose 
            @SerializedName("${it.jsonKey}")
            val ${it.name}: ${it.dtoType}
                """.trimIndent()
            }
        )
        append("\n)")
    }

fun buildDomain(className: String, fields: List<FieldInfo>): String =
    buildString {
        append("data class ${className.removeSuffix("Dto")}(\n")
        append(
            fields.joinToString(",\n") {
                "    val ${it.name}: ${it.domainType}"
            }
        )
        append("\n)")
    }

fun buildMapper(className: String, fields: List<FieldInfo>): String {
    val domain = className.removeSuffix("Dto")

    return buildString {
        append("fun $className.toDomain(): $domain = $domain(\n")
        append(
            fields.joinToString(",\n") {
                val n = it.name
                when {
                    it.dtoType.startsWith("String") -> "    $n = $n.orEmpty()"
                    it.dtoType.startsWith("Int") -> "    $n = $n ?: 0"
                    it.dtoType.startsWith("Boolean") -> "    $n = $n ?: false"
                    it.dtoType.startsWith("List") ->
                        "    $n = $n?.map { it.toDomain() }.orEmpty()"
                    it.dtoType.endsWith("Dto?") ->
                        "    $n = $n?.toDomain()"
                    else -> "    $n = $n"
                }
            }
        )
        append("\n)")
    }
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



    var rootClassName = askRootClassName()
            ?.trim()
            ?.replaceFirstChar { it.uppercase() }

    if (rootClassName.isNullOrEmpty()) {
        JOptionPane.showMessageDialog(
            null,
            "Root class name is required!",
            "Error",
            JOptionPane.ERROR_MESSAGE
        )
        return
    }
    //val root = JSONObject(jsonString)
   // val classes = mutableListOf<String>()
   // rootClassName += "ApiResponse"




   // File("$rootClassName.kt").writeText(output)

    val root = JSONObject(jsonString)
    val classes = mutableMapOf<String, List<FieldInfo>>()
    //rootClassName += "ApiResponse"
    generateDto(root, rootClassName + "Dto", classes)
    // 📁 output/
    val outputDir = FileUtils.getOrCreateOutputDir()

    // 📁 output/dto
    val dtoDir =  FileUtils.createSubDir(outputDir, "dto")

    // 📁 output/domain
    val domainDir = FileUtils.createSubDir(outputDir, "domain")

    // 📁 output/mapper
    val mapperDir = FileUtils.createSubDir(outputDir, "mapper")

    classes.forEach { (dtoName, fields) ->
        val domainName = dtoName.removeSuffix("Dto")

        // DTO file
        FileUtils.writeKtFile(
            dtoDir,
            dtoName,
            buildDto(dtoName, fields)
        )

        // Domain file
        FileUtils.writeKtFile(
            domainDir,
            domainName,
            buildDomain(dtoName, fields)
        )

        // Mapper file
        FileUtils.writeKtFile(
            mapperDir,
            "${domainName}Mapper",
            buildMapper(dtoName, fields)
        )
    }

    println("✅ Kotlin data classes generated with camelCase")
}
