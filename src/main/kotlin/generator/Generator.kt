package generator

import model.FieldInfo

abstract class Generator {
    abstract fun generate(className: String, fields: List<FieldInfo>): String
}