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
            /***
             * convert the variable name to camel case
             */
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
            /***
             * if json key is in root class and there key is data than class name
             * should be root class name then data DTO(Data transfer object)
             * otherwise data class
             */
            val nestedClassName =
                if (isRoot && key == "data")
                    "${config.rootClassName}DataDto"
                else
                    key.toClassName() + "Dto"

            /***
             * call recursive function to break down the {@nestedClassName}
             */

            parse(value, nestedClassName, false, result)

            "$nestedClassName?"
        }

        is JSONArray -> {
            /***
             * in json array is not empty and frist valu is json object
             */
            if (value.length() > 0 && value.get(0) is JSONObject) {

                val nestedClassName = key.toClassName().removeSuffix("s") + "Dto"

                /***
                 * call recursive function to break down the {@nestedClassName}
                 */

                parse(value.getJSONObject(0), nestedClassName, false, result)
                "List<$nestedClassName>?"
            } else "List<Any?>?"
        }

        else -> "Any?"
    }
}
