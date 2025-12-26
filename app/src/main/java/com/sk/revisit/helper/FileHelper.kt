package com.sk.revisit.helper

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.io.path.isRegularFile

object FileHelper {
    @Throws(IOException::class)
    fun search(dir: File, ext: String): List<String> {
        val dirP = dir.toPath()
        Files.find(dirP, Int.MAX_VALUE,
            { path, _ -> path.toFile().isFile && path.fileName.toString().lowercase().endsWith(ext) }).use { stream ->
            return stream.map { dirP.relativize(it) }
                .map { it.toString() }
                .collect(Collectors.toList())
        }
    }

    /**
     * Calculates the total size of all files in a folder (recursively).
     *
     * @param folderPath the path to the folder
     * @return the total size in bytes
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun getFolderSize(folderPath: String): Long {
        val folder = Paths.get(folderPath)
        Files.walk(folder).use { walk ->
            return walk.parallel()
                .filter { Files.isRegularFile(it) }
                .mapToLong { path ->
                    try {
                        Files.size(path)
                    } catch (e: IOException) {
                        0L
                    }
                }
                .sum()
        }
    }

    /**
     * Recursively searches for files with the given extension in a directory.
     *
     * @param dir       the directory to search
     * @param extension the file extension to look for (e.g., ".txt")
     * @param files     the list to store found file paths
     */
    fun searchRecursive(dir: File, extension: String, files: MutableList<String>) {
        val fileList = dir.listFiles() ?: return
        for (file in fileList) {
            if (file.isDirectory) {
                searchRecursive(file, extension, files)
            } else if (file.name.lowercase().endsWith(extension.lowercase())) {
                files.add(file.path)
            }
        }
    }

    /**
     * Searches for HTML files in a directory and its subdirectories in parallel.
     *
     * @param dir   the directory to search
     * @param files the list to store found HTML file paths
     * @throws IOException if an I/O error occurs
     */
    @Throws(IOException::class)
    fun searchHtmlParallel(dir: File, files: MutableList<String>) {
        val folder = dir.toPath()
        Files.walk(folder).use { walk ->
            walk.parallel()
                .filter { it.isRegularFile() }
                .filter { isHTML(it) }
                .map { it.toString() }
                .forEach { files.add(it) }
        }
    }

    /**
     * Prepares a file for writing. Creates parent directories and the file if they do not exist.
     *
     * @param filepath the path to the file
     * @return the File object
     * @throws IOException if the file or directories cannot be created
     */
    @Throws(IOException::class)
    fun prepareFile(filepath: String): File {
        val file = File(filepath)
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw IOException("Can't create directory: ${parentDir.absolutePath}")
        }
        if (!file.exists() && !file.createNewFile()) {
            throw IOException("Can't create file: $filepath")
        }
        return file
    }

    /**
     * Checks if a path is an HTML file.
     *
     * @param path the path to check
     * @return true if the file is .html or .htm (case-insensitive), false otherwise
     */
    private fun isHTML(path: Path): Boolean {
        val name = path.fileName.toString().lowercase()
        return name.endsWith(".html") || name.endsWith(".htm")
    }
}
