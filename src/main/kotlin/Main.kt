import config.GeneratorConfig
import generator.DomainGenerator
import generator.DtoGenerator
import generator.MapperGenerator
import model.FieldInfo
import org.json.JSONArray
import org.json.JSONObject
import parser.DtoParser
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




fun askRootClassName(): String {
    val rootClassName= JOptionPane.showInputDialog(
        null,
        "Enter Root Class Name (e.g. ApiResponse)",
        "Root Class Name",
        JOptionPane.QUESTION_MESSAGE
    )

    if (rootClassName.isNullOrEmpty()){
        JOptionPane.showMessageDialog(
            null,
            "❌ root file  name can not be empty",
            "Error",
            JOptionPane.ERROR_MESSAGE
        )
        askRootClassName()
    }

    return rootClassName
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




fun checkInputFile(file: File){
    if (!file.exists()) {

        JOptionPane.showMessageDialog(
            null,
            "❌ input.json file not found in project root",
            "Error",
            JOptionPane.ERROR_MESSAGE
        )
        error("❌ input.json file not found in project root")
    }
}

fun main() {


    val inputFile = File("input.json")

    checkInputFile(inputFile)

    val jsonText = inputFile.readText()
    val rootJson = JSONObject(jsonText)


    val rootClassName = askRootClassName()
            .trim()
            .replaceFirstChar { it.uppercase() }



    val config = GeneratorConfig(
        rootClassName = rootClassName,
        apiOnlyDtos = setOf(
            "${rootClassName}ApiResponse",
            "PaginationDto",
            "MetaDto",
            "LinksDto"
        )
    )

    val parser = DtoParser(config)
    val dtoGen = DtoGenerator()
    val domainGen = DomainGenerator()
    val mapperGen = MapperGenerator()


    val classes = parser.parse(
        rootJson,
        rootClassName + "ApiResponse",
        isRoot = true
    )

    val outputDir = FileUtils.getOrCreateOutputDir()
    val dtoDir = FileUtils.createSubDir(outputDir, "dto")
    val domainDir = FileUtils.createSubDir(outputDir, "domain")
    val mapperDir = FileUtils.createSubDir(outputDir, "mapper")




    classes.forEach { (dtoName, fields) ->

        FileUtils.writeKtFile(dtoDir, dtoName, dtoGen.generate(dtoName, fields))

        if (dtoName in config.apiOnlyDtos) return@forEach

        FileUtils.writeKtFile(domainDir, dtoName.removeSuffix("Dto"),
            domainGen.generate(dtoName, fields))

        FileUtils.writeKtFile(mapperDir,
            "${dtoName.removeSuffix("Dto")}Mapper",
            mapperGen.generate(dtoName, fields))
    }

    println("✅ Kotlin data classes generated with camelCase")
}



