import org.json.JSONArray
import org.json.JSONObject
import utils.FileUtils
import utils.toCamelCase
import utils.toClassName
import java.io.File
import javax.swing.JOptionPane




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



    var rootClassName = askRootClassName()
        .trim()
        .replaceFirstChar { it.uppercase() }



    val classes = mutableListOf<String>()
    rootClassName += "ApiResponse"
    DataClassGenerator.generateDataClasses(rootJson, rootClassName, classes)

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



    // DTO file
    FileUtils.writeKtFile(
        dtoDir,
        rootClassName,
        output
    )

    println("✅ Kotlin data classes generated with camelCase")
}
