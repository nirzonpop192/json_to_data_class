package utils

import java.io.File


object FileUtils {

    fun getOrCreateOutputDir(baseName: String = "output"): File {
        val dir = File(baseName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createSubDir(parent: File, name: String): File {
        val dir = File(parent, name)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun writeKtFile(
        dir: File,
        fileName: String,
        content: String
    ) {
        File(dir, "$fileName.kt").writeText(content)
    }
}
