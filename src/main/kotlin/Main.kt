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

fun askRootClassName(): String? {
    return JOptionPane.showInputDialog(
        null,
        "Enter Root Class Name (e.g. ApiResponse)",
        "Root Class Name",
        JOptionPane.QUESTION_MESSAGE
    )
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
    val root = JSONObject(jsonString)
    val classes = mutableListOf<String>()
    rootClassName += "ApiResponse"
    generateDataClasses(root, rootClassName, classes)

    val output = buildString {
        append("import com.google.gson.annotations.Expose\n")
        append("import com.google.gson.annotations.SerializedName\n\n")
        append(classes.reversed().joinToString("\n\n"))
    }

   // File("$rootClassName.kt").writeText(output)


    // 📁 output/
    val outputDir = FileUtils.getOrCreateOutputDir()

    // 📁 output/dto
    val dtoDir =  FileUtils.createSubDir(outputDir, "dto")

//    // 📁 output/domain
//    val domainDir = createSubDir(outputDir, "domain")
//
//    // 📁 output/mapper
//    val mapperDir = createSubDir(outputDir, "mapper")

    // DTO file
    FileUtils.writeKtFile(
        dtoDir,
        rootClassName,
        output
    )

    println("✅ Kotlin data classes generated with camelCase")
}
