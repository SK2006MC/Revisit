package com.sk.revisit.helper

import java.io.File
import java.io.IOException

object FileHelper {

    /**
     * Finds files with specific extension and returns relative paths.
     * Replaces the NIO Files.find implementation.
     */
    fun search(dir: File, ext: String): List<String> {
        val extension = ext.removePrefix(".")
        return dir.walkTopDown()
            .filter { it.isFile && it.extension.equals(extension, ignoreCase = true) }
            .map { it.relativeTo(dir).path }
            .toList()
    }

    /**
     * Calculates the total size of all files in a folder (recursively).
     * Replaces the NIO Files.walk/parallel implementation.
     */
    fun getFolderSize(folderPath: String): Long {
        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) return 0L

        return folder.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }

    /**
     * Recursively searches for files with the given extension.
     * This version is what your WebpageRepository specifically calls.
     */
    fun searchRecursive(dir: File, extension: String, files: MutableList<String>) {
        dir.walkTopDown()
            .filter { it.isFile && it.name.endsWith(extension, ignoreCase = true) }
            .forEach { files.add(it.absolutePath) }
    }

    /**
     * Searches for HTML files. In Kotlin, we can simplify the parallel logic
     * because the Repository already handles the threading via Coroutines.
     */
    fun searchHtmlParallel(dir: File, files: MutableList<String>) {
        // Since WebpageRepository calls this inside Dispatchers.IO,
        // a standard walk is efficient and thread-safe.
        dir.walkTopDown()
            .filter { it.isFile && (it.extension.equals("html", true) || it.extension.equals("htm", true)) }
            .forEach { files.add(it.absolutePath) }
    }

    /**
     * Prepares a file for writing. Creates parent directories and the file if they do not exist.
     */
    @Throws(IOException::class)
    fun prepareFile(filepath: String): File {
        return File(filepath).apply {
            parentFile?.let {
                if (!it.exists() && !it.mkdirs()) {
                    throw IOException("Can't create directory: ${it.absolutePath}")
                }
            }
            if (!exists() && !createNewFile()) {
                throw IOException("Can't create file: $absolutePath")
            }
        }
    }
}