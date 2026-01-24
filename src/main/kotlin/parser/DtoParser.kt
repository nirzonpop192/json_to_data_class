package parser

import config.GeneratorConfig
import model.FieldInfo
import org.json.JSONArray
import org.json.JSONObject
import utils.toCamelCase
import utils.toClassName

class DtoParser(private val config: GeneratorConfig) {

    fun parse(
        json: JSONObject,
        className: String,
        isRoot: Boolean = false,
        result: MutableMap<String, List<FieldInfo>> = mutableMapOf()
    ): Map<String, List<FieldInfo>> {

        val fields = mutableListOf<FieldInfo>()

        json.keys().forEach { key ->
            val camel = key.toCamelCase()
            val value = json.get(key)

            val dtoType = resolveDtoType(key, value, isRoot, result)

            val domainType = dtoType
                .removeSuffix("?")
                .replace("Dto", "")

            fields += FieldInfo(key, camel, dtoType, domainType)
        }

        result[className] = fields
        return result
    }

    private fun resolveDtoType(
        key: String,
        value: Any,
        isRoot: Boolean,
        result: MutableMap<String, List<FieldInfo>>
    ): String = when (value) {

        JSONObject.NULL -> "Any?"

        is Int -> "Int?"
        is Boolean -> "Boolean?"
        is String -> "String?"

        is JSONObject -> {
            val nestedName =
                if (isRoot && key == "data")
                    "${config.rootClassName}DataDto"
                else
                    key.toClassName() + "Dto"

            parse(value, nestedName, false, result)
            "$nestedName?"
        }

        is JSONArray -> {
            if (value.length() > 0 && value.get(0) is JSONObject) {
                val nested = key.toClassName().removeSuffix("s") + "Dto"
                parse(value.getJSONObject(0), nested, false, result)
                "List<$nested>?"
            } else "List<Any?>?"
        }

        else -> "Any?"
    }
}
