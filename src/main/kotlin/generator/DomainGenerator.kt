package generator

import model.FieldInfo

class DomainGenerator: Generator() {

   override fun generate(className: String, fields: List<FieldInfo>): String =
        buildString {
            append("data class ${className.removeSuffix("Dto")}(\n")
            append(
                fields.joinToString(",\n") {
                    "    val ${it.name}: ${it.domainType}"
                }
            )
            append("\n)")
        }
}
