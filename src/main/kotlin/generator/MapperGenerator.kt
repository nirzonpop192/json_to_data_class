package generator

import model.FieldInfo

class MapperGenerator: Generator() {
    override fun generate(className: String, fields: List<FieldInfo>): String {
        val domain = className.removeSuffix("Dto")

        return buildString {
            append("fun $className.toDomain(): $domain = $domain(\n")
            append(
                fields.joinToString(",\n") {
                    val n = it.name
                    when {
                        it.dtoType.startsWith("String")     -> "    $n = $n.orEmpty()"
                        it.dtoType.startsWith("Int")        -> "    $n = $n ?: 0"
                        it.dtoType.startsWith("Boolean")    -> "    $n = $n ?: false"
                        it.dtoType.startsWith("List")       -> "    $n = $n?.map { it.toDomain() }.orEmpty()"
                        it.dtoType.endsWith("Dto?")         -> "    $n = $n?.toDomain()"
                        else -> "    $n = $n"
                    }
                }
            )
            append("\n)")
        }
    }
}