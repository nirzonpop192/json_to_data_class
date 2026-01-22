package model

data class FieldInfo(
    val jsonKey: String,
    val name: String,
    val dtoType: String,
    val domainType: String
)
