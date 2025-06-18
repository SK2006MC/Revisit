package com.sk.revisit.helper;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHelper {
    public static List<String> search(File dir, String ext) throws IOException {
        Path dirP = dir.toPath();
        try (Stream<Path> stream = Files.find(dirP, Integer.MAX_VALUE,
                (path, attr) -> path.toFile().isFile() && path.getFileName().toString().toLowerCase().endsWith(ext))) {
            return stream.map(dirP::relativize)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Calculates the total size of all files in a folder (recursively).
     *
     * @param folderPath the path to the folder
     * @return the total size in bytes
     * @throws IOException if an I/O error occurs
     */
    public static long getFolderSize(String folderPath) throws IOException {
        Path folder = Paths.get(folderPath);
        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.parallel()
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        }
    }

    /**
     * Recursively searches for files with the given extension in a directory.
     *
     * @param dir       the directory to search
     * @param extension the file extension to look for (e.g., ".txt")
     * @param files     the list to store found file paths
     */
    public static void searchRecursive(@NonNull File dir, String extension, List<String> files) {
        File[] fileList = dir.listFiles();
        if (fileList == null) {
            return;
        }
        for (File file : fileList) {
            if (file.isDirectory()) {
                searchRecursive(file, extension, files);
            } else if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                files.add(file.getPath());
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
    public static void searchHtmlParallel(File dir, List<String> files) throws IOException {
        Path folder = dir.toPath();
        try (Stream<Path> walk = Files.walk(folder)) {
            walk.parallel()
                    .filter(Files::isRegularFile)
                    .filter(FileHelper::isHTML)
                    .map(Path::toString)
                    .forEach(files::add);
        }
    }

    /**
     * Prepares a file for writing. Creates parent directories and the file if they do not exist.
     *
     * @param filepath the path to the file
     * @return the File object
     * @throws IOException if the file or directories cannot be created
     */
    public static File prepareFile(String filepath) throws IOException {
        File file = new File(filepath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Can't create directory: " + parentDir.getAbsolutePath());
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Can't create file: " + filepath);
        }
        return file;
    }

    /**
     * Checks if a path is an HTML file.
     *
     * @param path the path to check
     * @return true if the file is .html or .htm (case-insensitive), false otherwise
     */
    static boolean isHTML(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".html") || name.endsWith(".htm");
    }
}
