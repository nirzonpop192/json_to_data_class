package config

data class GeneratorConfig(
    val rootClassName: String,
    val apiOnlyDtos: Set<String>
)
