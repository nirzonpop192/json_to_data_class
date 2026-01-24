package generator

import model.FieldInfo


class DtoGenerator : Generator() {

    override fun generate(className: String, fields: List<FieldInfo>): String =
        buildString {
            appendLine("import com.google.gson.annotations.Expose")
            appendLine("import com.google.gson.annotations.SerializedName")
            appendLine()

            append("data class $className(\n")
            append(
                fields.joinToString(",\n") {
                    """
                    @Expose
                    @SerializedName("${it.jsonKey}")
                    val ${it.name}: ${it.dtoType}
                    """.trimIndent()
                }
            )
            append("\n)")
        }
}
