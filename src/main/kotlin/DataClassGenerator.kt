import org.json.JSONArray
import org.json.JSONObject
import utils.toCamelCase
import utils.toClassName

object DataClassGenerator {

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
}